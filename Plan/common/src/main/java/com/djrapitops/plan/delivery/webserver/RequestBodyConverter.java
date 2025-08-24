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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.nio.charset.StandardCharsets;

public class RequestBodyConverter {

    private RequestBodyConverter() {
        /* Static utility class */
    }

    /**
     * Get the body of a request as an url-encoded form.
     *
     * @return {@link URIQuery}.
     */
    public static URIQuery formBody(Request request) {
        return new URIQuery(new String(request.getRequestBody(), StandardCharsets.UTF_8));
    }

    public static <T> T bodyJson(@Untrusted Request request, Gson gson, Class<T> ofType) {
        return gson.fromJson(new String(request.getRequestBody(), StandardCharsets.UTF_8), ofType);
    }

    public static <T> T bodyJson(Request request, Gson gson, TypeToken<T> ofType) {
        return gson.fromJson(new String(request.getRequestBody(), StandardCharsets.UTF_8), ofType.getType());
    }
}
