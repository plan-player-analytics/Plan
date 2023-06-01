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
import com.djrapitops.plan.utilities.java.ThreadContextClassLoaderSwap;
import net.playeranalytics.plugin.server.PluginLogger;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class JettyWebserver implements WebServer {

    private final PluginLogger logger;
    private final WebserverConfiguration webserverConfiguration;
    private final LegacyJettySSLContextLoader legacyJettySSLContextLoader;
    private final JettyRequestHandler jettyRequestHandler;
    private final ResponseResolver responseResolver;
    private final WebserverLogMessages webserverLogMessages;

    private int port;
    private boolean usingHttps;
    private Server webserver;

    @Inject
    public JettyWebserver(PluginLogger logger, WebserverConfiguration webserverConfiguration, LegacyJettySSLContextLoader legacyJettySSLContextLoader, JettyRequestHandler jettyRequestHandler, ResponseResolver responseResolver) {
        this.logger = logger;
        this.webserverConfiguration = webserverConfiguration;
        webserverLogMessages = webserverConfiguration.getWebserverLogMessages();
        this.legacyJettySSLContextLoader = legacyJettySSLContextLoader;
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
        webserver.setStopAtShutdown(true);

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

        HTTP2CServerConnectionFactory http2CConnector = new HTTP2CServerConnectionFactory(configuration);
        http2CConnector.setConnectProtocolEnabled(true);


        ServerConnector connector = sslContext
                .map(sslContextFactory -> {
                    HTTP2ServerConnectionFactory http2Connector = new HTTP2ServerConnectionFactory(configuration);
                    http2Connector.setConnectProtocolEnabled(true);
                    ALPNServerConnectionFactory alpn = getAlpnServerConnectionFactory(httpConnector.getProtocol());

                    return new ServerConnector(webserver, sslContextFactory, alpn, httpConnector, http2Connector, http2CConnector);
                })
                .orElseGet(() -> {
                    if (webserverConfiguration.isProxyModeHttps()) {
                        webserverLogMessages.authenticationUsingProxy();
                    } else {
                        webserverLogMessages.authenticationNotPossible();
                    }
                    return new ServerConnector(webserver, httpConnector, http2CConnector);
                });

        connector.setPort(port);
        String internalIP = webserverConfiguration.getInternalIP();
        connector.setHost(internalIP);
        webserver.addConnector(connector);

        webserver.setHandler(jettyRequestHandler);

        String startFailure = "Failed to start Jetty webserver: ";
        try {
            webserver.start();
        } catch (IOException e) {
            if (e.getMessage().contains("Failed to bind")) {
                boolean defaultInternalIp = "0.0.0.0".equals(internalIP);
                String causeHelp = defaultInternalIp ? ", is the port (" + port + ") in use?" : ", is the Internal_IP (" + internalIP + ") invalid? (Use 0.0.0.0 for automatic)";
                throw new EnableException(startFailure + e.getMessage().replace("0.0.0.0", "") + causeHelp, e);
            } else {
                throw new EnableException(startFailure + e.toString(), e);
            }
        } catch (Exception e) {
            throw new EnableException(startFailure + e.toString(), e);
        }

        webserverLogMessages.infoWebserverEnabled(getPort());
        sslContext.map(SslContextFactory::getKeyStore).ifPresent(this::logCertificateExpiryInformation);

        responseResolver.registerPages();

        webserverConfiguration.getAllowedIpList().prepare();
    }

    private void logCertificateExpiryInformation(KeyStore keyStore) {
        try {
            Certificate certificate = keyStore.getCertificate(webserverConfiguration.getAlias());
            if (certificate instanceof X509Certificate) {
                long expires = ((X509Certificate) certificate).getNotAfter().getTime();
                long timeLeft = expires - System.currentTimeMillis();
                webserverLogMessages.certificateExpiryIn(expires);
                if (timeLeft < TimeUnit.DAYS.toMillis(7L)) {
                    webserverLogMessages.certificateExpiryIsNear(timeLeft);
                }
            }
        } catch (KeyStoreException ignored) {
            // Don't care, just warning the user.
        }
    }

    private ALPNServerConnectionFactory getAlpnServerConnectionFactory(String protocol) {
        ClassLoader pluginClassLoader = getClass().getClassLoader();
        return ThreadContextClassLoaderSwap.performOperation(pluginClassLoader, () -> {
            try {
                Class.forName("org.eclipse.jetty.alpn.java.server.JDK9ServerALPNProcessor");
                // ALPN is protocol upgrade protocol required for upgrading http 1.1 connections to 2
                ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory("h2", "h2c", "http/1.1");
                alpn.setDefaultProtocol(protocol);
                return alpn;
            } catch (IllegalStateException | ClassNotFoundException ignored) {
                logger.warn("JDK9ServerALPNProcessor not found. ALPN (HTTP/2 upgrade protocol) is not available.");
                return null;
            }
        });
    }

    private Optional<SslContextFactory.Server> getSslContextFactory() {
        if (webserverConfiguration.isProxyModeHttps()) {
            return Optional.empty();
        }

        String keyStorePath = webserverConfiguration.getKeyStorePath();
        if (!new File(keyStorePath).exists()) {
            webserverLogMessages.keystoreFileNotFound(keyStorePath);
            return Optional.empty();
        }

        String storepass = webserverConfiguration.getKeyStorePassword();
        String keypass = webserverConfiguration.getKeyManagerPassword();
        String alias = webserverConfiguration.getAlias();

        if (keyStorePath.endsWith(".jks") && "DefaultPlanCert".equals(alias)) {
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
            return legacyJettySSLContextLoader.load(keyStorePath, storepass, keypass, alias);
        }

        if (!verifyAliasIsInKeystore(keyStorePath, storepass, alias)) {
            return Optional.empty();
        }

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setSniRequired(false);

        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(storepass);
        sslContextFactory.setKeyManagerPassword(keypass);
        sslContextFactory.setCertAlias(alias);
        return Optional.of(sslContextFactory);
    }

    private boolean verifyAliasIsInKeystore(String keyStorePath, String storepass, String alias) {
        String keyStoreKind = keyStorePath.endsWith(".p12") ? "PKCS12" : "JKS";
        try (FileInputStream fIn = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance(keyStoreKind);

            keystore.load(fIn, storepass.toCharArray());
            Certificate cert = keystore.getCertificate(alias);

            if (cert == null) {
                webserverLogMessages.invalidCertificateMissingAlias(alias, keyStorePath);
                return false;
            }
            return true;
        } catch (KeyStoreException | CertificateException e) {
            webserverLogMessages.unableToLoadKeystore(e, keyStorePath);
        } catch (EOFException e) {
            webserverLogMessages.wrongCertFileFormat();
        } catch (NoSuchAlgorithmException | IOException e) {
            webserverLogMessages.keystoreLoadingError(e);
        }
        return false;
    }

    @Override
    public boolean isEnabled() {
        return webserver != null && (webserver.isStarting() || webserver.isStarted());
    }

    @Override
    public void disable() {
        try {
            if (webserver != null) {
                webserver.stop();
                webserver.destroy();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        return usingHttps || webserverConfiguration.isProxyModeHttps();
    }

    @Override
    public boolean isAuthRequired() {
        return isUsingHTTPS() && webserverConfiguration.isAuthenticationEnabled();
    }

    @Override
    public int getPort() {
        return port;
    }
}
