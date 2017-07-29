package main.java.com.djrapitops.plan.ui.webserver;

import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.*;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.*;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Executors;

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
            server.createContext("/", serverResponse(null));
            HttpContext analysisPage = server.createContext("/server", serverResponse(null));
            HttpContext playersPage = server.createContext("/players", new PlayersPageResponse(null, plugin));
            HttpContext inspectPage = server.createContext("/player", new InspectPageResponse(null, dataReqHandler, UUID.randomUUID())); // TODO

            if (startSuccessful) {
                for (HttpContext c : new HttpContext[]{analysisPage, playersPage, inspectPage}) {
                    c.setAuthenticator(new Authenticator(plugin, c.getPath()));
                }
            }

            server.setExecutor(Executors.newSingleThreadExecutor());

            server.start();
            enabled = true;

            Log.info(Phrase.WEBSERVER_RUNNING.parse(String.valueOf(server.getAddress().getPort())));
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            enabled = false;
        }
    }

    // Used for deciding the Response appropriate for the Request.
    private Response getResponse(Request request, OutputStream output) {
        try {
            Verify.nullCheck(request);
            Verify.nullCheck(output);

            if (isFaviconRequest(request)) {
                return new RedirectResponse(output, "https://puu.sh/tK0KL/6aa2ba141b.ico");
            }

            if (!request.hasAuthorization()) {
                return new PromptAuthorizationResponse(output);
            }
            try {
                if (!isAuthorized(request)) {
                    ForbiddenResponse response403 = new ForbiddenResponse(output);
                    String content = "<h1>403 Forbidden - Access Denied</h1>"
                            + "<p>Unauthorized User.<br>"
                            + "Make sure your user has the correct access level.<br>"
                            + "You can use /plan web check <username> to check the permission level.</p>";
                    response403.setContent(content);
                    return response403;
                }
            } catch (IllegalArgumentException e) {
                return new PromptAuthorizationResponse(output);
            }
            String req = request.getRequest();
            String target = request.getTarget();
            if (!req.equals("GET") || target.equals("/")) {
                return responseNotFound(output);
            }
            String[] args = target.split("/");
            if (args.length < 2) {
                return responseNotFound(output);
            }
            String page = args[1];
            switch (page) {
                case "players":
                    return new PlayersPageResponse(output, plugin);
                case "player":
                    return playerResponse(args, output);
                case "server":
                    return serverResponse(output);
                default:
                    return responseNotFound(output);
            }
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return new InternalErrorResponse(output, e, request.getTarget());
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

    private boolean isAuthorized(Request request) throws PassEncryptUtil.CannotPerformOperationException, PassEncryptUtil.InvalidHashException, SQLException {
        Base64.Decoder decoder = Base64.getDecoder();
        String auth = request.getAuthorization();
        byte[] decoded = decoder.decode(auth);
        String[] userInfo = new String(decoded).split(":");
        if (userInfo.length != 2) {
            throw new IllegalArgumentException("User and Password not specified");
        }
        String user = userInfo[0];
        String passwordRaw = userInfo[1];
        return isAuthorized(user, passwordRaw, request.getTarget());
    }

    private boolean isAuthorized(String user, String passwordRaw, String target) throws PassEncryptUtil.CannotPerformOperationException, PassEncryptUtil.InvalidHashException, SQLException {

        SecurityTable securityTable = plugin.getDB().getSecurityTable();
        if (!securityTable.userExists(user)) {
            throw new IllegalArgumentException("User Doesn't exist");
        }
        WebUser securityInfo = securityTable.getSecurityInfo(user);

        boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, securityInfo.getSaltedPassHash());
        if (!correctPass) {
            throw new IllegalArgumentException("User and Password do not match");
        }
        int permLevel = securityInfo.getPermLevel(); // Lower number has higher clearance.
        int required = getRequiredPermLevel(target, securityInfo.getName());
        return permLevel <= required;
    }

    private int getRequiredPermLevel(String target, String user) {
        String[] t = target.split("/");
        if (t.length < 3) {
            return 0;
        }
        final String wantedUser = t[2].toLowerCase().trim();
        final String theUser = user.trim().toLowerCase();
        if (t[1].equals("players")) {
            return 1;
        }
        if (t[1].equals("player")) {
            if (wantedUser.equals(theUser)) {
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }

    private boolean isFaviconRequest(Request request) {
        String[] args = request.getTarget().split("/");
        if (args.length < 2 || args.length > 2) {
            return false;
        }
        String page = args[1];
        return page.equals("favicon.ico");
    }
}
