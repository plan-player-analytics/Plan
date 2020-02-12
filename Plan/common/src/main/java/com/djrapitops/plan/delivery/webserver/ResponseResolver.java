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
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.URIPath;
import com.djrapitops.plan.delivery.web.resolver.URIQuery;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.pages.*;
import com.djrapitops.plan.delivery.webserver.pages.json.RootJSONResolver;
import com.djrapitops.plan.delivery.webserver.response.OptionsResponse;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.BadRequestException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

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
    private final RootJSONResolver rootJSONResolver;
    private final ErrorHandler errorHandler;

    private final ServerInfo serverInfo;
    private final ResolverService resolverService;
    private final Lazy<WebServer> webServer;

    @Inject
    public ResponseResolver(
            ResolverSvc resolverService,
            ResponseFactory responseFactory,
            Lazy<WebServer> webServer,
            ServerInfo serverInfo,

            DebugPageResolver debugPageResolver,
            PlayersPageResolver playersPageResolver,
            PlayerPageResolver playerPageResolver,
            ServerPageResolver serverPageResolver,
            RootJSONResolver rootJSONResolver,

            ErrorHandler errorHandler
    ) {
        super(responseFactory);
        this.resolverService = resolverService;
        this.webServer = webServer;
        this.serverInfo = serverInfo;
        this.debugPageResolver = debugPageResolver;
        this.playersPageResolver = playersPageResolver;
        this.playerPageResolver = playerPageResolver;
        this.serverPageResolver = serverPageResolver;
        this.rootJSONResolver = rootJSONResolver;
        this.errorHandler = errorHandler;
    }

    public void registerPages() {
        resolverService.registerResolver("Plan", "/debug", debugPageResolver);
        registerPage("players", playersPageResolver);
        registerPage("player", playerPageResolver);

        registerPage("network", serverPageResolver);
        registerPage("server", serverPageResolver);

        // TODO Figure out how to deal with stuff like this
        registerPage("", new RootPageResolver(responseFactory, webServer.get(), serverInfo));

        registerPage("v1", rootJSONResolver);
    }

    public Response_old getResponse(Request request) {
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

    private Response_old tryToGetResponse(Request request) throws WebException {
        if ("OPTIONS".equalsIgnoreCase(request.getRequestMethod())) {
            return new OptionsResponse();
        }

        Optional<Authentication> authentication = request.getAuth();

        URIPath target = request.getPath();
        URIQuery query = request.getQuery();

        Optional<Resolver> foundResolver = resolverService.getResolver(target.asString());
        if (!foundResolver.isPresent()) return tryToGetResponse_old(request); // TODO Replace with 404 after refactoring

        Resolver resolver = foundResolver.get();

        if (resolver.requiresAuth(target, query)) {
            // Get required auth
            boolean isAuthRequired = webServer.get().isAuthRequired();
            if (isAuthRequired && !authentication.isPresent()) {
                if (webServer.get().isUsingHTTPS()) {
                    return responseFactory.basicAuth();
                } else {
                    return responseFactory.forbidden403();
                }
            }

            if (!isAuthRequired || resolver.canAccess(authentication.get().getWebUser().toNewWebUser(), target, query)) {
                return resolver.resolve(target, query).map(Response_old::from).orElseGet(responseFactory::pageNotFound404);
            } else {
                return responseFactory.forbidden403();
            }
        } else {
            return resolver.resolve(target, query).map(Response_old::from).orElseGet(responseFactory::pageNotFound404);
        }
    }

    private Response_old tryToGetResponse_old(Request request) throws WebException {
        RequestTarget target = request.getRequestTarget();
        Optional<Authentication> authentication = request.getAuth();
        String resource = target.getResourceString();
        // TODO Turn into resolvers
        if (target.endsWith(".css")) {
            return responseFactory.cssResponse(resource);
        }
        if (target.endsWith(".js")) {
            return responseFactory.javaScriptResponse(resource);
        }
        if (target.endsWith(".png")) {
            return responseFactory.imageResponse(resource);
        }
        if (target.endsWith("favicon.ico")) {
            return responseFactory.faviconResponse();
        }
        if (target.endsWithAny(".woff", ".woff2", ".eot", ".ttf")) {
            return responseFactory.fontResponse(resource);
        }
        boolean isAuthRequired = webServer.get().isAuthRequired();
        if (isAuthRequired && !authentication.isPresent()) {
            if (webServer.get().isUsingHTTPS()) {
                return responseFactory.basicAuth();
            } else {
                return responseFactory.forbidden403();
            }
        }
        PageResolver pageResolver = getPageResolver(target);
        if (pageResolver == null) {
            return responseFactory.pageNotFound404();
        } else {
            if (!isAuthRequired || pageResolver.isAuthorized(authentication.get(), target)) {
                return pageResolver.resolve(request, target);
            }
            return responseFactory.forbidden403();
        }
    }
}
