package main.java.com.djrapitops.plan.ui.webserver;

import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.*;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.*;
import main.java.com.djrapitops.plan.ui.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.ui.webserver.response.api.JsonResponse;
import main.java.com.djrapitops.plan.ui.webserver.response.api.SuccessResponse;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

/**
 * @author Rsl1122
 */
public class WebServer {

    private final Plan plugin;
    private final DataRequestHandler dataReqHandler;
    private final int port;
    private boolean enabled = false;
    private HttpServer server;

    private boolean usingHttps = false;

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

        Log.info(Locale.get(Msg.ENABLE_WEBSERVER).toString());
        try {
            usingHttps = startHttpsServer();

            Log.debug(usingHttps ? "Https Start Successful." : "Https Start Failed.");

            if (!usingHttps) {
                Log.infoColor(ChatColor.YELLOW + "User Authorization Disabled! (Not possible over http)");
                server = HttpServer.create(new InetSocketAddress(port), 10);
            }

            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    try {
                        Headers responseHeaders = exchange.getResponseHeaders();
                        URI uri = exchange.getRequestURI();
                        String target = uri.toString();

                        boolean apiRequest = "POST".equals(exchange.getRequestMethod());
                        Response response = null;

                        String type = "text/html;";

                        if (apiRequest) {
                            response = getAPIResponse(target, exchange);

                            if (response instanceof JsonResponse) {
                                type = "application/json;";
                            }
                        }

                        responseHeaders.set("Content-Type", type);

                        if (apiRequest) {
                            sendData(responseHeaders, exchange, response);
                            return;
                        }

                        WebUser user = null;

                        if (usingHttps) {
                            user = getUser(exchange.getRequestHeaders());

                            // Prompt authorization
                            if (user == null) {
                                responseHeaders.set("WWW-Authenticate", "Basic realm=\"/\";");
                            }
                        }

                        response = getResponse(target, user);

                        sendData(responseHeaders, exchange, response);
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

            Log.info(Locale.get(Msg.ENABLE_WEBSERVER_INFO).parse(server.getAddress().getPort()));
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            enabled = false;
        }
    }

