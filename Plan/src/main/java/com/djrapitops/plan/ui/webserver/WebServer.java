package main.java.com.djrapitops.plan.ui.webserver;

import com.sun.net.httpserver.*;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.*;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Rsl1122
 */
public class WebServer {

    private final Plan plugin;
    private final DataRequestHandler dataReqHandler;
    private boolean enabled = false;
    private HttpServer server;
    private final int port;
    private boolean shutdown;

    /**
     * Class Constructor.
     * <p>
     * Initializes DataRequestHandler
     *
     * @param plugin Current instance of Plan
     */
    public WebServer(Plan plugin) {
        this.plugin = plugin;
        this.port = Settings.WEBSERVER_PORT.getNumber();
        shutdown = false;
        dataReqHandler = new DataRequestHandler(plugin);
    }

    /**
     * Starts up the Webserver in a Asynchronous thread.
     */
    public void initServer() {
        //Server is already enabled stop code
        if (enabled) {
            return;
        }

        Log.info(Phrase.WEBSERVER_INIT.toString());
        try {
            String keyStorePath = Settings.WEBSERVER_CERTIFICATE_PATH.toString();
            if (!keyStorePath.contains(":")) {
                keyStorePath = plugin.getDataFolder() + keyStorePath;
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
                Log.error("!--------!---------!---------!");
                Log.error("WebServer: SSL Certificate KeyStore File not Found: " + keyStorePath);
                Log.error("!--------!---------!---------!");
            } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
                Log.error("WebServer: SSL Certificate loading Failed.");
                Log.toLog(this.getClass().getName(), e);
            }

            Log.debug("Start Successful: " + startSuccessful);

            if (!startSuccessful) {
                return; // TODO Http Server
            }

            Log.debug("Create server context");
            HttpContext context = server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange xghng) throws IOException {
                    HttpsExchange exchange = (HttpsExchange) xghng;
                    try {
                        URI uri = exchange.getRequestURI();
                        String target = uri.toString();
                        Response response = getResponse(target);
                        String content = response.getContent();
                        exchange.sendResponseHeaders(response.getCode(), content.length());

                        OutputStream os = exchange.getResponseBody();
                        os.write(content.getBytes());
                        os.close();
                    } catch (Exception e) {
                        Log.toLog(this.getClass().getName(), e);
                        throw e;
                    }
                }
            });

            if (startSuccessful) {
                context.setAuthenticator(new Authenticator(plugin, "/"));
            }

            server.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));

            server.start();
            enabled = true;

            Log.info(Phrase.WEBSERVER_RUNNING.parse(String.valueOf(server.getAddress().getPort())));
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            enabled = false;
        }
    }

//    if (!request.hasAuthorization()) {
//        return new PromptAuthorizationResponse(output);
//    }
//            try {
//        if (!isAuthorized(request)) {
//            ForbiddenResponse response403 = new ForbiddenResponse(output);
//            String content = "<h1>403 Forbidden - Access Denied</h1>"
//                    + "<p>Unauthorized User.<br>"
//                    + "Make sure your user has the correct access level.<br>"
//                    + "You can use /plan web check <username> to check the permission level.</p>";
//            response403.setContent(content);
//            return response403;
//        }

    private Response getResponse(String target) {
        String[] args = target.split("/");
        if (args.length < 2) {
            return responseNotFound(null);
        }
        String page = args[1];
        switch (page) {
            case "favicon.ico":
                return new RedirectResponse(null, "https://puu.sh/tK0KL/6aa2ba141b.ico");
            case "players":
                return new PlayersPageResponse(null, plugin);
            case "player":
                return playerResponse(args, null);
            case "server":
                return serverResponse(null);
            default:
                return responseNotFound(null);
        }
    }

    private Response serverResponse(OutputStream output) {
        if (!dataReqHandler.checkIfAnalysisIsCached()) {
            return new NotFoundResponse(output, "Analysis data was not cached.");
        }
        return new AnalysisPageResponse(output, dataReqHandler);
    }

    private Response playerResponse(String[] args, OutputStream output) {
        if (args.length < 3) {
            return new NotFoundResponse(output);
        }
        String playerName = args[2].trim();
        UUID uuid = UUIDUtility.getUUIDOf(playerName);
        if (uuid == null) {
            return new NotFoundResponse(output, "Player has no UUID");
        }
        if (!dataReqHandler.checkIfCached(uuid)) {
            return new NotFoundResponse(output, "Player's data was not cached.");
        }
        return new InspectPageResponse(output, dataReqHandler, uuid);
    }

    private Response responseNotFound(OutputStream output) {
        NotFoundResponse response404 = new NotFoundResponse(output);
        String content = "<h1>404 Not Found</h1>"
                + "<p>Make sure you're accessing a link given by a command, Examples:</p>"
                + "<p>" + HtmlUtils.getInspectUrl("<player>") + " or<br>"
                + HtmlUtils.getServerAnalysisUrl() + "</p>";
        response404.setContent(content);
        return response404;
    }

    /**
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shuts down the server - Async thread is closed with shutdown boolean.
     */
    public void stop() {
        Log.info(Phrase.WEBSERVER_CLOSE.toString());
        shutdown = true;
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * @return DataRequestHandler used by the WebServer.
     */
    public DataRequestHandler getDataReqHandler() {
        return dataReqHandler;
    }
}
