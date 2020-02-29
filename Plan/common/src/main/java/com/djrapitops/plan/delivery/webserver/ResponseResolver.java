/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.web.ResolverService;
import com.djrapitops.plan.delivery.web.ResolverSvc;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.pages.*;
import com.djrapitops.plan.delivery.webserver.pages.json.RootJSONResolver;
import com.djrapitops.plan.delivery.webserver.response.OptionsResponse;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.BadRequestException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Resolves All URLs.
 * <p>
 * - Some URLs are resolved with other PageResolvers pointed at pages.
 * - Some URLs point to resources that are resolved differently, those implementations are in this file.
 *
 * @author Rsl1122
 */
@Singleton
public class ResponseResolver extends CompositePageResolver {

    private final DebugPageResolver debugPageResolver;
    private final PlayersPageResolver playersPageResolver;
    private final PlayerPageResolver playerPageResolver;
    private final ServerPageResolver serverPageResolver;
    private final RootPageResolver rootPageResolver;
    private final RootJSONResolver rootJSONResolver;
    private final StaticResourceResolver staticResourceResolver;
    private final ErrorHandler errorHandler;

    private final ResolverService resolverService;
    private final Lazy<WebServer> webServer;

    @Inject
    public ResponseResolver(
            ResolverSvc resolverService,
            ResponseFactory responseFactory,
            Lazy<WebServer> webServer,

            DebugPageResolver debugPageResolver,
            PlayersPageResolver playersPageResolver,
            PlayerPageResolver playerPageResolver,
            ServerPageResolver serverPageResolver,
            RootPageResolver rootPageResolver,
            RootJSONResolver rootJSONResolver,
            StaticResourceResolver staticResourceResolver,

            ErrorHandler errorHandler
    ) {
        super(responseFactory);
        this.resolverService = resolverService;
        this.webServer = webServer;
        this.debugPageResolver = debugPageResolver;
        this.playersPageResolver = playersPageResolver;
        this.playerPageResolver = playerPageResolver;
        this.serverPageResolver = serverPageResolver;
        this.rootPageResolver = rootPageResolver;
        this.rootJSONResolver = rootJSONResolver;
        this.staticResourceResolver = staticResourceResolver;
        this.errorHandler = errorHandler;
    }

    public void registerPages() {
        String pluginName = "Plan";
        resolverService.registerResolver(pluginName, "/debug", debugPageResolver);
        resolverService.registerResolver(pluginName, "/players", playersPageResolver);
        resolverService.registerResolver(pluginName, "/player", playerPageResolver);
        resolverService.registerResolver(pluginName, "/favicon.ico", noAuthResolverFor(responseFactory.faviconResponse()));
        resolverService.registerResolver(pluginName, "/network", serverPageResolver);
        resolverService.registerResolver(pluginName, "/server", serverPageResolver);
        resolverService.registerResolverForMatches(pluginName, Pattern.compile("^/$"), rootPageResolver);
        resolverService.registerResolverForMatches(pluginName, Pattern.compile("^/(vendor|css|js|img)/.*"), staticResourceResolver);

        registerPage("v1", rootJSONResolver);
    }

    public NoAuthResolver noAuthResolverFor(Response response) {
        return (request) -> Optional.of(response);
    }

    public Response getResponse(RequestInternal request) {
        try {
            return tryToGetResponse(request);
        } catch (NotFoundException e) {
            return responseFactory.notFound404(e.getMessage());
        } catch (WebUserAuthException e) {
            return responseFactory.basicAuthFail(e);
        } catch (ForbiddenException e) {
            return responseFactory.forbidden403(e.getMessage());
        } catch (BadRequestException e) {
            return responseFactory.badRequest(e.getMessage(), request.getTargetString());
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
            return responseFactory.internalErrorResponse(e, request.getTargetString());
        }
    }

    private Response tryToGetResponse(RequestInternal internalRequest) throws WebException {
        if ("OPTIONS" .equalsIgnoreCase(internalRequest.getRequestMethod())) {
            return new OptionsResponse().toNewResponse();
        }

        Optional<Authentication> authentication = internalRequest.getAuth();

        Optional<Resolver> foundResolver = resolverService.getResolver(internalRequest.getPath().asString());
        if (!foundResolver.isPresent()) return responseFactory.pageNotFound404();

        Resolver resolver = foundResolver.get();

        Request request = internalRequest.toAPIRequest();
        if (resolver.requiresAuth(request)) {
            // Get required auth
            boolean isAuthRequired = webServer.get().isAuthRequired();
            if (isAuthRequired && !authentication.isPresent()) {
                if (webServer.get().isUsingHTTPS()) {
                    return responseFactory.basicAuth();
                } else {
                    return responseFactory.forbidden403();
                }
            }

            if (!isAuthRequired || resolver.canAccess(request)) {
                return resolver.resolve(request).orElseGet(responseFactory::pageNotFound404);
            } else {
                return responseFactory.forbidden403();
            }
        } else {
            return resolver.resolve(request).orElseGet(responseFactory::pageNotFound404);
        }
    }
}
