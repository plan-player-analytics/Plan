/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.webserver.ResponseResolver;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverLogMessages;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Optional;

@Singleton
public class JettyWebserver implements WebServer {

    private final PluginLogger logger;
    private final ErrorLogger errorLogger;
    private final Locale locale;
    private final PlanConfig config;
    private final WebserverConfiguration webserverConfiguration;
    private final JettyRequestHandler jettyRequestHandler;
    private final ResponseResolver responseResolver;
    private final WebserverLogMessages webserverLogMessages;

    private int port;
    private boolean usingHttps;
    private Server webserver;

    @Inject
    public JettyWebserver(PluginLogger logger, ErrorLogger errorLogger, Locale locale, PlanConfig config, WebserverConfiguration webserverConfiguration, JettyRequestHandler jettyRequestHandler, ResponseResolver responseResolver) {
        this.logger = logger;
        this.errorLogger = errorLogger;
        this.locale = locale;
        this.config = config;
        this.webserverConfiguration = webserverConfiguration;
        webserverLogMessages = webserverConfiguration.getWebserverLogMessages();
        this.jettyRequestHandler = jettyRequestHandler;
        this.responseResolver = responseResolver;
    }

    @Override
    public void enable() {
        if (isEnabled()) return;

        if (webserverConfiguration.isWebserverDisabled()) {
            webserverLogMessages.warnWebserverDisabledByConfig();
            return;
        }

        webserver = new Server();

        this.port = webserverConfiguration.getPort();

        HttpConfiguration configuration = new HttpConfiguration();
        Optional<SslContextFactory.Server> sslContext = getSslContextFactory();
        sslContext.ifPresent(ssl -> {
            configuration.setSecureScheme("https");
            configuration.setSecurePort(port);

            SecureRequestCustomizer serverNameIdentifierCheckSkipper = new SecureRequestCustomizer();
            serverNameIdentifierCheckSkipper.setSniHostCheck(false);
            serverNameIdentifierCheckSkipper.setSniRequired(false);
            configuration.addCustomizer(serverNameIdentifierCheckSkipper);

            usingHttps = true;
        });

        HttpConnectionFactory httpConnector = new HttpConnectionFactory(configuration);

        HTTP2ServerConnectionFactory http2Connector = new HTTP2ServerConnectionFactory(configuration);
        http2Connector.setConnectProtocolEnabled(true);
        HTTP2CServerConnectionFactory http2CConnector = new HTTP2CServerConnectionFactory(configuration);
        http2CConnector.setConnectProtocolEnabled(true);

        // TODO ALPN is protocol upgrade protocol required for upgrading http 1.1 connections to 2
//        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory("h2", "h2c", "http/1.1");
//        alpn.setDefaultProtocol("http/1.1");

        ServerConnector connector = sslContext
                .map(sslContextFactory -> new ServerConnector(webserver, sslContextFactory, httpConnector, http2Connector, http2CConnector))
                .orElseGet(() -> {
                    webserverLogMessages.authenticationNotPossible();
                    return new ServerConnector(webserver, httpConnector, http2CConnector);
                });

        connector.setPort(port);
        webserver.addConnector(connector);

        if (usingHttps) {
            webserver.setHandler(new HandlerList(new SecuredRedirectHandler(), jettyRequestHandler));
        } else {
            webserver.setHandler(jettyRequestHandler);
        }

        try {
            webserver.start();
        } catch (Exception e) {
            throw new EnableException("Failed to start Jetty webserver: " + e.toString(), e);
        }

        webserverLogMessages.infoWebserverEnabled(getPort());

        responseResolver.registerPages();
    }

    private Optional<SslContextFactory.Server> getSslContextFactory() {
        String keyStorePath = webserverConfiguration.getKeyStorePath();
        if ("proxy".equals(keyStorePath)) {
            webserverLogMessages.authenticationUsingProxy();
            return Optional.empty();
        }

        if (!new File(keyStorePath).exists()) {
            webserverLogMessages.keystoreFileNotFound();
            return Optional.empty();
        }

        String storepass = config.get(WebserverSettings.CERTIFICATE_STOREPASS);
        String keypass = config.get(WebserverSettings.CERTIFICATE_KEYPASS);
        String alias = config.get(WebserverSettings.CERTIFICATE_ALIAS);

        if (keyStorePath.endsWith(".jks") && alias.equals("DefaultPlanCert")) {
            logger.warn("You're using self-signed PlanCert.jks certificate included with Plan.jar (Considered legacy since 5.5), it has expired and can cause issues.");
            logger.info("Create new self-signed certificate using openssl:");
            logger.info("    openssl req -x509 -newkey rsa:4096 -keyout myKey.pem -out cert.pem -days 3650");
            logger.info("    openssl pkcs12 -export -out keyStore.p12 -inkey myKey.pem -in cert.pem -name alias -passout pass:<password> -passin pass:<password>");
            logger.info("Then change config settings to match.");
            logger.info("  SSL_certificate:");
            logger.info("      KeyStore_path: keyStore.p12");
            logger.info("      Key_pass: <password>");
            logger.info("      Store_pass: <password>");
            logger.info("      Alias: alias");
            return legacySSLContext(keyStorePath, storepass, keypass, alias);
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setSniRequired(false);

        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(storepass);
        sslContextFactory.setKeyManagerPassword(keypass);
        sslContextFactory.setCertAlias(alias);

        return Optional.of(sslContextFactory);
    }

    @NotNull
    private Optional<SslContextFactory.Server> legacySSLContext(String keyStorePath, String storepass, String keypass, String alias) {
        String keyStoreKind = keyStorePath.endsWith(".p12") ? "PKCS12" : "JKS";
        try (FileInputStream fIn = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance(keyStoreKind);

            keystore.load(fIn, storepass.toCharArray());
            Certificate cert = keystore.getCertificate(alias);

            if (cert == null) {
                throw new IllegalStateException("Alias: '" + alias + "' was not found in file " + keyStorePath + ".");
            }

            logger.info("Certificate: " + cert.getType());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keypass.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), null/*trustManagerFactory.getTrustManagers()*/, null);

            SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setSslContext(sslContext);

            return Optional.of(sslContextFactory);
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_SSL_CONTEXT));
            errorLogger.error(e, ErrorContext.builder().related(keyStoreKind).build());
        } catch (EOFException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_EMPTY_FILE));
        } catch (FileNotFoundException e) {
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_NO_CERT_FILE, keyStorePath));
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP));
        } catch (IOException e) {
            errorLogger.error(e, ErrorContext.builder().related(config.get(WebserverSettings.INTERNAL_IP) + ":" + port).build());
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_STORE_LOAD));
            errorLogger.error(e, ErrorContext.builder()
                    .whatToDo("Make sure the Certificate settings are correct / You can try remaking the keystore without -passin or -passout parameters.")
                    .related(keyStorePath).build());
        }
        return Optional.empty();
    }

    @Override
    public boolean isEnabled() {
        return webserver != null && (webserver.isStarting() || webserver.isStarted());
    }

    @Override
    public void disable() {
        try {
            if (webserver != null) webserver.stop();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getProtocol() {
        return isUsingHTTPS() ? "https" : "http";
    }

    @Override
    public boolean isUsingHTTPS() {
        return usingHttps;
    }

    @Override
    public boolean isAuthRequired() {
        return isUsingHTTPS() && config.isFalse(WebserverSettings.DISABLED_AUTHENTICATION);
    }

    @Override
    public int getPort() {
        return port;
    }
}
