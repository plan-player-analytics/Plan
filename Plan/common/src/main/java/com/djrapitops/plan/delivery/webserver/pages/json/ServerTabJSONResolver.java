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

import com.djrapitops.plan.delivery.rendering.json.ServerTabJSONCreator;
import com.djrapitops.plan.delivery.webserver.RequestInternal;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.delivery.webserver.pages.PageResolver;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Identifiers;

import java.util.UUID;
import java.util.function.Function;

/**
 * Functional interface wrapper for resolving server specific JSON directly from other methods.
 *
 * @author Rsl1122
 */
public class ServerTabJSONResolver<T> implements PageResolver {

    private final DataID dataID;
    private final Identifiers identifiers;
    private final Function<UUID, T> jsonCreator;

    public ServerTabJSONResolver(
            DataID dataID,
            Identifiers identifiers,
            ServerTabJSONCreator<T> jsonCreator
    ) {
        this.dataID = dataID;
        this.identifiers = identifiers;
        this.jsonCreator = jsonCreator;
    }

    @Override
    public Response_old resolve(RequestInternal request, RequestTarget target) throws WebException {
        UUID serverUUID = identifiers.getServerUUID(target); // Can throw BadRequestException
        return JSONCache.getOrCache(dataID, serverUUID, () -> new JSONResponse(jsonCreator.apply(serverUUID)));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}