    private void sendData(Headers header, HttpExchange exchange, Response response) throws IOException {
        header.set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(response.getCode(), 0);

        try (GZIPOutputStream out = new GZIPOutputStream(exchange.getResponseBody());
             ByteArrayInputStream bis = new ByteArrayInputStream(response.getContent().getBytes())) {
            byte[] buffer = new byte[2048];
            int count;
            while ((count = bis.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }

    private WebUser getUser(Headers requestHeaders) {
        Benchmark.start("getUser");
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

            WebUser webUser = securityTable.getWebUser(user);

            boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, webUser.getSaltedPassHash());
            if (!correctPass) {
                throw new IllegalArgumentException("User and Password do not match");
            }

            Benchmark.stop("getUser: " + requestHeaders);
            return webUser;
        } catch (IllegalArgumentException e) {
            Log.debug("WebServer: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return null;
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
            Log.infoColor(ChatColor.YELLOW + "WebServer: SSL Certificate KeyStore File not Found: " + keyStorePath);
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

    private String readPOSTRequest(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte buf[] = new byte[4096];
            for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), "ISO-8859-1");
        }
    }

    private Response getAPIResponse(String target, HttpExchange exchange) throws IOException {
        String[] args = target.split("/");

        if (args.length < 3) {
            String error = "API Method not specified";
            return PageCacheHandler.loadPage(error, () -> new NotFoundResponse(error));
        }

        String method = args[2];
        String response = readPOSTRequest(exchange);
        Map<String, String> variables = readVariables(response);

        //TODO ADD CHECK IF SERVER KEY VALID

        Plan plan = Plan.getInstance();

        String playerString;
        UUID uuid;
        String identifier;

        switch (method) {
            //TODO Add Bungee APIs
            case "analyze":
                plan.getAnalysisCache().updateCache();
                return PageCacheHandler.loadPage("success", SuccessResponse::new);
            case "inspect":
                playerString = variables.get("player");

                if (playerString == null) {
                    String error = "Player String not included";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                uuid = UUIDUtility.getUUIDOf(playerString);

                if (uuid == null) {
                    String error = "UUID not found";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                Plan.getInstance().getInspectCache().cache(uuid);

                return PageCacheHandler.loadPage("success", SuccessResponse::new);
            case "analysis":
            case "analytics":
                identifier = "analysisJson";
                if (!PageCacheHandler.isCached(identifier)) {
                    return PageCacheHandler.loadPage("No Analysis Data", () -> new BadRequestResponse("No analysis data available"));
                }

                return PageCacheHandler.loadPage(identifier);
            case "inspection":
                playerString = variables.get("player");

                if (playerString == null) {
                    String error = "Player String not included";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                uuid = UUIDUtility.getUUIDOf(playerString);

                if (uuid == null) {
                    String error = "UUID not found";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                UserData userData = plan.getInspectCache().getFromCache(uuid);

                if (userData == null) {
                    String error = "User not cached";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                return PageCacheHandler.loadPage("inspectionJson: " + uuid, () -> new JsonResponse(plan.getInspectCache().getFromCache(uuid)));
            case "configure":
                String key = variables.get("configKey");

                if (key == null) {
                    String error = "Config Key null";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                String value = variables.get("configValue");

                if (value == null) {
                    String error = "Config Value null";
                    return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
                }

                if (value.equals("null")) {
                    value = null;
                }

                FileConfiguration config = plan.getConfig();
                config.set(key, value);
                plan.saveConfig();

                return PageCacheHandler.loadPage("success", SuccessResponse::new);
            default:
                String error = "API Method not found";
                return PageCacheHandler.loadPage(error, () -> new BadRequestResponse(error));
        }
    }

    private Map<String, String> readVariables(String response) {
        Map<String, String> variableMap = new HashMap<>();
        String[] variables = response.split("&");

        for (String variable : variables) {
            String[] splittedVariables = variable.split("=", 2);
            if (splittedVariables.length != 2) {
                continue;
            }

            variableMap.put(splittedVariables[0], splittedVariables[1]);
        }

        return variableMap;
    }

    private Response getResponse(String target, WebUser user) {
        if ("/favicon.ico".equals(target)) {
            return PageCacheHandler.loadPage("Redirect: favicon", () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
        }

        if (usingHttps) {
            if (user == null) {
                return PageCacheHandler.loadPage("promptAuthorization", PromptAuthorizationResponse::new);
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
                return PageCacheHandler.loadPage("players", () -> new PlayersPageResponse(plugin));
            case "player":
                return playerResponse(args);
            case "server":
                return serverResponse();
            default:
                return notFoundResponse();
        }
    }

    private Response forbiddenResponse(int permLevel, int required) {
        return PageCacheHandler.loadPage("forbidden", () -> {
            ForbiddenResponse response403 = new ForbiddenResponse();
            String content = "<h1>403 Forbidden - Access Denied</h1>"
                    + "<p>Unauthorized User.<br>"
                    + "Make sure your user has the correct access level.<br>"
                    + "This page requires permission level of " + required + ",<br>"
                    + "This user has permission level of " + permLevel + "</p>";
            response403.setContent(content);
            return response403;
        });
    }

    private Response rootPageResponse(WebUser user) {
        if (user == null) {
            return notFoundResponse();
        }

        switch (user.getPermLevel()) {
            case 0:
                return serverResponse();
            case 1:
                return PageCacheHandler.loadPage("players", () -> new PlayersPageResponse(plugin));
            case 2:
                return playerResponse(new String[]{"", "", user.getName()});
            default:
                return forbiddenResponse(user.getPermLevel(), 0);
        }
    }

    private Response serverResponse() {
        if (!dataReqHandler.checkIfAnalysisIsCached()) {
            String error = "Analysis Data was not cached.<br>Use /plan analyze to cache the Data.";
            PageCacheHandler.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        return PageCacheHandler.loadPage("analysisPage", () -> new AnalysisPageResponse(dataReqHandler));
    }

    private Response playerResponse(String[] args) {
        if (args.length < 3) {
            return PageCacheHandler.loadPage("notFound", NotFoundResponse::new);
        }

        String playerName = args[2].trim();
        UUID uuid = UUIDUtility.getUUIDOf(playerName);

        if (uuid == null) {
            String error = "Player has no UUID";
            return PageCacheHandler.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        if (!dataReqHandler.checkIfCached(uuid)) {
            String error = "Player's data was not cached.<br>Use /plan inspect " + playerName + " to cache the Data.";
            return PageCacheHandler.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        return PageCacheHandler.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(dataReqHandler, uuid));
    }

    private Response notFoundResponse() {
        String error = "<h1>404 Not Found</h1>"
                + "<p>Make sure you're accessing a link given by a command, Examples:</p>"
                + "<p>" + getProtocol() + HtmlUtils.getInspectUrl("<player>") + " or<br>"
                + getProtocol() + HtmlUtils.getServerAnalysisUrl() + "</p>";

        return PageCacheHandler.loadPage("notFound: " + error, () -> {
            Response response404 = new NotFoundResponse();
            response404.setContent(error);
            return response404;
        });
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

                return wantedUser.equals(theUser) ? 2 : 1;
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
