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
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.ResponseResolver;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * HttpHandler for WebServer request management.
 *
 * @author AuroraLS3
 */
@Singleton
public class SunRequestHandler implements HttpHandler {

    private final PlanConfig config;
    private final Addresses addresses;
    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;
    private final ResponseResolver responseResolver;
    private final RequestHandler requestHandler;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public SunRequestHandler(
            PlanConfig config,
            Addresses addresses,
            WebserverConfiguration webserverConfiguration,
            AuthenticationExtractor authenticationExtractor,
            ResponseResolver responseResolver,
            RequestHandler requestHandler,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.addresses = addresses;
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
        this.responseResolver = responseResolver;
        this.requestHandler = requestHandler;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            InternalRequest internalRequest = new SunInternalRequest(exchange, webserverConfiguration, authenticationExtractor);
            Response response = requestHandler.getResponse(internalRequest);
            new SunResponseSender(addresses, exchange, response).send();
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

    public ResponseResolver getResponseResolver() {
        return responseResolver;
    }
}
