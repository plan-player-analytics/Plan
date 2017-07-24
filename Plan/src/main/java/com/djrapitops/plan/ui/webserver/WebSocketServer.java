package main.java.com.djrapitops.plan.ui.webserver;

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.ui.html.DataRequestHandler;
import main.java.com.djrapitops.plan.ui.webserver.response.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Base64;
import java.util.UUID;

/**
 *
 * @author Rsl1122
 */
public class WebSocketServer {

    private final int PORT;
    private boolean enabled = false;
    private Socket sslServer;
    private ServerSocket server;

    private final Plan plugin;
    private final DataRequestHandler dataReqHandler;

    private boolean shutdown;

    /**
     * Class Constructor.
     *
     * Initializes DataRequestHandler
     *
     * @param plugin Current instance of Plan
     */
    public WebSocketServer(Plan plugin) {
        this.plugin = plugin;
        this.PORT = Settings.WEBSERVER_PORT.getNumber();
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
        Log.info(Phrase.WEBSERVER_INIT + "");
        try {
            InetAddress ip = InetAddress.getByName(Settings.WEBSERVER_IP.toString());
//            SSLServerSocketFactory ssl = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
//            server = ssl.createServerSocket(PORT, 1, ip);
            server = new ServerSocket(PORT, 1, ip);

            plugin.getRunnableFactory().createNew(new AbsRunnable("WebServerTask") {
                @Override
                public void run() {
                    while (!shutdown) {
                        /*SSL*/Socket socket = null;
                        InputStream input = null;
                        OutputStream output = null;
                        Request request = null;
                        try {
                            socket = /*(SSLSocket)*/ server.accept();
                            Log.debug("New Socket Connection: " + socket.getInetAddress());
                            input = socket.getInputStream();
                            output = socket.getOutputStream();
                            request = new Request(input);
                            Benchmark.start("Webserver Response");
                            request.parse();
                            Response response = getResponse(request, output);
                            Log.debug("Parsed response: " + response.getClass().getSimpleName());
                            response.sendStaticResource();
                        } catch (IOException | IllegalArgumentException e) {
                        } finally {
                            Benchmark.stop("Webserver Response");
                            MiscUtils.close(input, request, output, socket);
                        }
                    }
                    this.cancel();
                }
            }).runTaskAsynchronously();

            enabled = true;

            Log.info(Phrase.WEBSERVER_RUNNING.parse(server.getLocalPort() + ""));
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

//            if (!request.hasAuthorization()) {
//                return new PromptAuthorizationResponse(output);
//            }
//            try {
//                if (!isAuthorized(request)) {
//                    ForbiddenResponse response403 = new ForbiddenResponse(output);
//                    String content = "<h1>403 Forbidden - Access Denied</h1>"
//                            + "<p>Unauthorized User.<br>"
//                            + "Make sure your user has the correct access level.<br>"
//                            + "You can use /plan web check <username> to check the permission level.</p>";
//                    response403.setContent(content);
//                    return response403;
//                }
//            } catch (IllegalArgumentException e) {
//                return new PromptAuthorizationResponse(output);
//            }
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
     *
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Shuts down the server - Async thread is closed with shutdown boolean.
     */
    public void stop() {
        Log.info(Phrase.WEBSERVER_CLOSE + "");
        shutdown = true;
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * @return DataRequestHandler used by the WebServer.
     */
    public DataRequestHandler getDataReqHandler() {
        return dataReqHandler;
    }

    private boolean isAuthorized(Request request) throws IllegalArgumentException, PassEncryptUtil.CannotPerformOperationException, PassEncryptUtil.InvalidHashException, SQLException {
        Base64.Decoder decoder = Base64.getDecoder();
        String auth = request.getAuthorization();
        byte[] decoded = decoder.decode(auth);
        String[] userInfo = new String(decoded).split(":");
        if (userInfo.length != 2) {
            throw new IllegalArgumentException("User and Password not specified");
        }
        String user = userInfo[0];
        SecurityTable securityTable = plugin.getDB().getSecurityTable();
        if (!securityTable.userExists(user)) {
            throw new IllegalArgumentException("User Doesn't exist");
        }
        WebUser securityInfo = securityTable.getSecurityInfo(user);
        String passwordRaw = userInfo[1];
        boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, securityInfo.getSaltedPassHash());
        if (!correctPass) {
            throw new IllegalArgumentException("User and Password do not match");
        }
        int permLevel = securityInfo.getPermLevel(); // Lower number has higher clearance.
        int required = getRequiredPermLevel(request, securityInfo.getName());
        return permLevel <= required;
    }

    private int getRequiredPermLevel(Request request, String user) {
        String target = request.getTarget();
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
