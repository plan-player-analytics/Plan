package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
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

    @Override
    public void enable() {
        this.port = Settings.WEBSERVER_PORT.getNumber();

        PlanPlugin plugin = PlanPlugin.getInstance();
        StaticHolder.saveInstance(APIRequestHandler.class, plugin.getClass());
        StaticHolder.saveInstance(RequestHandler.class, plugin.getClass());
        StaticHolder.saveInstance(ResponseHandler.class, plugin.getClass());
        StaticHolder.saveInstance(APIResponseHandler.class, plugin.getClass());

        initServer();
    }

    @Override
    public void disable() {
        stop();
    }

    // TODO WebAPIPageHandler
//    private void registerWebAPIs() {
//        webAPI.registerNewAPI(
//                new AnalysisReadyWebAPI(),
//                new AnalyzeWebAPI(),
//                new ConfigurationWebAPI(),
//                new InspectWebAPI(),
//                new IsOnlineWebAPI(),
//                new RequestInspectPluginsTabBukkitWebAPI(),
//                new PingWebAPI()
//        );
//
//        webAPI.registerNewAPI(
//                new IsCachedWebAPI(),
//                new PostHtmlWebAPI(),
//                new PostInspectPluginsTabWebAPI(),
//                new PostNetworkPageContentWebAPI(),
//                new PostOriginalBukkitSettingsWebAPI(),
//                new RequestPluginsTabWebAPI(),
//                new RequestSetupWebAPI()
//        );
//    }
    //Log.infoColor("§aWebServer Running in WebAPI-only Mode");

    /**
     * Starts up the WebServer in a new Thread Pool.
     */
    public void initServer() {
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

            server.createContext("/", new RequestHandler(this));

            server.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
            server.start();

            enabled = true;

            Log.info(Locale.get(Msg.ENABLE_WEBSERVER_INFO).parse(server.getAddress().getPort()) + " (" + getAccessAddress() + ")");
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
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
            Log.toLog(this.getClass().getName(), e);
        } catch (FileNotFoundException e) {
            Log.infoColor("§eWebServer: SSL Certificate KeyStore File not Found: " + keyStorePath);
            Log.info("No Certificate -> Using Http server for Visualization.");
        } catch (IOException e) {
            Log.error("WebServer: " + e);
            Log.toLog(this.getClass().getName(), e);
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            Log.error("WebServer: SSL Certificate loading Failed.");
            Log.toLog(this.getClass().getName(), e);
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
        Log.info(Locale.get(Msg.DISABLE_WEBSERVER).toString());
        if (server != null) {
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
}
