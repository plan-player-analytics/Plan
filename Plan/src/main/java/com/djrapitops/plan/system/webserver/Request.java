/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plugin.utilities.Verify;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Represents a HttpExchange Request.
 * <p>
 * Automatically gets the Basic Auth from headers.
 *
 * @author Rsl1122
 */
public class Request {
    private Authentication auth;
    private final String requestMethod;
    private final String target;

    private final HttpExchange exchange;

    public Request(HttpExchange exchange) {
        this.requestMethod = exchange.getRequestMethod();
        this.target = exchange.getRequestURI().toString();

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