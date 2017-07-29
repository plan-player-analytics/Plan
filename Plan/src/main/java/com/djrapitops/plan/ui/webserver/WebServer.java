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
import org.bukkit.ChatColor;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.List;
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

    private boolean usingHttps;

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
            usingHttps = startHttpsServer();

            Log.debug(usingHttps ? "Https Start Successful." : "Https Start Failed.");

            if (!usingHttps) {
                server = HttpServer.create(new InetSocketAddress(port), 10);
            }

            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    try {
                        URI uri = exchange.getRequestURI();
                        String target = uri.toString();
                        Headers responseHeaders = exchange.getResponseHeaders();
                        responseHeaders.set("Content-Type", "text/html;");
                        WebUser user = null;
                        if (usingHttps) {
                            user = getUser(exchange.getRequestHeaders());

                            // Prompt authorization
                            if (user == null) {

                                responseHeaders.set("WWW-Authenticate", "Basic realm=\"/\";");
                            }
                        }
                        Response response = getResponse(target, user);

                        String content = response.getContent();
                        exchange.sendResponseHeaders(response.getCode(), content.length());
                        try (BufferedOutputStream out = new BufferedOutputStream(exchange.getResponseBody())) {
                            try (ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes())) {
                                byte[] buffer = new byte[2048];
                                int count;
                                while ((count = bis.read(buffer)) != -1) {
                                    out.write(buffer, 0, count);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.toLog(this.getClass().getName(), e);
                        throw e;
                    } finally {
                        exchange.close();
                    }
                }
            });

            server.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
            server.start();

            enabled = true;

            Log.info(Phrase.WEBSERVER_RUNNING.parse(String.valueOf(server.getAddress().getPort())));
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            enabled = false;
        }
    }

    private WebUser getUser(Headers requestHeaders) {
        try {
            List<String> authorization = requestHeaders.get("Authorization");
            if (Verify.isEmpty(authorization)) {
                return null;
            }
            String auth = authorization.get(0);
            if (auth.contains("Basic ")) {
                auth = auth.split(" ")[1];
            } else {
                throw new IllegalArgumentException("Wrong format of Auth");
            }
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decoded = decoder.decode(auth);
            String[] userInfo = new String(decoded).split(":");
            if (userInfo.length != 2) {
                throw new IllegalArgumentException("User and Password not specified");
            }
            String user = userInfo[0];
            String passwordRaw = userInfo[1];

            SecurityTable securityTable = plugin.getDB().getSecurityTable();
            if (!securityTable.userExists(user)) {
                throw new IllegalArgumentException("User Doesn't exist");
            }

            WebUser webUser = securityTable.getSecurityInfo(user);

            boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, webUser.getSaltedPassHash());
            if (!correctPass) {
                throw new IllegalArgumentException("User and Password do not match");
            }
            return webUser;
        } catch (IllegalArgumentException e) {
            Log.debug("WebServer: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return null;
        }
    }

    private boolean startHttpsServer() throws IOException {
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
            Log.infoColor(ChatColor.YELLOW + "WebServer: SSL Certificate KeyStore File not Found: " + keyStorePath);
            Log.info("No Certificate -> Using Http server for Visualization.");
            Log.infoColor(ChatColor.YELLOW + "User Authorization Disabled! (Not possible over http)");
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException e) {
            Log.error("WebServer: SSL Certificate loading Failed.");
            Log.toLog(this.getClass().getName(), e);
        }
        return startSuccessful;
    }

    private Response getResponse(String target, WebUser user) {
        if ("/favicon.ico".equals(target)) {
            return new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico");
        }
        if (usingHttps) {
            if (user == null) {
                return new PromptAuthorizationResponse();
            }

            int permLevel = user.getPermLevel(); // Lower number has higher clearance.
            int required = getRequiredPermLevel(target, user.getName());
            if (permLevel > required) {
                return forbiddenResponse(permLevel, required);
            }
        }
        String[] args = target.split("/");
        if (args.length < 2) {
            return rootPageResponse(user);
        }

        String page = args[1];
        switch (page) {
            case "players":
                return new PlayersPageResponse(plugin);
            case "player":
                return playerResponse(args);
            case "server":
                return serverResponse();
            default:
                return notFoundResponse();
        }
    }

    private ForbiddenResponse forbiddenResponse(int permLevel, int required) {
        ForbiddenResponse response403 = new ForbiddenResponse();
        String content = "<h1>403 Forbidden - Access Denied</h1>"
                + "<p>Unauthorized User.<br>"
                + "Make sure your user has the correct access level.<br>"
                + "This page requires permission level of " + String.valueOf(required) + ",<br>"
                + "This user has permission level of " + String.valueOf(permLevel) + "</p>";
        response403.setContent(content);
        return response403;
    }

    private Response rootPageResponse(WebUser user) {
        if (user == null) {
            return notFoundResponse();
        }
        switch (user.getPermLevel()) {
            case 0:
                return serverResponse();
            case 1:
                return new PlayersPageResponse(plugin);
            case 2:
                return playerResponse(new String[]{"", "", user.getName()});
            default:
                return forbiddenResponse(user.getPermLevel(), 0);
        }
    }

    private Response serverResponse() {
        if (!dataReqHandler.checkIfAnalysisIsCached()) {
            return new NotFoundResponse("Analysis Data was not cached.<br>Use /plan analyze to cache the Data.");
        }
        return new AnalysisPageResponse(dataReqHandler);
    }

    private Response playerResponse(String[] args) {
        if (args.length < 3) {
            return new NotFoundResponse();
        }
        String playerName = args[2].trim();
        UUID uuid = UUIDUtility.getUUIDOf(playerName);
        if (uuid == null) {
            return new NotFoundResponse("Player has no UUID");
        }
        if (!dataReqHandler.checkIfCached(uuid)) {
            return new NotFoundResponse("Player's data was not cached.<br>Use /plan inspect " + playerName + " to cache the Data.");
        }
        return new InspectPageResponse(dataReqHandler, uuid);
    }

    private Response notFoundResponse() {
        NotFoundResponse response404 = new NotFoundResponse();
        String content = "<h1>404 Not Found</h1>"
                + "<p>Make sure you're accessing a link given by a command, Examples:</p>"
                + "<p>" + getProtocol() + HtmlUtils.getInspectUrl("<player>") + " or<br>"
                + getProtocol() + HtmlUtils.getServerAnalysisUrl() + "</p>";
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
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Used to get the handler for Html content requests.
     *
     * @return DataRequestHandler used by the WebServer.
     */
    public DataRequestHandler getDataReqHandler() {
        return dataReqHandler;
    }

    private int getRequiredPermLevel(String target, String user) {
        String[] t = target.split("/");
        if (t.length < 2) {
            return 100;
        }
        if (t.length > 3) {
            return 0;
        }
        String page = t[1];
        switch (page) {
            case "players":
                return 1;
            case "player":
                // /player/ - 404 for perm lvl 1
                if (t.length < 3) {
                    return 1;
                }
                final String wantedUser = t[2].toLowerCase().trim();
                final String theUser = user.trim().toLowerCase();
                if (wantedUser.equals(theUser)) {
                    return 2;
                } else {
                    return 1;
                }
            default:
                return 0;
        }
    }

    public String getProtocol() {
        return usingHttps ? "https" : "http";
    }

    public boolean usingHttps() {
        return usingHttps;
    }

    public boolean isAuthRequired() {
        return usingHttps;
    }
}
