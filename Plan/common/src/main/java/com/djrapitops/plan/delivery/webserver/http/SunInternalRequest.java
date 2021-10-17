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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.auth.Cookie;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.text.TextStringBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class SunInternalRequest implements InternalRequest {

    private final HttpExchange exchange;
    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;

    public SunInternalRequest(HttpExchange exchange, WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor) {
        this.exchange = exchange;
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
    }

    @Override
    public String getAccessAddress() {
        AccessAddressPolicy accessAddressPolicy = webserverConfiguration.getAccessAddressPolicy();
        if (accessAddressPolicy == AccessAddressPolicy.X_FORWARDED_FOR_HEADER) {
            String fromHeader = getAccessAddressFromHeader();
            if (fromHeader == null) {
                webserverConfiguration.getInvalidConfigurationWarnings().warnAboutXForwardedForSecurityIssue();
                return getAccessAddressFromSocketIp();
            } else {
                return fromHeader;
            }
        }
        return getAccessAddressFromSocketIp();
    }

    private String getAccessAddressFromSocketIp() {
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }

    private String getAccessAddressFromHeader() {
        return exchange.getRequestHeaders().getFirst("X-Forwarded-For");
    }

    @Override
    public Request toRequest() {
        return buildRequest(exchange);
    }

    private Request buildRequest(HttpExchange exchange) {
        String requestMethod = exchange.getRequestMethod();
        URIPath path = new URIPath(exchange.getRequestURI().getPath());
        URIQuery query = new URIQuery(exchange.getRequestURI().getRawQuery());
        byte[] requestBody = readRequestBody(exchange);
        WebUser user = getWebUser();
        Map<String, String> headers = getRequestHeaders(exchange);
        return new Request(requestMethod, path, query, user, headers, requestBody);
    }

    private byte[] readRequestBody(HttpExchange exchange) {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream(512)) {
            int b;
            while ((b = exchange.getRequestBody().read()) != -1) {
                buf.write((byte) b);
            }
            return buf.toByteArray();
        } catch (IOException ignored) {
            // requestBody stays empty
            return new byte[0];
        }
    }

    private WebUser getWebUser() {
        return getAuthentication()
                .map(Authentication::getUser) // Can throw WebUserAuthException
                .map(User::toWebUser)
                .orElse(null);
    }

    private Map<String, String> getRequestHeaders(HttpExchange exchange) {
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> e : exchange.getRequestHeaders().entrySet()) {
            List<String> value = e.getValue();
            headers.put(e.getKey(), new TextStringBuilder().appendWithSeparators(value, ";").build());
        }
        return headers;
    }

    @Override
    public List<Cookie> getCookies() {
        List<String> textCookies = exchange.getRequestHeaders().get("Cookie");
        List<Cookie> cookies = new ArrayList<>();
        if (textCookies != null && !textCookies.isEmpty()) {
            String[] separated = new TextStringBuilder().appendWithSeparators(textCookies, ";").build().split(";");
            for (String textCookie : separated) {
                cookies.add(new Cookie(textCookie));
            }
        }
        return cookies;
    }

    private Optional<Authentication> getAuthentication() {
        if (webserverConfiguration.isAuthenticationDisabled()) {
            return Optional.empty();
        }
        return authenticationExtractor.extractAuthentication(this);
    }
}
