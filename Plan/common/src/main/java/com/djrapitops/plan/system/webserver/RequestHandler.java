/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.auth.BasicAuthentication;
import com.djrapitops.plan.system.webserver.response.PromptAuthorizationResponse;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.benchmarking.Benchmark;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

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
    private final ResponseHandler responseHandler;
    private final Timings timings;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    RequestHandler(
            Locale locale,
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            ResponseHandler responseHandler,
            Timings timings,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.responseHandler = responseHandler;
        this.timings = timings;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void handle(HttpExchange exchange) {
        Headers requestHeaders = exchange.getRequestHeaders();
        Headers responseHeaders = exchange.getResponseHeaders();
        Request request = new Request(exchange, locale);
        request.setAuth(getAuthorization(requestHeaders));

        String requestString = request.toString();
        timings.start(requestString);
        int responseCode = -1;

        boolean inDevMode = config.isTrue(Settings.DEV_MODE);
        try {
            Response response = responseHandler.getResponse(request);
            responseCode = response.getCode();
            if (response instanceof PromptAuthorizationResponse) {
                responseHeaders.set("WWW-Authenticate", "Basic realm=\"/\"");
            }

            response.setResponseHeaders(responseHeaders);
            response.send(exchange, locale, theme);
        } catch (Exception e) {
            if (inDevMode) {
                logger.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        } finally {
            exchange.close();
            if (inDevMode) {
                logger.getDebugLogger().logOn(
                        DebugChannels.WEB_REQUESTS,
                        timings.end(requestString).map(Benchmark::toString).orElse("-") + " Code: " + responseCode
                );
            }
        }
    }

    private Authentication getAuthorization(Headers requestHeaders) {
        List<String> authorization = requestHeaders.get("Authorization");
        if (Verify.isEmpty(authorization)) {
            return null;
        }

        String authLine = authorization.get(0);
        if (authLine.contains("Basic ")) {
            return new BasicAuthentication(authLine.split(" ")[1], dbSystem.getDatabase());
        }
        return null;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
