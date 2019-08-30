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

import com.djrapitops.plan.delivery.rendering.json.JSONFactory;
import com.djrapitops.plan.delivery.rendering.json.PlayersTableJSONParser;
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.delivery.webserver.pages.PageHandler;
import com.djrapitops.plan.delivery.webserver.response.Response;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * JSON handler for different Player table JSON requests.
 *
 * @author Rsl1122
 * @see PlayersTableJSONParser For JSON parsing of /server players table.
 */
@Singleton
public class PlayersTableJSONHandler implements PageHandler {

    private final Identifiers identifiers;
    private final JSONFactory jsonFactory;
    private final JSONCache cache;

    @Inject
    public PlayersTableJSONHandler(
            Identifiers identifiers,
            JSONFactory jsonFactory,
            JSONCache cache
    ) {
        this.identifiers = identifiers;
        this.jsonFactory = jsonFactory;
        this.cache = cache;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        if (target.getParameter("server").isPresent()) {
            UUID serverUUID = identifiers.getServerUUID(target); // Can throw BadRequestException
            return cache.getOrCache(DataID.PLAYERS, serverUUID, () -> new JSONResponse(jsonFactory.serverPlayersTableJSON(serverUUID)));
        }
        // Assume network
        return cache.getOrCache(DataID.PLAYERS, () -> new JSONResponse(jsonFactory.networkPlayersTableJSON()));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}