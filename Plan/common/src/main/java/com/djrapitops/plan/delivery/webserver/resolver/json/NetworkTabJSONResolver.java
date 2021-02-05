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

import com.djrapitops.plan.delivery.rendering.json.network.NetworkTabJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.DataID;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Functional interface wrapper for resolving network JSON directly from other methods.
 *
 * @author Rsl1122
 */
public class NetworkTabJSONResolver<T> implements Resolver {

    private final DataID dataID;
    private final Supplier<T> jsonCreator;
    private final AsyncJSONResolverService asyncJSONResolverService;

    public NetworkTabJSONResolver(
            DataID dataID, NetworkTabJSONCreator<T> jsonCreator,
            AsyncJSONResolverService asyncJSONResolverService
    ) {
        this.dataID = dataID;
        this.jsonCreator = jsonCreator;
        this.asyncJSONResolverService = asyncJSONResolverService;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().orElse(new WebUser("")).hasPermission("page.network");
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(asyncJSONResolverService.resolve(getTimestamp(request), dataID, jsonCreator).json)
                .build();
    }

    private long getTimestamp(Request request) {
        try {
            return request.getQuery().get("timestamp")
                    .map(Long::parseLong)
                    .orElseGet(System::currentTimeMillis);
        } catch (NumberFormatException nonNumberTimestamp) {
            throw new BadRequestException("'timestamp' was not a number: " + nonNumberTimestamp.getMessage());
        }
    }
}