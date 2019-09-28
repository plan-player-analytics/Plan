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
package com.djrapitops.plan.identification;

import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.exceptions.connection.BadRequestException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
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
        String serverIndentifier = target.getParameter("server")
                .orElseThrow(() -> new BadRequestException("'server' parameter was not defined."));

        return UUIDUtility.parseFromString(serverIndentifier)
                .orElse(getServerUUIDFromName(serverIndentifier));
    }

    private UUID getServerUUIDFromName(String serverName) throws BadRequestException {
        return dbSystem.getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                .map(Server::getUuid)
                .orElseThrow(() -> new BadRequestException("Given 'server' was not found in the database: '" + serverName + "'"));
    }

    public UUID getPlayerUUID(RequestTarget target) throws BadRequestException {
        String playerIdentifier = target.getParameter("player")
                .orElseThrow(() -> new BadRequestException("'player' parameter was not defined."));

        return UUIDUtility.parseFromString(playerIdentifier)
                .orElse(getPlayerUUIDFromName(playerIdentifier));
    }

    private UUID getPlayerUUIDFromName(String playerName) throws BadRequestException {
        return dbSystem.getDatabase()
                .query(UserIdentifierQueries.fetchPlayerUUIDOf(playerName))
                .orElseThrow(() -> new BadRequestException("Given 'player' was not found in the database: '" + playerName + "'"));
    }
}