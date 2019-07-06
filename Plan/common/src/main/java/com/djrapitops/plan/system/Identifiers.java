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
package com.djrapitops.plan.system;

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.webserver.RequestTarget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility for getting server identifier from different sources.
 *
 * @author Rsl1122
 */
@Singleton
public class Identifiers {

    protected final DBSystem dbSystem;

    @Inject
    public Identifiers(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    public UUID getServerUUID(RequestTarget target) throws BadRequestException {
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
}