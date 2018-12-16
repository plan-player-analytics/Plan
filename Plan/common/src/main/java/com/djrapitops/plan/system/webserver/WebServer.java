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
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

/**
 * @author Rsl1122
 */
@Singleton
public class WebServer implements SubSystem {

    private final Locale locale;
    private final PlanFiles files;
    private final PlanConfig config;

    private final ServerProperties serverProperties;
    private final RequestHandler requestHandler;

    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private int port;
    private boolean enabled = false;
    private HttpServer server;

    private boolean usingHttps = false;

    @Inject
    public WebServer(
            Locale locale,
            PlanFiles files,
            PlanConfig config,
            ServerProperties serverProperties,
            PluginLogger logger,
            ErrorHandler errorHandler,
            RequestHandler requestHandler
    ) {
        this.locale = locale;
        this.files = files;
        this.config = config;
        this.serverProperties = serverProperties;

        this.requestHandler = requestHandler;

        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void enable() throws EnableException {
        this.port = config.get(WebserverSettings.PORT);

        initServer();

        if (!isEnabled()) {
            if (Check.isBungeeAvailable() || Check.isVelocityAvailable()) {
                throw new EnableException(locale.getString(PluginLang.ENABLE_FAIL_NO_WEB_SERVER_BUNGEE));
            }
            if (config.isTrue(WebserverSettings.DISABLED)) {
                logger.warn(locale.getString(PluginLang.ENABLE_NOTIFY_WEB_SERVER_DISABLED));
            } else {
                logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_PORT_BIND, port));
            }
        }

        requestHandler.getResponseHandler().registerPages();
    }

    /**
     * Starts up the WebServer in a new Thread Pool.
     */
    private void initServer() {
        if (!(Check.isBungeeAvailable() || Check.isVelocityAvailable()) && config.isTrue(WebserverSettings.DISABLED)) {
            // Bukkit WebServer has been disabled.
            return;
        }

        if (enabled) {
            // Server is already enabled stop code
            return;
        }

        try {
            usingHttps = startHttpsServer();

            logger.debug(usingHttps ? "Https Start Successful." : "Https Start Failed.");

            if (!usingHttps) {
                logger.log(L.INFO_COLOR, "§e" + locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP_USER_AUTH));
                server = HttpServer.create(new InetSocketAddress(config.getString(WebserverSettings.INTERNAL_IP), port), 10);
            } else if (server == null) {
                logger.log(L.INFO_COLOR, "§eWebServer: Proxy HTTPS Override enabled. HTTP Server in use, make sure that your Proxy webserver is routing with HTTPS and AlternativeIP.Link points to the Proxy");
                server = HttpServer.create(new InetSocketAddress(config.getString(WebserverSettings.INTERNAL_IP), port), 10);
            }
            server.createContext("/", requestHandler);

            ExecutorService executor = new ThreadPoolExecutor(
                    4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                    new ThreadFactoryBuilder().setNameFormat("Plan WebServer Thread-%d").build()
            );
            server.setExecutor(executor);
            server.start();

            enabled = true;

            logger.info(locale.getString(PluginLang.ENABLED_WEB_SERVER, server.getAddress().getPort(), getAccessAddress()));
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            enabled = false;
        }
    }

    private boolean startHttpsServer() {
        String keyStorePath = config.getString(WebserverSettings.CERTIFICATE_PATH);

        if (keyStorePath.equalsIgnoreCase("proxy")) {
            return true;
        }

        try {
            if (!Paths.get(keyStorePath).isAbsolute()) {
                keyStorePath = files.getDataFolder() + File.separator + keyStorePath;
            }
        } catch (InvalidPathException e) {
            logger.error("WebServer: Could not find Keystore: " + e.getMessage());
            errorHandler.log(L.ERROR, this.getClass(), e);
        }

        char[] storepass = config.getString(WebserverSettings.CERTIFICATE_STOREPASS).toCharArray();
        char[] keypass = config.getString(WebserverSettings.CERTIFICATE_KEYPASS).toCharArray();
        String alias = config.getString(WebserverSettings.CERTIFICATE_ALIAS);

        boolean startSuccessful = false;
        try (FileInputStream fIn = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance("JKS");

            keystore.load(fIn, storepass);
            Certificate cert = keystore.getCertificate(alias);

            if (cert == null) {
                throw new IllegalStateException("Certificate with Alias: " + alias + " was not found in the Keystore.");
            }

            logger.info("Certificate: " + cert.getType());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keypass);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);

            server = HttpsServer.create(new InetSocketAddress(config.getString(WebserverSettings.INTERNAL_IP), port), 10);
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(keyManagerFactory.getKeyManagers(), null/*trustManagerFactory.getTrustManagers()*/, null);

            ((HttpsServer) server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    SSLEngine engine = sslContext.createSSLEngine();

                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    SSLParameters defaultSSLParameters = sslContext.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);
                }
            });
            startSuccessful = true;
        } catch (IllegalStateException e) {
            logger.error(e.getMessage());
            errorHandler.log(L.ERROR, this.getClass(), e);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_SSL_CONTEXT));
            errorHandler.log(L.ERROR, this.getClass(), e);
        } catch (FileNotFoundException e) {
            logger.log(L.INFO_COLOR, "§e" + locale.getString(PluginLang.WEB_SERVER_NOTIFY_NO_CERT_FILE, keyStorePath));
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP));
        } catch (IOException e) {
            logger.error("WebServer: " + e);
            errorHandler.log(L.ERROR, this.getClass(), e);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_STORE_LOAD));
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
        return startSuccessful;
    }

    /**
     * @return if the WebServer is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shuts down the server - Async thread is closed with shutdown boolean.
     */
    @Override
    public void disable() {
        if (server != null) {
            shutdown();
            logger.info(locale.getString(PluginLang.DISABLED_WEB_SERVER));
        }
        enabled = false;
    }

    private void shutdown() {
        server.stop(0);
        Executor executor = server.getExecutor();
        if (executor instanceof ExecutorService) {
            ExecutorService service = (ExecutorService) executor;
            service.shutdown();
            try {
                if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
                    service.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("WebServer ExecutorService shutdown thread interrupted on disable: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public String getProtocol() {
        return usingHttps ? "https" : "http";
    }

    public boolean isUsingHTTPS() {
        return usingHttps;
    }

    public boolean isAuthRequired() {
        return isUsingHTTPS();
    }

    public String getAccessAddress() {
        return isEnabled() ? getProtocol() + "://" + getIP() : config.getString(WebserverSettings.EXTERNAL_LINK);
    }

    private String getIP() {
        return config.isTrue(WebserverSettings.SHOW_ALTERNATIVE_IP)
                ? config.getString(WebserverSettings.ALTERNATIVE_IP).replace("%port%", String.valueOf(port))
                : serverProperties.getIp() + ":" + port;
    }
}
