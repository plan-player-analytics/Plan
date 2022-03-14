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
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.resolver.*;
import com.djrapitops.plan.delivery.webserver.resolver.auth.*;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Resolves All URLs.
 * <p>
 * - Some URLs are resolved with other PageResolvers pointed at pages.
 * - Some URLs point to resources that are resolved differently, those implementations are in this file.
 *
 * @author AuroraLS3
 */
@Singleton
public class ResponseResolver {

    private final QueryPageResolver queryPageResolver;
    private final PlayersPageResolver playersPageResolver;
    private final PlayerPageResolver playerPageResolver;
    private final ServerPageResolver serverPageResolver;
    private final RootPageResolver rootPageResolver;
    private final RootJSONResolver rootJSONResolver;
    private final StaticResourceResolver staticResourceResolver;
    private final LoginPageResolver loginPageResolver;
    private final RegisterPageResolver registerPageResolver;
    private final LoginResolver loginResolver;
    private final LogoutResolver logoutResolver;
    private final RegisterResolver registerResolver;
    private final ErrorsPageResolver errorsPageResolver;
    private final ErrorLogger errorLogger;

    private final ResolverService resolverService;
    private final ResponseFactory responseFactory;
    private final Lazy<WebServer> webServer;

    @Inject
    public ResponseResolver(
            ResolverSvc resolverService,
            ResponseFactory responseFactory,
            Lazy<WebServer> webServer,

            QueryPageResolver queryPageResolver,
            PlayersPageResolver playersPageResolver,
            PlayerPageResolver playerPageResolver,
            ServerPageResolver serverPageResolver,
            RootPageResolver rootPageResolver,
            RootJSONResolver rootJSONResolver,
            StaticResourceResolver staticResourceResolver,

            LoginPageResolver loginPageResolver,
            RegisterPageResolver registerPageResolver,
            LoginResolver loginResolver,
            LogoutResolver logoutResolver,
            RegisterResolver registerResolver,
            ErrorsPageResolver errorsPageResolver,

            ErrorLogger errorLogger
    ) {
        this.resolverService = resolverService;
        this.responseFactory = responseFactory;
        this.webServer = webServer;
        this.queryPageResolver = queryPageResolver;
        this.playersPageResolver = playersPageResolver;
        this.playerPageResolver = playerPageResolver;
        this.serverPageResolver = serverPageResolver;
        this.rootPageResolver = rootPageResolver;
        this.rootJSONResolver = rootJSONResolver;
        this.staticResourceResolver = staticResourceResolver;
        this.loginPageResolver = loginPageResolver;
        this.registerPageResolver = registerPageResolver;
        this.loginResolver = loginResolver;
        this.logoutResolver = logoutResolver;
        this.registerResolver = registerResolver;
        this.errorsPageResolver = errorsPageResolver;
        this.errorLogger = errorLogger;
    }

    public void registerPages() {
        String plugin = "Plan";
        resolverService.registerResolver(plugin, "/robots.txt", fileResolver(responseFactory::robotsResponse));
        resolverService.registerResolver(plugin, "/manifest.json", fileResolver(() -> responseFactory.jsonFileResponse("manifest.json")));
        resolverService.registerResolver(plugin, "/asset-manifest.json", fileResolver(() -> responseFactory.jsonFileResponse("asset-manifest.json")));
        resolverService.registerResolver(plugin, "/favicon.ico", fileResolver(responseFactory::faviconResponse));

        resolverService.registerResolver(plugin, "/query", queryPageResolver);
        resolverService.registerResolver(plugin, "/players", playersPageResolver);
        resolverService.registerResolver(plugin, "/player", playerPageResolver);
        resolverService.registerResolver(plugin, "/network", serverPageResolver);
        resolverService.registerResolver(plugin, "/server", serverPageResolver);

        resolverService.registerResolver(plugin, "/login", loginPageResolver);
        resolverService.registerResolver(plugin, "/register", registerPageResolver);
        resolverService.registerResolver(plugin, "/auth/login", loginResolver);
        resolverService.registerResolver(plugin, "/auth/logout", logoutResolver);
        resolverService.registerResolver(plugin, "/auth/register", registerResolver);

        resolverService.registerResolver(plugin, "/errors", errorsPageResolver);

        resolverService.registerResolverForMatches(plugin, Pattern.compile("^/$"), rootPageResolver);
        resolverService.registerResolverForMatches(plugin, Pattern.compile(StaticResourceResolver.PATH_REGEX), staticResourceResolver);

        resolverService.registerResolver(plugin, "/v1", rootJSONResolver.getResolver());
    }

    private NoAuthResolver fileResolver(Supplier<Response> response) {
        return request -> Optional.of(response.get());
    }

    public Response getResponse(Request request) {
        try {
            return tryToGetResponse(request);
        } catch (NotFoundException e) {
            return responseFactory.notFound404(e.getMessage());
        } catch (ForbiddenException e) {
            return responseFactory.forbidden403(e.getMessage());
        } catch (BadRequestException e) {
            return responseFactory.badRequest(e.getMessage(), request.getPath().asString());
        } catch (WebUserAuthException e) {
            throw e; // Pass along
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(request).build());
            return responseFactory.internalErrorResponse(e, "Failed to get a response");
        }
    }

    /**
     * @throws NotFoundException   In some cases when page was not found, not all.
     * @throws ForbiddenException  If the user is not allowed to see the page
     * @throws BadRequestException If the request did not have required things.
     */
    private Response tryToGetResponse(Request request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS
            return Response.builder().setStatus(204).build();
        }

        Optional<WebUser> user = request.getUser();

        List<Resolver> foundResolvers = resolverService.getResolvers(request.getPath().asString());
        if (foundResolvers.isEmpty()) return responseFactory.pageNotFound404();

        for (Resolver resolver : foundResolvers) {
            boolean isAuthRequired = webServer.get().isAuthRequired() && resolver.requiresAuth(request);
            if (isAuthRequired) {
                if (!user.isPresent()) {
                    if (webServer.get().isUsingHTTPS()) {
                        throw new WebUserAuthException(FailReason.NO_USER_PRESENT);
                    } else {
                        return responseFactory.forbidden403();
                    }
                }

                if (resolver.canAccess(request)) {
                    Optional<Response> resolved = resolver.resolve(request);
                    if (resolved.isPresent()) return resolved.get();
                } else {
                    return responseFactory.forbidden403();
                }
            } else {
                Optional<Response> resolved = resolver.resolve(request);
                if (resolved.isPresent()) return resolved.get();
            }
        }
        return responseFactory.pageNotFound404();
    }
}
