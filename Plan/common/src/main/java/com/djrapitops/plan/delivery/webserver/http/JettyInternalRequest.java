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

import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.auth.AuthenticationExtractor;
import com.djrapitops.plan.delivery.webserver.auth.Cookie;
import com.djrapitops.plan.delivery.webserver.configuration.WebserverConfiguration;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JettyInternalRequest implements InternalRequest {

    private final Request request;
    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;

    public JettyInternalRequest(Request request, WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor) {
        this.request = request;
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
    }

    @Override
    public long getTimestamp() {return Request.getTimeStamp(request);}

    @Override
    public String getMethod() {return request.getMethod();}

    @Override
    public String getAccessAddressFromSocketIp() {
        return Request.getRemoteAddr(request);
    }

    @Override
    public String getAccessAddressFromHeader() {
        String header = getHeader(HttpHeader.X_FORWARDED_FOR);
        if (header != null && header.contains(",")) {
            return header.split(",")[0].trim();
        }
        return header;
    }

    private String getHeader(HttpHeader headerName) {
        return request.getHeaders().get(headerName);
    }

    @Override
    public com.djrapitops.plan.delivery.web.resolver.request.Request toRequest(@Untrusted String accessAddress) {
        String requestMethod = request.getMethod();
        @Untrusted URIPath path = new URIPath(request.getHttpURI().getDecodedPath());
        @Untrusted URIQuery query = new URIQuery(request.getHttpURI().getQuery());
        CompletableFuture<byte[]> requestBody = readRequestBodyAsync();
        WebUser user = getWebUser(webserverConfiguration, authenticationExtractor, accessAddress);
        @Untrusted Map<String, String> headers = getRequestHeaders();
        return new com.djrapitops.plan.delivery.web.resolver.request.Request(requestMethod, path, query, user, headers, requestBody, accessAddress);
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return request.getHeaders().stream()
                .collect(Collectors.toMap(HttpField::getName, HttpField::getValue,
                        (one, two) -> one + ';' + two));
    }

    private CompletableFuture<byte[]> readRequestBodyAsync() {
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        Content.Source.asByteArrayAsync(request, Integer.MAX_VALUE, new Promise.Invocable<>() {
            @Override
            public void succeeded(byte[] result) {
                future.complete(result != null ? result : new byte[0]);
            }

            @Override
            public void failed(Throwable x) {
                future.complete(new byte[0]);
            }
        });
        return future;
    }

    @Override
    public List<Cookie> getCookies() {
        @Untrusted List<HttpCookie> jettyCookies = Request.getCookies(request);
        List<Cookie> cookies = new ArrayList<>();
        if (!jettyCookies.isEmpty()) {
            for (HttpCookie cookie : jettyCookies) {
                cookies.add(new Cookie(cookie.getName(), cookie.getValue()));
            }
        }
        return cookies;
    }

    @Override
    public String getRequestedURIString() {
        return request.getHttpURI().getPath();
    }

    @Override
    public String getRequestedPathAndQuery() {
        return request.getHttpURI().getDecodedPath() + request.getHttpURI().getQuery();
    }

    @Override
    public String toString() {
        return "JettyInternalRequest{" +
                "request=" + request +
                ", webserverConfiguration=" + webserverConfiguration +
                ", authenticationExtractor=" + authenticationExtractor +
                '}';
    }
}
