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
import com.djrapitops.plan.delivery.web.resolver.AsyncResolver;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.delivery.webserver.resolver.*;
import com.djrapitops.plan.delivery.webserver.resolver.auth.*;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.delivery.webserver.resolver.swagger.SwaggerJsonResolver;
import com.djrapitops.plan.delivery.webserver.resolver.swagger.SwaggerPageResolver;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dagger.Lazy;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
@OpenAPIDefinition(info = @Info(
        title = "Swagger Docs",
        description = "If authentication is enabled (see response of /v1/whoami) logging in is required for endpoints (/auth/login). Pass 'Cookie' header in the requests after login.",
        contact = @Contact(name = "Github Discussions", url = "https://github.com/plan-player-analytics/Plan/discussions/categories/apis-and-development"),
        license = @License(name = "GNU Lesser General Public License v3.0 (LGPLv3.0)", url = "https://github.com/plan-player-analytics/Plan/blob/master/LICENSE")
))
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
    private final SwaggerJsonResolver swaggerJsonResolver;
    private final SwaggerPageResolver swaggerPageResolver;
    private final ManagePageResolver managePageResolver;
    private final ThemeEditorResolver themeEditorResolver;
    private final ErrorLogger errorLogger;

    private final ResolverService resolverService;
    private final ResponseFactory responseFactory;
    private final Lazy<WebServer> webServer;
    private final WebserverConfiguration webserverConfiguration;
    private final PublicHtmlResolver publicHtmlResolver;

    @Inject
    public ResponseResolver(
            ResolverSvc resolverService,
            ResponseFactory responseFactory,
            Lazy<WebServer> webServer,
            WebserverConfiguration webserverConfiguration,

            QueryPageResolver queryPageResolver,
            PlayersPageResolver playersPageResolver,
            PlayerPageResolver playerPageResolver,
            ServerPageResolver serverPageResolver,
            RootPageResolver rootPageResolver,
            RootJSONResolver rootJSONResolver,
            StaticResourceResolver staticResourceResolver,
            ThemeEditorResolver themeEditorResolver,
            PublicHtmlResolver publicHtmlResolver,

            LoginPageResolver loginPageResolver,
            RegisterPageResolver registerPageResolver,
            LoginResolver loginResolver,
            LogoutResolver logoutResolver,
            RegisterResolver registerResolver,
            ErrorsPageResolver errorsPageResolver,

            SwaggerJsonResolver swaggerJsonResolver,
            SwaggerPageResolver swaggerPageResolver,

            ManagePageResolver managePageResolver, ErrorLogger errorLogger
    ) {
        this.resolverService = resolverService;
        this.responseFactory = responseFactory;
        this.webServer = webServer;
        this.webserverConfiguration = webserverConfiguration;
        this.queryPageResolver = queryPageResolver;
        this.playersPageResolver = playersPageResolver;
        this.playerPageResolver = playerPageResolver;
        this.serverPageResolver = serverPageResolver;
        this.rootPageResolver = rootPageResolver;
        this.rootJSONResolver = rootJSONResolver;
        this.staticResourceResolver = staticResourceResolver;
        this.themeEditorResolver = themeEditorResolver;
        this.publicHtmlResolver = publicHtmlResolver;
        this.loginPageResolver = loginPageResolver;
        this.registerPageResolver = registerPageResolver;
        this.loginResolver = loginResolver;
        this.logoutResolver = logoutResolver;
        this.registerResolver = registerResolver;
        this.errorsPageResolver = errorsPageResolver;
        this.swaggerJsonResolver = swaggerJsonResolver;
        this.swaggerPageResolver = swaggerPageResolver;
        this.managePageResolver = managePageResolver;
        this.errorLogger = errorLogger;
    }

    public void registerPages() {
        String plugin = "Plan";
        resolverService.registerResolver(plugin, "/robots.txt", fileResolver(responseFactory::robotsResponse));
        resolverService.registerResolver(plugin, "/manifest.json", fileResolver(() -> responseFactory.jsonFileResponse("manifest.json")));
        resolverService.registerResolver(plugin, "/favicon.ico", fileResolver(responseFactory::faviconResponse));
        resolverService.registerResolver(plugin, "/logo192.png", fileResolver(() -> responseFactory.imageResponse("logo192.png")));
        resolverService.registerResolver(plugin, "/logo512.png", fileResolver(() -> responseFactory.imageResponse("logo512.png")));
        resolverService.registerResolver(plugin, "/pageExtensionApi.js", fileResolver(() -> responseFactory.javaScriptResponse("pageExtensionApi.js")));

        resolverService.registerResolver(plugin, "/query", queryPageResolver);
        resolverService.registerResolver(plugin, "/players", playersPageResolver);
        resolverService.registerResolver(plugin, "/player", playerPageResolver);
        resolverService.registerResolver(plugin, "/network", serverPageResolver);
        resolverService.registerResolver(plugin, "/server", serverPageResolver);
        resolverService.registerResolver(plugin, "/theme-editor", themeEditorResolver);
        if (webServer.get().isAuthRequired()) {
            resolverService.registerResolver(plugin, "/login", loginPageResolver);
            resolverService.registerResolver(plugin, "/register", registerPageResolver);
            resolverService.registerResolver(plugin, "/auth/login", loginResolver);
            resolverService.registerResolver(plugin, "/auth/logout", logoutResolver);
            if (webserverConfiguration.isRegistrationEnabled()) {
                resolverService.registerResolver(plugin, "/auth/register", registerResolver);
            }
            resolverService.registerResolver(plugin, "/manage", managePageResolver);
        }

        resolverService.registerResolver(plugin, "/errors", errorsPageResolver);

        resolverService.registerResolverForMatches(plugin, Pattern.compile("^/$"), rootPageResolver);
        resolverService.registerResolverForMatches(plugin, Pattern.compile(StaticResourceResolver.PATH_REGEX), staticResourceResolver);
        resolverService.registerResolverForMatches(plugin, Pattern.compile(".*"), publicHtmlResolver);

        resolverService.registerResolver(plugin, "/v1", rootJSONResolver.getResolver());
        resolverService.registerResolver(plugin, "/docs/swagger.json", swaggerJsonResolver);
        resolverService.registerResolver(plugin, "/docs", swaggerPageResolver);
    }

    private NoAuthResolver fileResolver(Supplier<Response> response) {
        return request -> Optional.of(response.get());
    }

    public CompletableFuture<Response> getResponse(@Untrusted Request request) {
        try {
            return tryToGetResponse(request)
                    .exceptionallyCompose(throwable -> handleException(request, throwable));
        } catch (Exception t) {
            return handleException(request, t);
        }
    }

    public CompletableFuture<Response> handleException(@Untrusted Request request, Throwable exception) {
        while (exception instanceof CompletionException || exception instanceof java.util.concurrent.ExecutionException) {
            if (exception.getCause() != null) {
                exception = exception.getCause();
            } else {
                break;
            }
        }
        if (exception instanceof BadRequestException badRequest) {
            return CompletableFuture.completedFuture(responseFactory.badRequest(
                    badRequest.getMessage(), request.getPath().asString()));
        }
        if (exception instanceof MethodNotAllowedException notAllowed) {
            return CompletableFuture.completedFuture(responseFactory.methodNotAllowed405(
                    notAllowed.getMessage(), notAllowed.getAllowedMethods()));
        }
        if (exception instanceof WebUserAuthException authException) {
            return CompletableFuture.failedFuture(authException); // Pass along
        }
        return CompletableFuture.completedFuture(responseFactory.internalErrorResponse(
                exception, "Failed to get a response"));
    }

    private CompletableFuture<Response> tryToGetResponse(@Untrusted Request request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods/OPTIONS
            return CompletableFuture.completedFuture(Response.builder().setStatus(204).build());
        }


        List<Resolver> foundResolvers = resolverService.getResolvers(request.getPath().asString());
        if (foundResolvers.isEmpty()) return CompletableFuture.completedFuture(responseFactory.pageNotFound404());
        return resolveNext(foundResolvers.iterator(), request);
    }

    private CompletableFuture<Response> resolveNext(Iterator<Resolver> iterator, @Untrusted Request request) {
        if (!iterator.hasNext()) {
            return CompletableFuture.completedFuture(responseFactory.pageNotFound404());
        }

        Resolver resolver = iterator.next();
        Optional<WebUser> user = request.getUser();
        boolean isAuthRequired = webServer.get().isAuthRequired() && resolver.requiresAuth(request);
        if (isAuthRequired) {
            if (user.isEmpty()) {
                if (webServer.get().isUsingHTTPS()) {
                    throw new WebUserAuthException(FailReason.NO_USER_PRESENT);
                } else {
                    return CompletableFuture.completedFuture(responseFactory.forbidden403());
                }
            }

            if (!resolver.canAccess(request)) {
                if (request.getPath().startsWith("/v1/")) {
                    return CompletableFuture.completedFuture(responseFactory.forbidden403Json());
                } else {
                    return CompletableFuture.completedFuture(responseFactory.forbidden403());
                }
            }
        }

        CompletableFuture<Optional<Response>> futureResponse;
        try {
            futureResponse = resolver instanceof AsyncResolver asyncResolver
                    ? asyncResolver.resolveAsync(request)
                    : CompletableFuture.supplyAsync(() -> resolver.resolve(request));
        } catch (Exception t) { // Some exceptions may be thrown sync based on the request.
            futureResponse = CompletableFuture.failedFuture(t);
        }
        return futureResponse.thenCompose(gotResponse ->
                gotResponse.map(CompletableFuture::completedFuture) // try next resolver
                        .orElseGet(() -> resolveNext(iterator, request))
        );
    }
}
