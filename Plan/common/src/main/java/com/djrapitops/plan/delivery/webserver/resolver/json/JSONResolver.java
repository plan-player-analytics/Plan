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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.CacheStrategy;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;
import org.eclipse.jetty.http.HttpHeader;

import java.util.Optional;

/**
 * @author AuroraLS3
 */
public abstract class JSONResolver implements Resolver {

    protected Response getCachedOrNewResponse(@Untrusted Request request, JSONStorage.StoredJSON storedJSON) {
        if (storedJSON == null) {
            return Response.builder()
                    .setMimeType(MimeType.JSON)
                    .setJSONContent(Maps.builder(String.class, String.class)
                            .put("error", "Json failed to generate for some reason, see /Plan/logs for errors")
                            .build())
                    .build();
        }

        Optional<Long> browserCached = Identifiers.getEtag(request);
        if (browserCached.isPresent() && browserCached.get() == storedJSON.getTimestamp()) {
            return Response.builder()
                    .setStatus(304)
                    .setContent(new byte[0])
                    .build();
        }

        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(storedJSON.getJson())
                .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG_USER_SPECIFIC)
                .setHeader(HttpHeader.LAST_MODIFIED.asString(), getHttpLastModifiedFormatter().apply(storedJSON.getTimestamp()))
                .setHeader(HttpHeader.ETAG.asString(), storedJSON.getTimestamp())
                .build();
    }

    protected abstract Formatter<Long> getHttpLastModifiedFormatter();

}
