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
package com.djrapitops.plan.system.webserver.pages.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.PageHandler;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.data.JSONResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * JSON handler for different Player table JSON requests.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.utilities.html.tables.PlayersTableJSONParser For JSON parsing of /server players table.
 */
@Singleton
public class PlayersTableJSONHandler implements PageHandler {

    private final DBSystem dbSystem;
    private final JSONFactory jsonFactory;

    @Inject
    public PlayersTableJSONHandler(
            DBSystem dbSystem,
            JSONFactory jsonFactory
    ) {
        this.jsonFactory = jsonFactory;
        this.dbSystem = dbSystem;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        UUID serverUUID = getServerUUID(target); // Can throw BadRequestException
        return new JSONResponse(jsonFactory.serverPlayersTableJSON(serverUUID));
    }

    private UUID getServerUUID(RequestTarget target) throws BadRequestException {
        Optional<String> serverUUID = target.getParameter("serverUUID");
        if (serverUUID.isPresent()) {
            return getServerUUIDDirectly(serverUUID.get());
        } else {
            return getServerUUIDFromName(target); // Preferred
        }
    }

    private UUID getServerUUIDFromName(RequestTarget target) throws BadRequestException {
        String serverName = target.getParameter("serverName")
                .orElseThrow(() -> new BadRequestException("'serverName' parameter was not defined."));
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                .map(Server::getUuid)
                .orElseThrow(() -> new BadRequestException("'serverName' was not found in the database.: '" + serverName + "'"));
    }

    private UUID getServerUUIDDirectly(String serverUUIDString) throws BadRequestException {
        try {
            return UUID.fromString(serverUUIDString);
        } catch (IllegalArgumentException malformedUUIDException) {
            throw new BadRequestException("'serverName' was not a valid UUID: " + malformedUUIDException.getMessage());
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}