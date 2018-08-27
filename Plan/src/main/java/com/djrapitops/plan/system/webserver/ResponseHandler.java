/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.info.connection.InfoRequestPageHandler;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.pages.*;
import com.djrapitops.plan.system.webserver.response.*;
import com.djrapitops.plan.system.webserver.response.errors.*;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Handles choosing of the correct response to a request.
 *
 * @author Rsl1122
 */
@Singleton
public class ResponseHandler extends TreePageHandler {

    private final ResponseFactory responseFactory;

    private final ErrorHandler errorHandler;

    private WebServer webServer;

    @Inject
    public ResponseHandler(
            ResponseFactory responseFactory,

            DebugPageHandler debugPageHandler,
            PlayersPageHandler playersPageHandler,
            PlayerPageHandler playerPageHandler,
            ServerPageHandler serverPageHandler,
            InfoRequestPageHandler infoRequestPageHandler,

            ErrorHandler errorHandler
    ) {
        this.responseFactory = responseFactory;
        this.errorHandler = errorHandler;

        registerPage("favicon.ico", responseFactory.redirectResponse("https://puu.sh/tK0KL/6aa2ba141b.ico"), 5);
        registerPage("debug", debugPageHandler);
        registerPage("players", playersPageHandler);
        registerPage("player", playerPageHandler);

        registerPage("network", serverPageHandler);
        registerPage("server", serverPageHandler);

        registerPage("", webServer.isAuthRequired()
                ? new RootPageHandler()
                : (request, target) -> new RedirectResponse("/server"));

        registerPage("info", infoRequestPageHandler);
    }

    public Response getResponse(Request request) {
        String targetString = request.getTarget();
        List<String> target = new ArrayList<>(Arrays.asList(targetString.split("/")));
        if (!target.isEmpty()) {
            target.remove(0);
        }
        try {
            return getResponse(request, targetString, target);
        } catch (NoServersException | NotFoundException e) {
            return new NotFoundResponse(e.getMessage());
        } catch (WebUserAuthException e) {
            return PromptAuthorizationResponse.getBasicAuthResponse(e);
        } catch (ForbiddenException e) {
            return new ForbiddenResponse(e.getMessage());
        } catch (BadRequestException e) {
            return new BadRequestResponse(e.getMessage());
        } catch (UnauthorizedServerException e) {
            return new UnauthorizedServerResponse(e.getMessage());
        } catch (GatewayException e) {
            return new GatewayErrorResponse(e.getMessage());
        } catch (InternalErrorException e) {
            if (e.getCause() != null) {
                return new InternalErrorResponse(request.getTarget(), e.getCause());
            } else {
                return new InternalErrorResponse(request.getTarget(), e);
            }
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            return new InternalErrorResponse(request.getTarget(), e);
        }
    }

    private Response getResponse(Request request, String targetString, List<String> target) throws WebException {
        Optional<Authentication> authentication = Optional.empty();

        if (targetString.endsWith(".css")) {
            return ResponseCache.loadResponse(PageId.CSS.of(targetString), () -> responseFactory.cssResponse(targetString));
        }
        if (targetString.endsWith(".js")) {
            return ResponseCache.loadResponse(PageId.JS.of(targetString), () -> responseFactory.javaScriptResponse(targetString));
        }
        boolean isNotInfoRequest = target.isEmpty() || !target.get(0).equals("info");
        boolean isAuthRequired = webServer.isAuthRequired() && isNotInfoRequest;
        if (isAuthRequired) {
            authentication = request.getAuth();
            if (!authentication.isPresent()) {
                if (webServer.isUsingHTTPS()) {
                    return DefaultResponses.BASIC_AUTH.get();
                } else {
                    return DefaultResponses.FORBIDDEN.get();
                }
            }
        }
        PageHandler pageHandler = getPageHandler(target);
        if (pageHandler == null) {
            return responseFactory.pageNotFound404();
        } else {
            boolean isAuthorized = authentication.isPresent() && pageHandler.isAuthorized(authentication.get(), target);
            if (!isAuthRequired || isAuthorized) {
                return pageHandler.getResponse(request, target);
            }
            return DefaultResponses.FORBIDDEN.get();
        }
    }

    public void setWebServer(WebServer webServer) {
        this.webServer = webServer;
    }
}
