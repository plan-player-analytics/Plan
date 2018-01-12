/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.database.tables.SecurityTable;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.auth.FailReason;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plan.system.webserver.pagecache.PageId;
import com.djrapitops.plan.system.webserver.pages.*;
import com.djrapitops.plan.system.webserver.response.*;
import com.djrapitops.plan.system.webserver.response.errors.ForbiddenResponse;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;
import com.djrapitops.plan.system.webserver.response.pages.DebugPageResponse;
import com.djrapitops.plan.system.webserver.response.pages.InspectPageResponse;
import com.djrapitops.plan.system.webserver.response.pages.PlayersPageResponse;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.*;

/**
 * Handles choosing of the correct response to a request.
 *
 * @author Rsl1122
 */
public class ResponseHandler extends TreePageHandler {

    private final boolean authRequired;

    private final boolean usingHttps;

    public ResponseHandler(WebServer webServer) {
        authRequired = webServer.isAuthRequired();
        this.usingHttps = webServer.isUsingHTTPS();
    }

    public void registerDefaultPages() {
        registerPage("favicon.ico", new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
        registerPage("debug", new DebugPageHandler());
        registerPage("players", new PlayersPageHandler());
        registerPage("player", new PlayerPageHandler());

        ServerPageHandler serverPageHandler = new ServerPageHandler();
        registerPage("network", serverPageHandler);
        registerPage("server", serverPageHandler);
    }

    public void registerWebAPIPages() {

    }

    public Response getResponse(Request request) {
        String targetString = request.getTarget();
        List<String> target = Arrays.asList(targetString.split("/"));
        target.remove(0);
        try {
            Optional<Authentication> authentication = Optional.empty();
            if (authRequired) {
                authentication = request.getAuth();
                if (!authentication.isPresent() && usingHttps) {
                    return DefaultResponses.BASIC_AUTH.get();
                }

                if (authentication.isPresent() && !authentication.get().isAuthorized(null)) {
                    return forbiddenResponse(0, 0);
                }
            }

            PageHandler pageHandler = getPageHandler(target);
            if (pageHandler == null) {
                if (targetString.endsWith(".css")) {
                    return ResponseCache.loadResponse(PageId.CSS.of(targetString), () -> new CSSResponse(targetString));
                }
                if (targetString.endsWith(".js")) {
                    return ResponseCache.loadResponse(PageId.JS.of(targetString), () -> new JavaScriptResponse(targetString));
                }
                return DefaultResponses.NOT_FOUND.get();
            } else {
                if (authentication.isPresent() && authentication.get().isAuthorized(pageHandler.getPermission())) {
                    return forbiddenResponse(0, 0);
                }
                return pageHandler.getResponse(request, target);
            }
        } catch (WebUserAuthException e) {
            return PromptAuthorizationResponse.getBasicAuthResponse(e);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return new InternalErrorResponse(e, request.getTarget());
        }

        try {
            if ("/favicon.ico".equals(targetString)) {
                return ResponseCache.loadResponse(PageId.FAVICON_REDIRECT.id(), () -> new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"));
            }
            if (request.isAPIRequest()) {
                return getAPIResponse(request);
            }


            UUID serverUUID = PlanPlugin.getInstance().getServerUuid();

            if (usingHttps) {
                if (!request.hasAuth()) {
                    throw new WebUserAuthException("No Authorization");
                }

                WebUser user = getUser(request.getAuth());
                int required = getRequiredPermLevel(targetString, user.getName());
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
                    return ResponseCache.loadResponse(PageId.PLAYERS.id(), PlayersPageResponse::new);
                case "player":
                    return playerResponse(args);
                case "network":
                case "server":
                    if (args.length > 2) {
                        try {
                            Optional<UUID> serverUUIDOptional = PlanPlugin.getInstance().getDB().getServerTable().getServerUUID(args[2].replace("%20", " "));
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
            return ResponseCache.loadResponse(PageId.AUTH_PROMPT.id(), PromptAuthorizationResponse::getBasicAuthResponse);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return new InternalErrorResponse(e, request.getTarget());
        }
    }

    private Response forbiddenResponse(int required, int permLevel) {
        return ResponseCache.loadResponse(PageId.FORBIDDEN.of(required + "/" + permLevel), () ->
                new ForbiddenResponse("Unauthorized User.<br>"
                        + "Make sure your user has the correct access level.<br>"
                        + "This page requires permission level of " + required + ",<br>"
                        + "This user has permission level of " + permLevel));
    }

    private boolean isAuthorized(int requiredPermLevel, int permLevel) {
        return permLevel <= requiredPermLevel;
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
                return ResponseCache.loadResponse(PageId.PLAYERS.id(), PlayersPageResponse::new);
            case 2:
                return playerResponse(new String[]{"", "", user.getName()});
            default:
                return forbiddenResponse(user.getPermLevel(), 0);
        }
    }

    private Response serverResponse(UUID serverUUID) {
        return ResponseCache.loadResponse(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(plugin.getInfoManager()));
    }

    private Response notFoundResponse() {
        String error = "404 Not Found";
        return ResponseCache.loadResponse(PageId.NOT_FOUND.of("Wrong Page"), () -> {
                    String url = plugin.getInfoManager().getWebServerAddress();
                    return new NotFoundResponse("Make sure you're accessing a link given by a command, Examples:</p>"
                            + "<p>" + url + "/player/Playername<br>" +
                            url + "/server</p>");
                }
        );
    }


}