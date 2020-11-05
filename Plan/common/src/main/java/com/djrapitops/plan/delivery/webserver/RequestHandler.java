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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.auth.BasicAuthentication;
import com.djrapitops.plan.delivery.webserver.auth.CookieAuthentication;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
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

    private final Locale locale;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Addresses addresses;
    private final ResponseResolver responseResolver;
    private final ResponseFactory responseFactory;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private final PassBruteForceGuard bruteForceGuard;
    private List<String> ipWhitelist = null;

    @Inject
    RequestHandler(
            Locale locale,
            PlanConfig config,
            DBSystem dbSystem,
            Addresses addresses,
            ResponseResolver responseResolver,
            ResponseFactory responseFactory,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.config = config;
        this.dbSystem = dbSystem;
        this.addresses = addresses;
        this.responseResolver = responseResolver;
        this.responseFactory = responseFactory;
        this.logger = logger;
        this.errorLogger = errorLogger;

        bruteForceGuard = new PassBruteForceGuard();
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            Response response = getResponse(exchange);
            response.getHeaders().putIfAbsent("Access-Control-Allow-Origin", config.get(WebserverSettings.CORS_ALLOW_ORIGIN));
            response.getHeaders().putIfAbsent("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.getHeaders().putIfAbsent("Access-Control-Allow-Credentials", "true");
            response.getHeaders().putIfAbsent("X-Robots-Tag", "noindex, nofollow");
            ResponseSender sender = new ResponseSender(addresses, exchange, response);
            sender.send();
        } catch (Exception e) {
            if (config.isTrue(PluginSettings.DEV_MODE)) {
                logger.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                errorLogger.log(L.WARN, e, ErrorContext.builder()
                        .whatToDo("THIS ERROR IS ONLY LOGGED IN DEV MODE")
                        .related(exchange.getRequestMethod(), exchange.getRemoteAddress(), exchange.getRequestHeaders(), exchange.getResponseHeaders(), exchange.getRequestURI())
                        .build());
            }
        } finally {
            exchange.close();
        }
    }

    public Response getResponse(HttpExchange exchange) {
        if (ipWhitelist == null) {
            ipWhitelist = config.isTrue(WebserverSettings.IP_WHITELIST)
                    ? config.get(WebserverSettings.WHITELIST)
                    : Collections.emptyList();
        }
        String accessor = exchange.getRemoteAddress().getAddress().getHostAddress();
        Request request = null;
        Response response;
        try {
            request = buildRequest(exchange);
            if (bruteForceGuard.shouldPreventRequest(accessor)) {
                response = responseFactory.failedLoginAttempts403();
            } else if (!ipWhitelist.isEmpty() && !ipWhitelist.contains(accessor)) {
                response = responseFactory.ipWhitelist403(accessor);
                logger.info(locale.getString(PluginLang.WEB_SERVER_NOTIFY_IP_WHITELIST_BLOCK, accessor, exchange.getRequestURI().toString()));
            } else {
                response = responseResolver.getResponse(request);
            }
        } catch (WebUserAuthException thrownByAuthentication) {
            FailReason failReason = thrownByAuthentication.getFailReason();
            if (failReason == FailReason.USER_PASS_MISMATCH) {
                bruteForceGuard.increaseAttemptCountOnFailedLogin(accessor);
                response = responseFactory.badRequest(failReason.getReason(), "/auth/login");
            } else {
                String from = exchange.getRequestURI().toASCIIString();
                response = Response.builder()
                        .redirectTo(StringUtils.startsWithAny(from, "/auth/", "/login") ? "/login" : "/login?from=." + from)
                        .setHeader("Set-Cookie", "auth=expired; Path=/; Max-Age=1; SameSite=Lax; Secure;")
                        .build();
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
        URIQuery query = new URIQuery(exchange.getRequestURI().getRawQuery());
        WebUser user = getWebUser(exchange);
        Map<String, String> headers = getRequestHeaders(exchange);
        return new Request(requestMethod, path, query, user, headers);
    }

    private WebUser getWebUser(HttpExchange exchange) {
        return getAuthentication(exchange.getRequestHeaders())
                .map(Authentication::getUser) // Can throw WebUserAuthException
                .map(User::toWebUser)
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
                String name = split[0];
                String value = split[1];
                if ("auth".equals(name)) {
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
