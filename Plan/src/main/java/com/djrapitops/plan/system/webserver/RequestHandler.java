/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.database.databases.Database;
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
    private final Database database;
    private final ResponseHandler responseHandler;
    private final Timings timings;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    RequestHandler(
            Locale locale,
            PlanConfig config,
            Theme theme,
            Database database,
            ResponseHandler responseHandler,
            Timings timings,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;
        this.config = config;
        this.theme = theme;
        this.database = database;
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
                logger.debug(requestString + " Response code: " + responseCode + timings.end(requestString).map(Benchmark::toString).orElse("-"));
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
            return new BasicAuthentication(authLine.split(" ")[1], database);
        }
        return null;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
