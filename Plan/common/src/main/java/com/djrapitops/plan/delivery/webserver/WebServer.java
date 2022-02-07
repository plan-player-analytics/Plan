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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.*;
import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.*;

/**
 * @author AuroraLS3
 */
@Singleton
public class WebServer implements SubSystem {

    private final Locale locale;
    private final PlanFiles files;
    private final PlanConfig config;

    private final Addresses addresses;
    private final RequestHandler requestHandler;

    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private int port;
    private boolean enabled = false;
    private HttpServer server;

    private boolean usingHttps = false;

    @Inject
    public WebServer(
            Locale locale,
            PlanFiles files,
            PlanConfig config,
            Addresses addresses,
            PluginLogger logger,
            ErrorLogger errorLogger,
            RequestHandler requestHandler
    ) {
        this.locale = locale;
        this.files = files;
        this.config = config;
        this.addresses = addresses;

        this.requestHandler = requestHandler;

        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void enable() {
        this.port = config.get(WebserverSettings.PORT);

        initServer();

        if (!addresses.getAccessAddress().isPresent()) {
            logger.warn(locale.getString(PluginLang.ENABLE_NOTIFY_BAD_IP));
        }

        if (!isEnabled()) {
            if (config.isTrue(WebserverSettings.DISABLED)) {
                logger.info(locale.getString(PluginLang.ENABLE_NOTIFY_WEB_SERVER_DISABLED));
            } else {
                logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_PORT_BIND, port));
            }
        } else if (config.isTrue(WebserverSettings.IP_WHITELIST)) {
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_IP_WHITELIST));
        }

        requestHandler.getResponseResolver().registerPages();
    }

    /**
     * Starts up the WebServer in a new Thread Pool.
     */
    private void initServer() {
        if (config.isTrue(WebserverSettings.DISABLED)) {
            // WebServer has been disabled.
            return;
        }

        if (enabled) {
            // Server is already enabled stop code
            return;
        }

        try {
            usingHttps = startHttpsServer();

            if (!usingHttps) {
                logger.info("§e" + locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP_USER_AUTH));
                server = HttpServer.create(new InetSocketAddress(config.get(WebserverSettings.INTERNAL_IP), port), 10);
            } else if (server == null) {
                logger.info("§e" + locale.getString(PluginLang.WEB_SERVER_NOTIFY_USING_PROXY_MODE));
                server = HttpServer.create(new InetSocketAddress(config.get(WebserverSettings.INTERNAL_IP), port), 10);
            } else if (config.isTrue(WebserverSettings.DISABLED_AUTHENTICATION)) {
                logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTPS_USER_AUTH));
            }
            server.createContext("/", requestHandler);

            ExecutorService executor = new ThreadPoolExecutor(
                    4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100),
                    new BasicThreadFactory.Builder()
                            .namingPattern("Plan WebServer Thread-%d")
                            .uncaughtExceptionHandler((thread, throwable) -> {
                                if (config.isTrue(PluginSettings.DEV_MODE)) {
                                    errorLogger.warn(throwable, ErrorContext.builder()
                                            .whatToDo("THIS ERROR IS ONLY LOGGED IN DEV MODE")
                                            .build());
                                }
                            }).build()
            );
            server.setExecutor(executor);
            server.start();

            enabled = true;

            String address = addresses.getAccessAddress().orElse(addresses.getFallbackLocalhostAddress());
            logger.info(locale.getString(PluginLang.ENABLED_WEB_SERVER, server.getAddress().getPort(), address));

            boolean usingAlternativeIP = config.isTrue(WebserverSettings.SHOW_ALTERNATIVE_IP);
            if (!usingAlternativeIP && !addresses.getAccessAddress().isPresent()) {
                logger.info("§e" + locale.getString(PluginLang.ENABLE_NOTIFY_EMPTY_IP));
            }
        } catch (BindException failedToBind) {
            logger.error("Webserver failed to bind port: " + failedToBind.toString());
            enabled = false;
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            errorLogger.error(e, ErrorContext.builder().related("Trying to enable webserver", config.get(WebserverSettings.INTERNAL_IP) + ":" + port).build());
            enabled = false;
        }
    }

    private boolean startHttpsServer() throws BindException {
        String keyStorePath = config.get(WebserverSettings.CERTIFICATE_PATH);

        if ("proxy".equalsIgnoreCase(keyStorePath)) {
            return true;
        }

        try {
            if (!Paths.get(keyStorePath).isAbsolute()) {
                keyStorePath = files.getDataFolder() + File.separator + keyStorePath;
            }
        } catch (InvalidPathException e) {
            logger.error("WebServer: Could not find Keystore: " + e.getMessage());
            errorLogger.error(e, ErrorContext.builder()
                    .whatToDo(e.getMessage() + ", Fix this path to point to a valid keystore file: " + keyStorePath)
                    .related(keyStorePath).build());
        }

        char[] storepass = config.get(WebserverSettings.CERTIFICATE_STOREPASS).toCharArray();
        char[] keypass = config.get(WebserverSettings.CERTIFICATE_KEYPASS).toCharArray();
        String alias = config.get(WebserverSettings.CERTIFICATE_ALIAS);

        boolean startSuccessful = false;
        String keyStoreKind = keyStorePath.endsWith(".p12") ? "PKCS12" : "JKS";
        try (FileInputStream fIn = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance(keyStoreKind);

            keystore.load(fIn, storepass);
            Certificate cert = keystore.getCertificate(alias);

            if (cert == null) {
                throw new IllegalStateException("Alias: '" + alias + "' was not found in file " + keyStorePath + ".");
            }

            logger.info("Certificate: " + cert.getType());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keypass);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);

            server = HttpsServer.create(new InetSocketAddress(config.get(WebserverSettings.INTERNAL_IP), port), 10);
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
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_SSL_CONTEXT));
            errorLogger.error(e, ErrorContext.builder().related(keyStoreKind).build());
        } catch (EOFException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_EMPTY_FILE));
        } catch (FileNotFoundException e) {
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_NO_CERT_FILE, keyStorePath));
            logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_HTTP));
        } catch (BindException e) {
            throw e; // Pass to above error handler
        } catch (IOException e) {
            errorLogger.error(e, ErrorContext.builder().related(config.get(WebserverSettings.INTERNAL_IP) + ":" + port).build());
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            logger.error(locale.getString(PluginLang.WEB_SERVER_FAIL_STORE_LOAD));
            errorLogger.error(e, ErrorContext.builder()
                    .whatToDo("Make sure the Certificate settings are correct / You can try remaking the keystore without -passin or -passout parameters.")
                    .related(keyStorePath).build());
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
        return isUsingHTTPS() && config.isFalse(WebserverSettings.DISABLED_AUTHENTICATION);
    }

    public int getPort() {
        return port;
    }
}
