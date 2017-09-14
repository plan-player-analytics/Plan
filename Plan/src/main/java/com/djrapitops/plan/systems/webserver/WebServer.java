package main.java.com.djrapitops.plan.systems.webserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.*;
import main.java.com.djrapitops.plan.systems.webserver.webapi.universal.PingWebAPI;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

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
public class WebServer {

    private final IPlan plugin;
    private InformationManager infoManager;
    private final WebAPIManager webAPI;

    private final int port;
    private boolean enabled = false;
    private HttpServer server;

    private boolean usingHttps = false;

    /**
     * @param plugin
     */
    public WebServer(IPlan plugin) {
        this.plugin = plugin;
        this.port = Settings.WEBSERVER_PORT.getNumber();
        webAPI = new WebAPIManager();
        registerWebAPIs();
    }

    public void setInfoManager(InformationManager infoManager) {
        this.infoManager = infoManager;
    }

    private void registerWebAPIs() {
        webAPI.registerNewAPI(new GetAnalysisWebAPI());
        webAPI.registerNewAPI(new AnalyzeWebAPI());
        webAPI.registerNewAPI(new ConfigurationWebAPI());
        webAPI.registerNewAPI(new InspectWebAPI());
        webAPI.registerNewAPI(new OnlinePlayersWebAPI());
        webAPI.registerNewAPI(new PingWebAPI());
    }

    /**
     * Starts up the WebServer in a new Thread Pool.
     */
    public void initServer() {
        //Server is already enabled stop code
        if (enabled) {
            return;
        }

        Log.info(Locale.get(Msg.ENABLE_WEBSERVER).toString());
        try {
            usingHttps = startHttpsServer();

            Log.debug(usingHttps ? "Https Start Successful." : "Https Start Failed.");

            if (!usingHttps) {
                Log.infoColor("§eUser Authorization Disabled! (Not possible over http)");
                server = HttpServer.create(new InetSocketAddress(port), 10);
            }

            server.createContext("/", new RequestHandler(plugin, this));

            server.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
            server.start();

            enabled = true;

            Log.info(Locale.get(Msg.ENABLE_WEBSERVER_INFO).parse(server.getAddress().getPort()));
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            enabled = false;
        }
    }

    private boolean startHttpsServer() {
        String keyStorePath = Settings.WEBSERVER_CERTIFICATE_PATH.toString();
        if (!Paths.get(keyStorePath).isAbsolute()) {
            keyStorePath = plugin.getDataFolder() + File.separator + keyStorePath;
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

            server = HttpsServer.create(new InetSocketAddress(port), 10);
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
        return getProtocol() + ":/" + HtmlUtils.getIP();
    }

    public WebAPIManager getWebAPI() {
        return webAPI;
    }
}
