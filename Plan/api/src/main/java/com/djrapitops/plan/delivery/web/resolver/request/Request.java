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
 * @author Rsl1122
 */
public final class Request {

    private final String method;
    private final URIPath path;
    private final URIQuery query;
    private final WebUser user;
    private final Map<String, String> headers;

    /**
     * Constructor.
     *
     * @param method  HTTP method, GET, PUT, POST, etc
     * @param path    Requested path /example/target
     * @param query   Request parameters ?param=value etc
     * @param user    Web user doing the request (if authenticated)
     * @param headers Request headers https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers
     */
    public Request(String method, URIPath path, URIQuery query, WebUser user, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.user = user;
        this.headers = headers;
    }

    // Special constructor that figures out URIPath and URIQuery from "/path/and?query=params"
    public Request(String method, String target, WebUser user, Map<String, String> headers) {
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
     * @param key https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers
     * @return Value if it is present in the request.
     */
    public Optional<String> getHeader(String key) {
        return Optional.ofNullable(headers.get(key));
    }

    public Request omitFirstInPath() {
        return new Request(method, path.omitFirst(), query, user, headers);
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path=" + path +
                ", query=" + query +
                ", user=" + user +
                ", headers=" + headers +
                '}';
    }
}