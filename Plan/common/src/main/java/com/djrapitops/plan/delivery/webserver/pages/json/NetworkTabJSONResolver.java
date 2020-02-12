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
package com.djrapitops.plan.delivery.webserver.pages.json;

import com.djrapitops.plan.delivery.rendering.json.network.NetworkTabJSONCreator;
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.delivery.webserver.pages.PageResolver;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;

import java.util.function.Supplier;

/**
 * Functional interface wrapper for resolving network JSON directly from other methods.
 *
 * @author Rsl1122
 */
public class NetworkTabJSONResolver<T> implements PageResolver {

    private final DataID dataID;
    private final Supplier<T> jsonCreator;

    public NetworkTabJSONResolver(DataID dataID, NetworkTabJSONCreator<T> jsonCreator) {
        this.dataID = dataID;
        this.jsonCreator = jsonCreator;
    }

    @Override
    public Response_old resolve(Request request, RequestTarget target) {
        return JSONCache.getOrCache(dataID, () -> new JSONResponse(jsonCreator.get()));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}