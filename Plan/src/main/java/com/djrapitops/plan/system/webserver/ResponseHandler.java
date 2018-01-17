/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.*;
import com.djrapitops.plan.system.webserver.response.*;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.ForbiddenResponse;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        registerPage("favicon.ico", new RedirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"), 5);
        registerPage("debug", new DebugPageHandler());
        registerPage("players", new PlayersPageHandler());
        registerPage("player", new PlayerPageHandler());

        ServerPageHandler serverPageHandler = new ServerPageHandler();
        registerPage("network", serverPageHandler);
        registerPage("server", serverPageHandler);
        if (authRequired) {
            registerPage("", new RootPageHandler(this));
        }
    }

    public void registerWebAPIPages() {
// TODO WebAPIPageHandler
//    private void registerWebAPIs() {
//        webAPI.registerNewAPI(
//                new AnalysisReadyWebAPI(),
//                new AnalyzeWebAPI(),
//                new ConfigurationWebAPI(),
//                new InspectWebAPI(),
//                new IsOnlineWebAPI(),
//                new RequestInspectPluginsTabBukkitWebAPI(),
//                new PingWebAPI()
//        );
//
//        webAPI.registerNewAPI(
//                new IsCachedWebAPI(),
//                new PostHtmlWebAPI(),
//                new PostInspectPluginsTabWebAPI(),
//                new PostNetworkPageContentWebAPI(),
//                new PostOriginalBukkitSettingsWebAPI(),
//                new RequestPluginsTabWebAPI(),
//                new RequestSetupWebAPI()
//        );
//    }
        //Log.infoColor("§aWebServer Running in WebAPI-only Mode");
    }

    public Response getResponse(Request request) {
        String targetString = request.getTarget();
        List<String> target = Arrays.asList(targetString.split("/"));
        target.remove(0);
        try {
            Optional<Authentication> authentication = Optional.empty();
            if (authRequired) {
                authentication = request.getAuth();
                if (!authentication.isPresent()) {
                    if (usingHttps) {
                        return DefaultResponses.BASIC_AUTH.get();
                    } else {
                        return forbiddenResponse(0, 0);
                    }
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
                if (authentication.isPresent() && pageHandler.isAuthorized(authentication.get(), target)) {
                    return forbiddenResponse(0, 0);
                }
                return pageHandler.getResponse(request, target);
            }
        } catch (WebUserAuthException e) {
            return PromptAuthorizationResponse.getBasicAuthResponse(e);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return new InternalErrorResponse(request.getTarget(), e);
        }
    }

    public Response forbiddenResponse(int required, int permLevel) {
        return ResponseCache.loadResponse(PageId.FORBIDDEN.of(required + "/" + permLevel), () ->
                new ForbiddenResponse("Unauthorized User.<br>"
                        + "Make sure your user has the correct access level.<br>"
                        + "This page requires permission level of " + required + ",<br>"
                        + "This user has permission level of " + permLevel));
    }
}