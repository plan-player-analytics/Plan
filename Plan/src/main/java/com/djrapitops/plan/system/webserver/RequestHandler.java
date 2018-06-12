/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.auth.BasicAuthentication;
import com.djrapitops.plan.system.webserver.response.PromptAuthorizationResponse;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.List;

/**
 * HttpHandler for WebServer request management.
 *
 * @author Rsl1122
 */
public class RequestHandler implements HttpHandler {

    private final ResponseHandler responseHandler;

    RequestHandler(WebServer webServer) {
        responseHandler = new ResponseHandler(webServer);
    }

    @Override
    public void handle(HttpExchange exchange) {
        Headers requestHeaders = exchange.getRequestHeaders();
        Headers responseHeaders = exchange.getResponseHeaders();
        Request request = new Request(exchange);
        request.setAuth(getAuthorization(requestHeaders));

        String requestString = request.toString();
        Benchmark.start("", requestString);
        int responseCode = -1;
        try {
            Response response = responseHandler.getResponse(request);
            responseCode = response.getCode();
            if (response instanceof PromptAuthorizationResponse) {
                responseHeaders.set("WWW-Authenticate", "Basic realm=\"/\"");
            }

            response.setResponseHeaders(responseHeaders);
            response.send(exchange);
        } catch (Exception e) {
            if (Settings.DEV_MODE.isTrue()) {
                Log.warn("THIS ERROR IS ONLY LOGGED IN DEV MODE:");
                Log.toLog(this.getClass(), e);
            }
        } finally {
            exchange.close();
            if (Settings.DEV_MODE.isTrue()) {
                Log.debug(requestString + " Response code: " + responseCode + " took " + Benchmark.stop("", requestString) + " ms");
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
            return new BasicAuthentication(authLine.split(" ")[1]);
        }
        return null;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
