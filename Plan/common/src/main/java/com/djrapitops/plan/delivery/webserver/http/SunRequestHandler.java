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
package com.djrapitops.plan.delivery.webserver.http;

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.PassBruteForceGuard;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.ResponseResolver;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;

/**
 * HttpHandler for WebServer request management.
 *
 * @author AuroraLS3
 */
@Singleton
public class SunRequestHandler implements HttpHandler {

    private final Locale locale;
    private final PlanConfig config;
    private final Addresses addresses;
    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;
    private final ResponseResolver responseResolver;
    private final ResponseFactory responseFactory;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    private final PassBruteForceGuard bruteForceGuard;
    private List<String> ipWhitelist = null;

    @Inject
    SunRequestHandler(
            Locale locale,
            PlanConfig config,
            Addresses addresses,
            WebserverConfiguration webserverConfiguration,
            AuthenticationExtractor authenticationExtractor,
            ResponseResolver responseResolver,
            ResponseFactory responseFactory,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.config = config;
        this.addresses = addresses;
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
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
            SunResponseSender sender = new SunResponseSender(addresses, exchange, response);
            sender.send();
        } catch (Exception e) {
            if (config.isTrue(PluginSettings.DEV_MODE)) {
                logger.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                errorLogger.warn(e, ErrorContext.builder()
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

        SunInternalRequest internalRequest = new SunInternalRequest(exchange, webserverConfiguration, authenticationExtractor);

        String accessor = internalRequest.getAccessAddress();
        Request request = null;
        Response response;
        try {
            request = internalRequest.toRequest();

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
                String directTo = StringUtils.startsWithAny(from, "/auth/", "/login") ? "/login" : "/login?from=." + from;
                response = Response.builder()
                        .redirectTo(directTo)
                        .setHeader("Set-Cookie", "auth=expired; Path=/; Max-Age=1; SameSite=Lax; Secure;")
                        .build();
            }
        }

        if (bruteForceGuard.shouldPreventRequest(accessor)) {
            response = responseFactory.failedLoginAttempts403();
        }
        if (response.getCode() != 401 // Not failed
                && response.getCode() != 403 // Not blocked
                && request != null && request.getUser().isPresent() // Logged in
        ) {
            bruteForceGuard.resetAttemptCount(accessor);
        }
        return response;
    }

    public ResponseResolver getResponseResolver() {
        return responseResolver;
    }
}
