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
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreRequestTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionException;

@Singleton
public class AccessLogger {

    private final WebserverConfiguration webserverConfiguration;
    private final DBSystem dbSystem;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public AccessLogger(WebserverConfiguration webserverConfiguration, DBSystem dbSystem, PluginLogger logger, ErrorLogger errorLogger) {
        this.webserverConfiguration = webserverConfiguration;
        this.dbSystem = dbSystem;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    public void log(@Untrusted InternalRequest internalRequest, @Untrusted Request request, Response response) {
        if (webserverConfiguration.logAccessToConsole()) {
            int code = response.getCode();
            @Untrusted String message = "Access Log: " + internalRequest.getMethod() + " " +
                    getRequestURI(internalRequest, request) +
                    " (from " + internalRequest.getAccessAddress(webserverConfiguration) + ") - " +
                    code;
            if (webserverConfiguration.isDevMode()) {
                message += " Request Headers" + internalRequest.getRequestHeaders();
            }

            int codeFamily = code - (code % 100); // 5XX, 4XX etc
            switch (codeFamily) {
                case 500:
                    logger.error(message);
                    break;
                case 400:
                    logger.warn(message);
                    break;
                case 300:
                case 200:
                case 100:
                default:
                    logger.info(message);
                    break;
            }
        }
        try {
            long timestamp = internalRequest.getTimestamp();
            String accessAddress = internalRequest.getAccessAddress(webserverConfiguration);
            String method = internalRequest.getMethod();
            method = method != null ? method : "?";
            String url = StoreRequestTransaction.getTruncatedURI(request, internalRequest);
            int responseCode = response.getCode();
            dbSystem.getDatabase().executeTransaction(
                    new StoreRequestTransaction(timestamp, accessAddress, method, url, responseCode)
            );
        } catch (CompletionException | DBOpException e) {
            errorLogger.warn(e, ErrorContext.builder()
                    .related("Logging request failed")
                    .related(getRequestURI(internalRequest, request))
                    .build());
        }
    }

    @Untrusted
    private String getRequestURI(InternalRequest internalRequest, Request request) {
        return request != null ? request.getPath().asString() + request.getQuery().asString()
                : internalRequest.getRequestedURIString();
    }
}
