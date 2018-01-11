/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.util.List;

/**
 * Represents a HttpExchange Request.
 * <p>
 * Automatically gets the Basic Auth from headers.
 *
 * @author Rsl1122
 */
public class Request {
    private String auth;
    private final String requestMethod;
    private final String target;

    private final HttpExchange exchange;

    public Request(HttpExchange exchange) {
        this.requestMethod = exchange.getRequestMethod();
        this.target = exchange.getRequestURI().toString();

        this.exchange = exchange;
        setAuth(exchange.getRequestHeaders());
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(Headers requestHeaders) {
        List<String> authorization = requestHeaders.get("Authorization");
        if (Verify.isEmpty(authorization)) {
            return;
        }

        String authLine = authorization.get(0);
        if (authLine.contains("Basic ")) {
            auth = authLine.split(" ")[1];
        }
    }

    public boolean hasAuth() {
        return auth != null;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getTarget() {
        return target;
    }

    public boolean isAPIRequest() {
        return "POST".equals(requestMethod);
    }

    public InputStream getRequestBody() {
        return exchange.getRequestBody();
    }

    @Override
    public String toString() {
        return "Request:" + requestMethod + " " + target;
    }
}