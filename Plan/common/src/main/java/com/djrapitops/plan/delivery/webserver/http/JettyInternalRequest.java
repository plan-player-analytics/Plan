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
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.text.TextStringBuilder;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Request;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JettyInternalRequest implements InternalRequest {

    private final Request baseRequest;
    private final HttpServletRequest request;
    private final WebserverConfiguration webserverConfiguration;
    private final AuthenticationExtractor authenticationExtractor;

    public JettyInternalRequest(Request baseRequest, HttpServletRequest request, WebserverConfiguration webserverConfiguration, AuthenticationExtractor authenticationExtractor) {
        this.baseRequest = baseRequest;
        this.request = request;
        this.webserverConfiguration = webserverConfiguration;
        this.authenticationExtractor = authenticationExtractor;
    }

    @Override
    public long getTimestamp() {return baseRequest.getTimeStamp();}

    @Override
    public String getMethod() {return baseRequest.getMethod();}

    @Override
    public String getAccessAddressFromSocketIp() {
        return baseRequest.getRemoteAddr();
    }

    @Override
    public String getAccessAddressFromHeader() {
        return baseRequest.getHeader(HttpHeader.X_FORWARDED_FOR.asString());
    }

    @Override
    public com.djrapitops.plan.delivery.web.resolver.request.Request toRequest(@Untrusted String accessAddress) {
        String requestMethod = baseRequest.getMethod();
        @Untrusted URIPath path = new URIPath(baseRequest.getHttpURI().getDecodedPath());
        @Untrusted URIQuery query = new URIQuery(baseRequest.getHttpURI().getQuery());
        @Untrusted byte[] requestBody = readRequestBody();
        WebUser user = getWebUser(webserverConfiguration, authenticationExtractor, accessAddress);
        @Untrusted Map<String, String> headers = getRequestHeaders();
        return new com.djrapitops.plan.delivery.web.resolver.request.Request(requestMethod, path, query, user, headers, requestBody, accessAddress);
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return streamHeaderNames()
                .collect(Collectors.toMap(Function.identity(), baseRequest::getHeader,
                        (one, two) -> one + ';' + two));
    }

    private Stream<String> streamHeaderNames() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(baseRequest.getHeaderNames().asIterator(), 0), false);
    }

    private byte[] readRequestBody() {
        try (BufferedReader reader = request.getReader();
             ByteArrayOutputStream buf = new ByteArrayOutputStream(512)) {
            int b;
            while ((b = reader.read()) != -1) {
                buf.write((byte) b);
            }
            return buf.toByteArray();
        } catch (IOException ignored) {
            // requestBody stays empty
            return new byte[0];
        }
    }

    @Override
    public List<Cookie> getCookies() {
        @Untrusted List<String> textCookies = getCookieHeaders();
        List<Cookie> cookies = new ArrayList<>();
        if (!textCookies.isEmpty()) {
            String[] separated = new TextStringBuilder().appendWithSeparators(textCookies, ";").get().split(";");
            for (String textCookie : separated) {
                cookies.add(new Cookie(textCookie.trim()));
            }
        }
        return cookies;
    }

    private List<String> getCookieHeaders() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(request.getHeaders(HttpHeader.COOKIE.asString()).asIterator(), 0), false)
                .collect(Collectors.toList());
    }

    @Override
    public String getRequestedURIString() {
        return baseRequest.getRequestURI();
    }

    @Override
    public String getRequestedPathAndQuery() {
        return baseRequest.getHttpURI().getDecodedPath() + baseRequest.getHttpURI().getQuery();
    }

    @Override
    public String toString() {
        return "JettyInternalRequest{" +
                "baseRequest=" + baseRequest +
                ", request=" + request +
                ", webserverConfiguration=" + webserverConfiguration +
                ", authenticationExtractor=" + authenticationExtractor +
                '}';
    }
}
