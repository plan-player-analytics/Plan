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
package com.djrapitops.plan.system.json;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.mutators.PlayerKillMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayersTableContainersQuery;
import com.djrapitops.plan.db.access.queries.objects.KillQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerPlayerDataTableQuery;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Factory with different JSON parsing placed to a single class.
 *
 * @author Rsl1122
 */
@Singleton
public class JSONFactory {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final Formatters formatters;

    @Inject
    public JSONFactory(
            PlanConfig config,
            DBSystem dbSystem,
            Graphs graphs,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.graphs = graphs;
        this.formatters = formatters;
    }

    public String serverPlayersTableJSON(UUID serverUUID) {
        Integer xMostRecentPlayers = config.get(DisplaySettings.PLAYERS_PER_SERVER_PAGE);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);
        Boolean openPlayerLinksInNewTab = config.get(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB);

        Database database = dbSystem.getDatabase();

        return new PlayersTableJSONParser(
                database.query(new ServerPlayersTableContainersQuery(serverUUID)),
                database.query(new ExtensionServerPlayerDataTableQuery(serverUUID, xMostRecentPlayers)),
                xMostRecentPlayers, playtimeThreshold, openPlayerLinksInNewTab,
                formatters
        ).toJSONString();
    }

    public List<Map<String, Object>> serverSessionsAsJSONMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        List<Session> sessions = db.query(SessionQueries.fetchLatestSessionsOfServer(
                serverUUID, config.get(DisplaySettings.SESSIONS_PER_PAGE)
        ));
        return new SessionsMutator(sessions).toPlayerNameJSONMaps(graphs, config.getWorldAliasSettings(), formatters);
    }

    public List<Map<String, Object>> serverPlayerKillsAsJSONMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        List<PlayerKill> kills = db.query(KillQueries.fetchPlayerKillsOnServer(serverUUID, 100));
        return new PlayerKillMutator(kills).toJSONAsMap(formatters);
    }
}