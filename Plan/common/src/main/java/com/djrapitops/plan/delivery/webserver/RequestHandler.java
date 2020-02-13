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

import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.auth.BasicAuthentication;
import com.djrapitops.plan.delivery.webserver.response.PromptAuthorizationResponse;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.errors.ForbiddenResponse;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * HttpHandler for WebServer request management.
 *
 * @author Rsl1122
 */
@Singleton
public class RequestHandler implements HttpHandler {

    private final Locale locale;
    private final PlanConfig config;
    private final Theme theme;
    private final DBSystem dbSystem;
    private final ResponseResolver responseResolver;
    private final ResponseFactory responseFactory;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private final Cache<String, Integer> failedLoginAttempts = Caffeine.newBuilder()
            .expireAfterWrite(90, TimeUnit.SECONDS)
            .build();

    @Inject
    RequestHandler(
            Locale locale,
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            ResponseResolver responseResolver,
            ResponseFactory responseFactory,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.responseResolver = responseResolver;
        this.responseFactory = responseFactory;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void handle(HttpExchange exchange) {
        Headers requestHeaders = exchange.getRequestHeaders();
        Headers responseHeaders = exchange.getResponseHeaders();

        Request request = new Request(exchange, locale);
        request.setAuth(getAuthorization(requestHeaders));

        try {
            Response_old response = shouldPreventRequest(request.getRemoteAddress()) // Forbidden response (Optional)
                    .orElseGet(() -> responseResolver.getResponse(request));     // Or the actual requested response

            // Increase attempt count and block if too high
            Optional<Response_old> forbid = handlePasswordBruteForceAttempts(request, response);
            if (forbid.isPresent()) {
                response = forbid.get();
            }

            // Authentication failed, but was not blocked
            if (response instanceof PromptAuthorizationResponse) {
                responseHeaders.set("WWW-Authenticate", response.getHeader("WWW-Authenticate").orElse("Basic realm=\"Plan WebUser (/plan register)\""));
            }

            responseHeaders.set("Access-Control-Allow-Origin", config.get(WebserverSettings.CORS_ALLOW_ORIGIN));
            responseHeaders.set("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setResponseHeaders(responseHeaders);
            response.send(exchange, locale, theme);
        } catch (Exception e) {
            if (config.isTrue(PluginSettings.DEV_MODE)) {
                logger.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        } finally {
            exchange.close();
        }
    }

    private Optional<Response_old> shouldPreventRequest(String accessor) {
        Integer attempts = failedLoginAttempts.getIfPresent(accessor);
        if (attempts == null) {
            attempts = 0;
        }

        // Too many attempts, forbid further attempts.
        if (attempts >= 5) {
            return createForbiddenResponse();
        }
        return Optional.empty();
    }

    private Optional<Response_old> handlePasswordBruteForceAttempts(Request request, Response_old response) {
        if (request.getAuth().isPresent() && response instanceof PromptAuthorizationResponse) {
            // Authentication was attempted, but failed so new attempt is going to be given if not forbidden

            failedLoginAttempts.cleanUp();

            String accessor = request.getRemoteAddress();
            Integer attempts = failedLoginAttempts.getIfPresent(accessor);
            if (attempts == null) {
                attempts = 0;
            }

            // Too many attempts, forbid further attempts.
            if (attempts >= 5) {
                logger.warn(accessor + " failed to login 5 times. Their access is blocked for 90 seconds.");
                return createForbiddenResponse();
            }

            // Attempts only increased if less than 5 attempts to prevent frustration from the cache value not
            // getting removed.
            failedLoginAttempts.put(accessor, attempts + 1);
        } else if (!(response instanceof PromptAuthorizationResponse) && !(response instanceof ForbiddenResponse)) {
            // Successful login
            failedLoginAttempts.invalidate(request.getRemoteAddress());
        }
        // First connection, no authentication headers present.
        return Optional.empty();
    }

    private Optional<Response_old> createForbiddenResponse() {
        return Optional.of(responseFactory.forbidden403_old("You have too many failed login attempts. Please wait 2 minutes until attempting again."));
    }

    private Authentication getAuthorization(Headers requestHeaders) {
        List<String> authorization = requestHeaders.get("Authorization");
        if (Verify.isEmpty(authorization)) {
            return null;
        }

        String authLine = authorization.get(0);
        if (StringUtils.contains(authLine, "Basic ")) {
            return new BasicAuthentication(StringUtils.split(authLine, ' ')[1], dbSystem.getDatabase());
        }
        return null;
    }

    public ResponseResolver getResponseResolver() {
        return responseResolver;
    }
}
