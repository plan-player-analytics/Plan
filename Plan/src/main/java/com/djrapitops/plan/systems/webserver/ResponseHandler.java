/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.webserver;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.tables.SecurityTable;
import com.djrapitops.plan.systems.webserver.pagecache.PageCache;
import com.djrapitops.plan.systems.webserver.pagecache.PageId;
import com.djrapitops.plan.systems.webserver.response.*;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles choosing of the correct response to a request.
 *
 * @author Rsl1122
 */
public class ResponseHandler extends APIResponseHandler {

    private final PlanPlugin plugin;

    private final boolean usingHttps;

    public ResponseHandler(PlanPlugin plugin, WebServer webServer) {
        super(webServer.getWebAPI());
        this.plugin = plugin;
        this.usingHttps = webServer.isUsingHTTPS();
    }

    public Response getResponse(Request request) {
        String target = request.getTarget();
        String[] args = target.split("/");
        try {
            if ("/favicon.ico".equals(target)) {
                return PageCache.loadPage(PageId.FAVICON_REDIRECT.id(), () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
            }
            if (request.isAPIRequest()) {
                return getAPIResponse(request);
            }
            if (target.endsWith(".css")) {
                return PageCache.loadPage(PageId.CSS.of(target), () -> new CSSResponse(target));
            }

            if (target.endsWith(".js")) {
                return PageCache.loadPage(PageId.JS.of(target), () -> new JavaScriptResponse(target));
            }

            UUID serverUUID = PlanPlugin.getInstance().getServerUuid();

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
            } else if (args.length < 2) {
                return notFoundResponse();
            }

            String page = args[1];
            switch (page) {
                case "debug":
                    return new DebugPageResponse();
                case "players":
                    return PageCache.loadPage(PageId.PLAYERS.id(), PlayersPageResponse::new);
                case "player":
                    return playerResponse(args);
                case "network":
                case "server":
                    if (args.length > 2) {
                        try {
                            Optional<UUID> serverUUIDOptional = plugin.getDB().getServerTable().getServerUUID(args[2].replace("%20", " "));
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
            return PageCache.loadPage(PageId.AUTH_PROMPT.id(), PromptAuthorizationResponse::new);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return new InternalErrorResponse(e, request.getTarget());
        }
    }

    private Response forbiddenResponse(int required, int permLevel) {
        return PageCache.loadPage(PageId.FORBIDDEN.id(), () ->
                new ForbiddenResponse("Unauthorized User.<br>"
                        + "Make sure your user has the correct access level.<br>"
                        + "This page requires permission level of " + required + ",<br>"
                        + "This user has permission level of " + permLevel));
    }

    private boolean isAuthorized(int requiredPermLevel, int permLevel) {
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
                return PageCache.loadPage(PageId.PLAYERS.id(), PlayersPageResponse::new);
            case 2:
                return playerResponse(new String[]{"", "", user.getName()});
            default:
                return forbiddenResponse(user.getPermLevel(), 0);
        }
    }

    private Response serverResponse(UUID serverUUID) {
        return PageCache.loadPage(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(plugin.getInfoManager()));
    }

    private Response playerResponse(String[] args) {
        if (args.length < 3) {
            return PageCache.loadPage(PageId.NOT_FOUND.id(), NotFoundResponse::new);
        }

        String playerName = args[2].trim();
        UUID uuid = UUIDUtility.getUUIDOf(playerName);

        if (uuid == null) {
            String error = "Player has no UUID";
            return PageCache.loadPage(PageId.NOT_FOUND.of(error), () -> new NotFoundResponse(error));
        }

        if (plugin.getDB().wasSeenBefore(uuid)) {
            plugin.getInfoManager().cachePlayer(uuid);
            Response response = PageCache.loadPage(PageId.PLAYER.of(uuid));
            // TODO Create a new method that places NotFoundResponse to PageCache instead.
            if (response == null || response.getContent().contains("No Bukkit Servers were online to process this request")) {
                PageCache.cachePage(PageId.PLAYER.of(uuid), () -> {
                    try {
                        return new InspectPageResponse(plugin.getInfoManager(), uuid);
                    } catch (ParseException e) {
                        return new InternalErrorResponse(e, this.getClass().getName());
                    }
                });
                response = PageCache.loadPage(PageId.PLAYER.of(uuid));
            }
            return response;
        }
        return new NotFoundResponse("Player has not played on this server.");
    }

    private Response notFoundResponse() {
        String error = "404 Not Found";
        return PageCache.loadPage(PageId.NOT_FOUND.of("Wrong Page"), () -> {
                    String url = plugin.getInfoManager().getWebServerAddress();
                    return new NotFoundResponse("Make sure you're accessing a link given by a command, Examples:</p>"
                            + "<p>" + url + "/player/Playername<br>" +
                            url + "/server</p>");
                }
        );
    }


}