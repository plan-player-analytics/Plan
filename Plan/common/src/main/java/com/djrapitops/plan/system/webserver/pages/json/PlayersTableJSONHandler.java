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
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayersTableContainersQuery;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerPlayerDataTableQuery;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.PageHandler;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.data.JSONResponse;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.tables.PlayersTableJSONParser;

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

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Formatters formatters;

    @Inject
    public PlayersTableJSONHandler(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.formatters = formatters;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        UUID serverUUID = getServerUUID(target); // Can throw BadRequestException
        return new JSONResponse(parseJSON(serverUUID));
    }

    public String parseJSON(UUID serverUUID) {
        Integer xMostRecentPlayers = config.get(DisplaySettings.PLAYERS_PER_SERVER_PAGE);
        Integer loginThreshold = config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);
        Boolean openPlayerLinksInNewTab = config.get(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB);

        Database database = dbSystem.getDatabase();

        return new PlayersTableJSONParser(
                database.query(new ServerPlayersTableContainersQuery(serverUUID)),
                database.query(new ExtensionServerPlayerDataTableQuery(serverUUID, xMostRecentPlayers)),
                xMostRecentPlayers, playtimeThreshold, loginThreshold, openPlayerLinksInNewTab,
                formatters
        ).toJSONString();
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