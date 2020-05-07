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
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;

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

    public NetworkTabJSONResolver(DataID dataID, NetworkTabJSONCreator<T> jsonCreator) {
        this.dataID = dataID;
        this.jsonCreator = jsonCreator;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().orElse(new WebUser("")).hasPermission("page.network");
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return JSONCache.getOrCache(dataID, jsonCreator);
    }

}