/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver;

import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.WebUserAuthException;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.systems.webserver.response.*;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;

import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ResponseHandler extends APIResponseHandler {

    private final IPlan plugin;
    private final WebServer webServer;

    private final boolean usingHttps;

    public ResponseHandler(IPlan plugin, WebServer webServer) {
        super(webServer.getWebAPI());
        this.plugin = plugin;
        this.webServer = webServer;
        this.usingHttps = webServer.isUsingHTTPS();
    }

    public Response getResponse(Request request) {
        String target = request.getTarget();
        String[] args = target.split("/");
        try {
            if ("/favicon.ico".equals(target)) {
                return PageCache.loadPage("Redirect: favicon", () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
            }
            if (request.isAPIRequest()) {
                return getAPIResponse(request);
            }
            if (target.endsWith(".css")) {
                return PageCache.loadPage(target + "css", () -> new CSSResponse("main.css"));
            }

            if (target.endsWith(".js")) {
                String fileName = args[args.length - 1];
                return PageCache.loadPage(target + "js", () -> new JavaScriptResponse(fileName));
            }

            UUID serverUUID = MiscUtils.getIPlan().getServerInfoManager().getServerUUID();

            if (usingHttps) {
                if (!request.hasAuth()) {
                    throw new WebUserAuthException("No Authorization");
                }

                WebUser user = getUser(request.getAuth());
                int required = getRequiredPermLevel(target, user.getName());
                int permLevel = user.getPermLevel();

                if (!isAuthorized(required, permLevel)) {
                    return forbiddenResponse(required, permLevel);
                }
                if (args.length < 2) {
                    return rootPageResponse(user, serverUUID);
                }
            }

            String page = args[1];
            switch (page) {
                case "players":
                    return PageCache.loadPage("players", PlayersPageResponse::new);
                case "player":
                    return playerResponse(args);
                case "network":
                case "server":
                    if (args.length > 2) {
                        try {
                            Optional<UUID> serverUUIDOptional = plugin.getDB().getServerTable().getServerUUID(args[2]);
                            if (serverUUIDOptional.isPresent()) {
                                serverUUID = serverUUIDOptional.get();
                            }
                        } catch (IllegalArgumentException ignore) {
                            /*ignored*/
                        }
                    }
                    return serverResponse(serverUUID);
                default:
                    return notFoundResponse();
            }

        } catch (WebUserAuthException e) {
            return PageCache.loadPage("promptAuthorization", PromptAuthorizationResponse::new);
        } catch (Exception e) {
            return new InternalErrorResponse(e, request.getTarget());
        }
    }

    private Response forbiddenResponse(int required, int permLevel) {
        return PageCache.loadPage("forbidden", () ->
                new ForbiddenResponse("Unauthorized User.<br>"
                        + "Make sure your user has the correct access level.<br>"
                        + "This page requires permission level of " + required + ",<br>"
                        + "This user has permission level of " + permLevel));
    }

    private boolean isAuthorized(int requiredPermLevel, int permLevel) throws Exception {
        return permLevel <= requiredPermLevel;
    }

    private WebUser getUser(String auth) throws SQLException, PassEncryptUtil.InvalidHashException, PassEncryptUtil.CannotPerformOperationException, WebUserAuthException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decoded = decoder.decode(auth);
        String[] userInfo = new String(decoded).split(":");
        if (userInfo.length != 2) {
            throw new WebUserAuthException("User and Password not specified");
        }

        String user = userInfo[0];
        String passwordRaw = userInfo[1];

        SecurityTable securityTable = plugin.getDB().getSecurityTable();
        if (!securityTable.userExists(user)) {
            throw new WebUserAuthException("User Doesn't exist");
        }

        WebUser webUser = securityTable.getWebUser(user);

        boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, webUser.getSaltedPassHash());
        if (!correctPass) {
            throw new WebUserAuthException("User and Password do not match");
        }
        return webUser;
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

    private Response rootPageResponse(WebUser user, UUID serverUUID) {
        if (user == null) {
            return notFoundResponse();
        }

        switch (user.getPermLevel()) {
            case 0:
                return serverResponse(serverUUID);
            case 1:
                return PageCache.loadPage("players", PlayersPageResponse::new);
            case 2:
                return playerResponse(new String[]{"", "", user.getName()});
            default:
                return forbiddenResponse(user.getPermLevel(), 0);
        }
    }

    private Response serverResponse(UUID serverUUID) {
        if (!plugin.getInfoManager().isAnalysisCached(serverUUID)) {
            String error = "Analysis Data was not cached.<br>Use /plan analyze to cache the Data.";
            PageCache.loadPage("notFound: " + error, () -> new NotFoundResponse(error));
        }

        return PageCache.loadPage("analysisPage", () -> new AnalysisPageResponse(plugin.getInfoManager()));
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

        plugin.getInfoManager().cachePlayer(uuid);
        return PageCache.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(plugin.getInfoManager(), uuid));
    }

    private Response notFoundResponse() {
        String error = "404 Not Found";
        return PageCache.loadPage("notFound: " + error, () ->
                new NotFoundResponse("Make sure you're accessing a link given by a command, Examples:</p>"
                        + "<p>" + webServer.getProtocol() + ":" + HtmlUtils.getInspectUrl("<player>") + " or<br>"
                        + webServer.getProtocol() + ":" + HtmlUtils.getServerAnalysisUrl())
        );
    }


}