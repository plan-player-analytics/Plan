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

import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Callback;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

@Singleton
public class JettyRequestHandler extends Handler.Abstract {

    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;
    private final Addresses addresses;
    private final RequestHandler requestHandler;
    private final Processing processing;
    private final ErrorLogger errorLogger;

    @Inject
    public JettyRequestHandler(WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor, Addresses addresses, RequestHandler requestHandler, Processing processing, ErrorLogger errorLogger) {
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
        this.addresses = addresses;
        this.requestHandler = requestHandler;
        this.processing = processing;
        this.errorLogger = errorLogger;
    }

    @Override
    public boolean handle(Request request, org.eclipse.jetty.server.Response jettyResponse, Callback callback) throws Exception {
        try {
            InternalRequest internalRequest = new JettyInternalRequest(request, webserverConfiguration, authenticationExtractor);
            CompletableFuture.supplyAsync(() -> requestHandler.getResponse(internalRequest), processing.getNonCriticalExecutor())
                    .thenCompose(Function.identity())
                    .thenApply(response -> new JettyResponseSender(response, request, jettyResponse, addresses))
                    .thenCompose(JettyResponseSender::sendAsync)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            logError(request, throwable);
                            callback.failed(throwable);
                        } else {
                            callback.succeeded();
                        }
                    });
            return true;
        } catch (Exception e) {
            logError(request, e);
            callback.failed(e);
            throw e;
        }
    }

    private void logError(Request request, Throwable e) {
        if (e instanceof CompletionException) {
            logError(request, e.getCause());
            return;
        }
        if (e instanceof EofException) {
            return; // EOF is quite common and ignored.
        }
        errorLogger.warn(e, ErrorContext.builder()
                .related(request.getMethod(), Request.getRemoteAddr(request), request.getHttpURI().getPath())
                .build());
    }
}
