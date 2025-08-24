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
package com.djrapitops.plan.delivery.web.resolver.request;

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Represents a HTTP request to use with {@link Resolver}.
 *
 * @author AuroraLS3
 */
public final class Request {

    private final String method;
    private final URIPath path;
    private final URIQuery query;
    private final WebUser user;
    private final Map<String, String> headers;
    private final byte[] requestBody;
    private final String accessIpAddress;

    /**
     * Constructor.
     *
     * @param method      HTTP method, GET, PUT, POST, etc
     * @param path        Requested path /example/target
     * @param query       Request parameters ?param=value etc
     * @param user        Web user doing the request (if authenticated)
     * @param headers     Request headers <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers">Documentation</a>
     * @param requestBody Raw body as bytes, if present
     * @deprecated Use newer constructor with IP address.
     */
    @Deprecated
    public Request(String method, URIPath path, URIQuery query, WebUser user, Map<String, String> headers, byte[] requestBody) {
        this(method, path, query, user, headers, requestBody, null);
    }

    /**
     * Constructor.
     *
     * @param method          HTTP method, GET, PUT, POST, etc
     * @param path            Requested path /example/target
     * @param query           Request parameters ?param=value etc
     * @param user            Web user doing the request (if authenticated)
     * @param headers         Request headers <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers">Documentation</a>
     * @param requestBody     Raw body as bytes, if present
     * @param accessIpAddress IP address this request is coming from.
     */
    public Request(String method, URIPath path, URIQuery query, WebUser user, Map<String, String> headers, byte[] requestBody, String accessIpAddress) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.user = user;
        this.headers = headers;
        this.requestBody = requestBody;
        this.accessIpAddress = accessIpAddress;
    }

    /**
     * Special constructor that figures out URIPath and URIQuery from "/path/and?query=params" and has no request body.
     *
     * @param method  HTTP request method
     * @param target  The requested path and query, e.g. "/path/and?query=params"
     * @param user    User that made the request
     * @param headers HTTP request headers
     * @deprecated Use newer constructor with IP address.
     */
    @Deprecated
    public Request(String method, String target, WebUser user, Map<String, String> headers) {
        this(method, target, user, headers, null);
    }

    /**
     * Special constructor that figures out URIPath and URIQuery from "/path/and?query=params" and has no request body.
     *
     * @param method          HTTP request method
     * @param target          The requested path and query, e.g. "/path/and?query=params"
     * @param user            User that made the request
     * @param headers         HTTP request headers
     * @param accessIpAddress IP address this request is coming from.
     */
    public Request(String method, String target, WebUser user, Map<String, String> headers, String accessIpAddress) {
        this.method = method;
        if (target.contains("?")) {
            String[] halves = StringUtils.split(target, "?", 2);
            this.path = new URIPath(halves[0]);
            this.query = new URIQuery(halves[1]);
        } else {
            this.path = new URIPath(target);
            this.query = new URIQuery("");
        }
        this.user = user;
        this.headers = headers;
        this.requestBody = new byte[0];
        this.accessIpAddress = accessIpAddress;
    }

    /**
     * Get HTTP method.
     *
     * @return GET, PUT, POST, etc
     */
    public String getMethod() {
        return method;
    }

    /**
     * Get the Requested path.
     *
     * @return {@link URIPath}.
     */
    public URIPath getPath() {
        return path;
    }

    /**
     * Get the Request parameters.
     *
     * @return {@link URIQuery}.
     */
    public URIQuery getQuery() {
        return query;
    }

    /**
     * Get the raw body, if present.
     *
     * @return byte[].
     */
    public byte[] getRequestBody() {
        return requestBody;
    }

    /**
     * Get the user making the request.
     *
     * @return the user if authentication is enabled
     */
    public Optional<WebUser> getUser() {
        return Optional.ofNullable(user);
    }

    /**
     * Get a header in the request.
     *
     * @param key <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers">Documentation</a>
     * @return Value if it is present in the request.
     */
    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(headers.get(key));
    }

    public Request omitFirstInPath() {
        return new Request(method, path.omitFirst(), query, user, headers, requestBody, accessIpAddress);
    }

    public String getAccessIpAddress() {
        return accessIpAddress;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path=" + path +
                ", query=" + query +
                ", user=" + user +
                ", headers=" + headers +
                ", body=" + requestBody.length +
                '}';
    }
}
