/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.util.Optional;

/**
 * Represents a HttpExchange Request.
 * <p>
 * Automatically gets the Basic Auth from headers.
 *
 * @author Rsl1122
 */
public class Request {
    private final String requestMethod;
    private final String target;
    private final HttpExchange exchange;
    private final String remoteAddress;
    private Authentication auth;

    public Request(HttpExchange exchange) {
        this.requestMethod = exchange.getRequestMethod();
        this.target = exchange.getRequestURI().toString();

        remoteAddress = exchange.getRemoteAddress().getAddress().getHostAddress();

        this.exchange = exchange;
    }

    public Optional<Authentication> getAuth() {
        return Optional.ofNullable(auth);
    }

    public void setAuth(Authentication authentication) {
        auth = authentication;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getTarget() {
        return target;
    }

    public InputStream getRequestBody() {
        return exchange.getRequestBody();
    }

    @Override
    public String toString() {
        return "Request:" + requestMethod + " " + target;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
