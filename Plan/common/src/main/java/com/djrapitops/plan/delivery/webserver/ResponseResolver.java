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
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.resolver.*;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
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
public class ResponseResolver {

    private final DebugPageResolver debugPageResolver;
    private final PlayersPageResolver playersPageResolver;
    private final PlayerPageResolver playerPageResolver;
    private final ServerPageResolver serverPageResolver;
    private final RootPageResolver rootPageResolver;
    private final RootJSONResolver rootJSONResolver;
    private final StaticResourceResolver staticResourceResolver;
    private final ErrorHandler errorHandler;

    private final ResolverService resolverService;
    private final ResponseFactory responseFactory;
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
        this.resolverService = resolverService;
        this.responseFactory = responseFactory;
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
        String plugin = "Plan";
        resolverService.registerResolver(plugin, "/debug", debugPageResolver);
        resolverService.registerResolver(plugin, "/players", playersPageResolver);
        resolverService.registerResolver(plugin, "/player", playerPageResolver);
        resolverService.registerResolver(plugin, "/favicon.ico", (NoAuthResolver) request -> Optional.of(responseFactory.faviconResponse()));
        resolverService.registerResolver(plugin, "/network", serverPageResolver);
        resolverService.registerResolver(plugin, "/server", serverPageResolver);
        resolverService.registerResolverForMatches(plugin, Pattern.compile("^/$"), rootPageResolver);
        resolverService.registerResolverForMatches(plugin, Pattern.compile("^/(vendor|css|js|img)/.*"), staticResourceResolver);

        resolverService.registerResolver(plugin, "/v1", rootJSONResolver.getResolver());
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

    /**
     * @throws NotFoundException    In some cases when page was not found, not all.
     * @throws WebUserAuthException If user could not be authenticated
     * @throws ForbiddenException   If the user is not allowed to see the page
     * @throws BadRequestException  If the request did not have required things.
     */
    private Response tryToGetResponse(RequestInternal internalRequest) {
        if ("OPTIONS".equalsIgnoreCase(internalRequest.getRequestMethod())) {
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS
            return Response.builder().setStatus(204).setContent(new byte[0]).build();
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
