package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Rsl1122
 */
public class WebServer implements SubSystem {

    private int port;
    private boolean enabled = false;
    private HttpServer server;

    private boolean usingHttps = false;

    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;

    public static WebServer getInstance() {
        WebServer webServer = WebServerSystem.getInstance().getWebServer();
        Verify.nullCheck(webServer, () -> new IllegalStateException("WebServer was not initialized."));
        return webServer;
    }

    @Override
    public void enable() throws EnableException {
        this.port = Settings.WEBSERVER_PORT.getNumber();

        requestHandler = new RequestHandler(this);
        responseHandler = requestHandler.getResponseHandler();

        PlanPlugin plugin = PlanHelper.getInstance();
        StaticHolder.saveInstance(RequestHandler.class, plugin.getClass());
        StaticHolder.saveInstance(ResponseHandler.class, plugin.getClass());

        initServer();

        if (!isEnabled()) {
            if (Check.isBungeeAvailable()) {
                throw new EnableException("WebServer did not initialize!");
            }
            if (Settings.WEBSERVER_DISABLED.isTrue()) {
                Log.warn("WebServer was not initialized. (WebServer.DisableWebServer: true)");
            } else {
                Log.error("WebServer was not initialized successfully. Is the port (" + Settings.WEBSERVER_PORT.getNumber() + ") in use?");
            }
        }
    }

    @Override
    public void disable() {
        stop();
    }

    /**
     * Starts up the WebServer in a new Thread Pool.
     */
    private void initServer() {
        // Check if Bukkit WebServer has been disabled.
        if (!Check.isBungeeAvailable() && Settings.WEBSERVER_DISABLED.isTrue()) {
            return;
        }

        // Server is already enabled stop code
        if (enabled) {
            return;
        }

        Log.info(Locale.get(Msg.ENABLE_WEBSERVER).toString());
        try {
            usingHttps = startHttpsServer();

            Log.debug(usingHttps ? "Https Start Successful." : "Https Start Failed.");

            if (!usingHttps) {
                Log.infoColor("§eUser Authorization Disabled! (Not possible over http)");
                server = HttpServer.create(new InetSocketAddress(Settings.WEBSERVER_IP.toString(), port), 10);
            }
            server.createContext("/", requestHandler);

            server.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
            server.start();

            enabled = true;

            Log.info(Locale.get(Msg.ENABLE_WEBSERVER_INFO).parse(server.getAddress().getPort()) + " (" + getAccessAddress() + ")");
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass(), e);
            enabled = false;
        }
    }

    private boolean startHttpsServer() {
        String keyStorePath = Settings.WEBSERVER_CERTIFICATE_PATH.toString();
        if (!Paths.get(keyStorePath).isAbsolute()) {
            keyStorePath = FileSystem.getDataFolder() + File.separator + keyStorePath;
        }

        char[] storepass = Settings.WEBSERVER_CERTIFICATE_STOREPASS.toString().toCharArray();
        char[] keypass = Settings.WEBSERVER_CERTIFICATE_KEYPASS.toString().toCharArray();
        String alias = Settings.WEBSERVER_CERTIFICATE_ALIAS.toString();

        boolean startSuccessful = false;
        try (FileInputStream fIn = new FileInputStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance("JKS");

            keystore.load(fIn, storepass);
            Certificate cert = keystore.getCertificate(alias);

            Log.info("Found Certificate: " + cert.getType());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keystore, keypass);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keystore);

            server = HttpsServer.create(new InetSocketAddress(Settings.WEBSERVER_IP.toString(), port), 10);
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
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.error("WebServer: SSL Context Initialization Failed.");
            Log.toLog(this.getClass(), e);
        } catch (FileNotFoundException e) {
            Log.infoColor("§eWebServer: SSL Certificate KeyStore File not Found: " + keyStorePath);
            Log.info("No Certificate -> Using Http server for Visualization.");
        } catch (IOException e) {
            Log.error("WebServer: " + e);
            Log.toLog(this.getClass(), e);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            Log.error("WebServer: SSL Certificate loading Failed.");
            Log.toLog(this.getClass(), e);
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
    public void stop() {
        if (server != null) {
            Log.info(Locale.get(Msg.DISABLE_WEBSERVER).toString());
            server.stop(0);
        }
        enabled = false;
    }

    public String getProtocol() {
        return usingHttps ? "https" : "http";
    }

    public boolean isUsingHTTPS() {
        return usingHttps;
    }

    public boolean isAuthRequired() {
        return usingHttps;
    }

    public String getAccessAddress() {
        return isEnabled() ? getProtocol() + "://" + HtmlUtils.getIP() : Settings.EXTERNAL_WEBSERVER_LINK.toString();
    }

    public RequestHandler getRequestHandler() {
        return requestHandler;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
