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

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.rendering.json.network.NetworkTabJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.AsyncJSONResolverService;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.identification.Identifiers;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Functional interface wrapper for resolving network JSON directly from other methods.
 *
 * @author AuroraLS3
 */
public class NetworkTabJSONResolver<T> extends JSONResolver {

    private final DataID dataID;
    private final WebPermission permission;
    private final Supplier<T> jsonCreator;
    private final AsyncJSONResolverService asyncJSONResolverService;

    public NetworkTabJSONResolver(
            DataID dataID, WebPermission permission, NetworkTabJSONCreator<T> jsonCreator,
            AsyncJSONResolverService asyncJSONResolverService
    ) {
        this.dataID = dataID;
        this.permission = permission;
        this.jsonCreator = jsonCreator;
        this.asyncJSONResolverService = asyncJSONResolverService;
    }

    @Override
    public Formatter<Long> getHttpLastModifiedFormatter() {return asyncJSONResolverService.getHttpLastModifiedFormatter();}

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().orElse(new WebUser("")).hasPermission(permission);
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        JSONStorage.StoredJSON json = asyncJSONResolverService.resolve(Identifiers.getTimestamp(request), dataID, jsonCreator);
        return getCachedOrNewResponse(request, json);
    }
}