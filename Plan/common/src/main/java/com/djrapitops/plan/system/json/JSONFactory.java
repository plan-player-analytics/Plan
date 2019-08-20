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
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.PlayerKillMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayersTableContainersQuery;
import com.djrapitops.plan.db.access.queries.objects.KillQueries;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.TPSQueries;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerPlayerDataTableQuery;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public List<Map<String, Object>> serversAsJSONMaps() {
        Database db = dbSystem.getDatabase();
        Map<UUID, Server> serverInformation = db.query(ServerQueries.fetchPlanServerInformation());
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);

        Formatter<Long> year = formatters.yearLong();
        Formatter<Double> decimals = formatters.decimals();
        Formatter<Long> timeAmount = formatters.timeAmount();

        List<Map<String, Object>> servers = new ArrayList<>();
        serverInformation.entrySet()
                .stream() // Sort alphabetically
                .sorted(Comparator.comparing(entry -> entry.getValue().getIdentifiableName().toLowerCase()))
                .forEach(entry -> {
                    if (entry.getValue().isProxy()) return;

                    UUID serverUUID = entry.getKey();
                    Map<String, Object> server = new HashMap<>();
                    server.put("name", entry.getValue().getIdentifiableName());

                    // TODO Optimize these queries
                    Optional<DateObj<Integer>> recentPeak = db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, now - TimeUnit.DAYS.toMillis(2L)));
                    Optional<DateObj<Integer>> allTimePeak = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID));
                    server.put("last_peak_date", recentPeak.map(DateObj::getDate).map(year).orElse("-"));
                    server.put("best_peak_date", allTimePeak.map(DateObj::getDate).map(year).orElse("-"));
                    server.put("last_peak_players", recentPeak.map(DateObj::getValue).orElse(0));
                    server.put("best_peak_players", allTimePeak.map(DateObj::getValue).orElse(0));

                    TPSMutator tpsMonth = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(now - TimeUnit.DAYS.toMillis(30L), now, serverUUID)));
                    server.put("playersOnline", tpsMonth.all().stream().map(tps -> new double[]{tps.getDate(), tps.getPlayers()}).toArray(double[][]::new));
                    server.put("players", db.query(PlayerCountQueries.newPlayerCount(0, now, serverUUID)));
                    server.put("new_players", db.query(PlayerCountQueries.newPlayerCount(weekAgo, now, serverUUID)));
                    server.put("unique_players", db.query(PlayerCountQueries.uniquePlayerCount(weekAgo, now, serverUUID)));
                    TPSMutator tpsWeek = tpsMonth.filterDataBetween(weekAgo, now);
                    double averageTPS = tpsWeek.averageTPS();
                    server.put("avg_tps", averageTPS != -1 ? decimals.apply(averageTPS) : "No data");
                    server.put("low_tps_spikes", tpsWeek.lowTpsSpikeCount(config.getNumber(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)));
                    server.put("downtime", timeAmount.apply(tpsWeek.serverDownTime()));
                    Optional<TPS> online = db.query(TPSQueries.fetchLatestTPSEntryForServer(serverUUID));
                    server.put("online", online.isPresent() ?
                            online.get().getDate() >= now - TimeUnit.MINUTES.toMillis(3L) ?
                                    online.get().getPlayers() : "Possibly offline"
                            : "No data");
                    servers.add(server);
                });
        return servers;
    }
}