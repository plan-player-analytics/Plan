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

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.*;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * HttpHandler for WebServer request management.
 *
 * @author Rsl1122
 */
@Singleton
public class RequestHandler implements HttpHandler {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Addresses addresses;
    private final ResponseResolver responseResolver;
    private final ResponseFactory responseFactory;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    private PassBruteForceGuard bruteForceGuard;

    @Inject
    RequestHandler(
            PlanConfig config,
            DBSystem dbSystem,
            Addresses addresses,
            ResponseResolver responseResolver,
            ResponseFactory responseFactory,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.addresses = addresses;
        this.responseResolver = responseResolver;
        this.responseFactory = responseFactory;
        this.logger = logger;
        this.errorHandler = errorHandler;

        bruteForceGuard = new PassBruteForceGuard();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            Response response = getResponse(exchange);
            response.getHeaders().putIfAbsent("Access-Control-Allow-Origin", config.get(WebserverSettings.CORS_ALLOW_ORIGIN));
            response.getHeaders().putIfAbsent("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.getHeaders().putIfAbsent("Access-Control-Allow-Credentials", "true");
            ResponseSender sender = new ResponseSender(addresses, exchange, response);
            sender.send();
        } catch (Exception e) {
            if (config.isTrue(PluginSettings.DEV_MODE)) {
                logger.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        } finally {
            exchange.close();
        }
    }

    public Response getResponse(HttpExchange exchange) {
        String accessor = exchange.getRemoteAddress().getAddress().getHostAddress();
        Request request = null;
        Response response;
        try {
            request = buildRequest(exchange);
            if (bruteForceGuard.shouldPreventRequest(accessor)) {
                response = responseFactory.failedLoginAttempts403();
            } else {
                response = responseResolver.getResponse(request);
            }
        } catch (WebUserAuthException thrownByAuthentication) {
            FailReason failReason = thrownByAuthentication.getFailReason();
            if (failReason == FailReason.USER_PASS_MISMATCH) {
                bruteForceGuard.increaseAttemptCountOnFailedLogin(accessor);
            }
            if (failReason == FailReason.EXPIRED_COOKIE) {
                response = Response.builder()
                        .redirectTo("/login")
                        .setHeader("Set-Cookie", "auth=expired; Path=/; Max-Age=1")
                        .build();
            } else {
                response = responseFactory.redirectResponse("/login?from=" + exchange.getRequestURI().toASCIIString());
            }
        }

        if (bruteForceGuard.shouldPreventRequest(accessor)) {
            response = responseFactory.failedLoginAttempts403();
        }
        if (response.getCode() != 401 // Not failed
                && response.getCode() != 403 // Not blocked
                && (request != null && request.getUser().isPresent()) // Logged in
        ) {
            bruteForceGuard.resetAttemptCount(accessor);
        }
        return response;
    }

    private Request buildRequest(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();
        URIPath path = new URIPath(exchange.getRequestURI().getPath());
        URIQuery query = new URIQuery(exchange.getRequestURI().getQuery());
        WebUser user = getWebUser(exchange);
        Map<String, String> headers = getRequestHeaders(exchange);
        return new Request(requestMethod, path, query, user, headers);
    }

    private WebUser getWebUser(HttpExchange exchange) {
        return getAuthentication(exchange.getRequestHeaders())
                .map(Authentication::getWebUser) // Can throw WebUserAuthException
                .map(com.djrapitops.plan.delivery.domain.WebUser::toNewWebUser)
                .orElse(null);
    }

    private Map<String, String> getRequestHeaders(HttpExchange exchange) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> e : exchange.getResponseHeaders().entrySet()) {
            List<String> value = e.getValue();
            headers.put(e.getKey(), new TextStringBuilder().appendWithSeparators(value, ";").build());
        }
        return headers;
    }

    private Optional<Authentication> getAuthentication(Headers requestHeaders) {
        if (config.isTrue(WebserverSettings.DISABLED_AUTHENTICATION)) {
            return Optional.empty();
        }

        List<String> cookies = requestHeaders.get("Cookie");
        if (cookies != null && !cookies.isEmpty()) {
            for (String cookie : new TextStringBuilder().appendWithSeparators(cookies, ";").build().split(";")) {
                String[] split = cookie.trim().split("=", 2);
                System.out.println(Arrays.toString(split));
                String name = split[0];
                String value = split[1];
                if ("auth".equals(name)) {
                    if (!ActiveCookieStore.checkCookie(value).isPresent()) {
                        throw new WebUserAuthException(FailReason.EXPIRED_COOKIE);
                    }
                    return Optional.of(new CookieAuthentication(value));
                }
            }
        }

        List<String> authorization = requestHeaders.get("Authorization");
        if (Verify.isEmpty(authorization)) return Optional.empty();

        String authLine = authorization.get(0);
        if (StringUtils.contains(authLine, "Basic ")) {
            return Optional.of(new BasicAuthentication(StringUtils.split(authLine, ' ')[1], dbSystem.getDatabase()));
        }
        return Optional.empty();
    }

    public ResponseResolver getResponseResolver() {
        return responseResolver;
    }
}
