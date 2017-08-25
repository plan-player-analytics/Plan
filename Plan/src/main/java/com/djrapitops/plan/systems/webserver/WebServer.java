package main.java.com.djrapitops.plan.systems.webserver;

import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.*;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.cache.PageCache;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webapi.WebAPI;
import main.java.com.djrapitops.plan.systems.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.systems.webapi.bukkit.AnalyticsWebAPI;
import main.java.com.djrapitops.plan.systems.webapi.bukkit.AnalyzeWebAPI;
import main.java.com.djrapitops.plan.systems.webapi.bukkit.ConfigureWebAPI;
import main.java.com.djrapitops.plan.systems.webapi.bukkit.InspectWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.response.*;
import main.java.com.djrapitops.plan.systems.webserver.response.api.BadRequestResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.api.JsonResponse;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.ChatColor;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author Rsl1122
 */
public class WebServer {

    private final Plan plugin;
    private InformationManager infoManager;

    private final int port;
    private boolean enabled = false;
    private HttpServer server;

    private boolean usingHttps = false;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public WebServer(Plan plugin) {
        this.plugin = plugin;
        this.port = Settings.WEBSERVER_PORT.getNumber();

        registerWebAPIs();
    }

    public void setInfoManager(InformationManager infoManager) {
        this.infoManager = infoManager;
    }

    private void registerWebAPIs() {
        WebAPIManager.registerNewAPI("analytics", new AnalyticsWebAPI());
        WebAPIManager.registerNewAPI("analyze", new AnalyzeWebAPI());
        WebAPIManager.registerNewAPI("configure", new ConfigureWebAPI());
        WebAPIManager.registerNewAPI("inspect", new InspectWebAPI());
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
                        if (response instanceof CSSResponse) {
                            responseHeaders.set("Content-Type", "text/css");
                        }
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
        byte[] bytes;

        try (InputStream in = exchange.getRequestBody()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            for (int n = in.read(buf); n > 0; n = in.read(buf)) {
                out.write(buf, 0, n);
            }

            bytes = out.toByteArray();
        }

        try {
            return new String(bytes, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            return null;
        }
    }

    private Response getAPIResponse(String target, HttpExchange exchange) throws IOException {
        String[] args = target.split("/");

        if (args.length < 3) {
            String error = "API Method not specified";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        String method = args[2];
        String response = readPOSTRequest(exchange);

        if (response == null) {
            String error = "Error at reading the POST request." +
                    "Note that the Encoding must be ISO-8859-1.";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        Map<String, String> variables = readVariables(response);
        String key = variables.get("key");

        Plan plan = Plan.getInstance();

        if (!checkKey(key)) {
            String error = "Server Key not given or invalid";
            return PageCache.loadPage(error, () -> {
                ForbiddenResponse forbidden = new ForbiddenResponse();
                forbidden.setContent(error);
                return forbidden;
            });
        }

        WebAPI api = WebAPIManager.getAPI(method);

        if (api == null) {
            String error = "API Method not found";
            return PageCache.loadPage(error, () -> new BadRequestResponse(error));
        }

        try {
            return api.onResponse(plan, variables);
        } catch (Exception ex) {
            Log.toLog("WebServer.getAPIResponse", ex);
            return new InternalErrorResponse(ex, "An error while processing the request happened");
        }
    }

    private boolean checkKey(String key) {
        UUID uuid = Plan.getServerUUID();
        UUID keyUUID;
        try {
            keyUUID = UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return uuid.equals(keyUUID);
    }

    private Map<String, String> readVariables(String response) {
        String[] variables = response.split("&");

        return Arrays.stream(variables)
                .map(variable -> variable.split("=", 2))
                .filter(splittedVariables -> splittedVariables.length == 2)
                .collect(Collectors.toMap(splittedVariables -> splittedVariables[0], splittedVariables -> splittedVariables[1], (a, b) -> b));
    }

    private Response getResponse(String target, WebUser user) {
        if ("/favicon.ico".equals(target)) {
            return PageCache.loadPage("Redirect: favicon", () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
        }

        if (usingHttps) {
            if (user == null) {
                return PageCache.loadPage("promptAuthorization", PromptAuthorizationResponse::new);
            }

            int permLevel = user.getPermLevel(); // Lower number has higher clearance.
            int required = getRequiredPermLevel(target, user.getName());
            if (permLevel > required) {
                return forbiddenResponse(permLevel, required);
            }
        }

        boolean javaScriptRequest = target.endsWith(".js");
        boolean cssRequest = target.endsWith(".css");

        String[] args = target.split("/");
        if (args.length < 2) {
            return rootPageResponse(user);
        }

        if (javaScriptRequest) {
            return getJSResponse(args[args.length - 1]);
        }
        if (cssRequest) {
            try {
                return new CSSResponse("main.css");
            } catch (Exception e) {
                return new InternalErrorResponse(e, target);
            }
        }

        String page = args[1];
        switch (page) {
            case "players":
                return PageCache.loadPage("players", () -> new PlayersPageResponse(plugin));
            case "player":
                return playerResponse(args);
            case "server":
                return serverResponse();
            default:
                return notFoundResponse();
        }
    }

    private Response getJSResponse(String fileName) {
        try {
            return new JavaScriptResponse(fileName);
        } catch (Exception e) {
            return new InternalErrorResponse(e, fileName);
        }
    }

    private Response forbiddenResponse(int permLevel, int required) {
        return PageCache.loadPage("forbidden", () -> {
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
                return PageCache.loadPage("players", () -> new PlayersPageResponse(plugin));
            case 2:
                return playerResponse(new String[]{"", "", user.getName()});
            default:
                return forbiddenResponse(user.getPermLevel(), 0);
        }
    }

    private Response serverResponse() {
        if (!infoManager.isAnalysisCached()) {
            String error = "Analysis Data was not cached.<br>Use /plan analyze to cache the Data.";
            PageCache.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        return PageCache.loadPage("analysisPage", () -> new AnalysisPageResponse(infoManager));
    }

    private Response playerResponse(String[] args) {
        if (args.length < 3) {
            return PageCache.loadPage("notFound", NotFoundResponse::new);
        }

        String playerName = args[2].trim();
        UUID uuid = UUIDUtility.getUUIDOf(playerName);

        if (uuid == null) {
            String error = "Player has no UUID";
            return PageCache.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        if (!infoManager.isCached(uuid)) {
            String error = "Player's data was not cached.<br>Use /plan inspect " + playerName + " to cache the Data.";
            return PageCache.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        return PageCache.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(infoManager, uuid));
    }

    private Response notFoundResponse() {
        String error = "<h1>404 Not Found</h1>"
                + "<p>Make sure you're accessing a link given by a command, Examples:</p>"
                + "<p>" + getProtocol() + HtmlUtils.getInspectUrl("<player>") + " or<br>"
                + getProtocol() + HtmlUtils.getServerAnalysisUrl() + "</p>";

        return PageCache.loadPage("notFound: " + error, () -> {
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

    public boolean isUsingHTTPS() {
        return usingHttps;
    }

    public boolean isAuthRequired() {
        return usingHttps;
    }

    public String getAccessAddress() {
        return getProtocol()+":/"+ HtmlUtils.getIP();
    }
}
