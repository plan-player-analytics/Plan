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
package com.djrapitops.plan.delivery.rendering.json;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.datatransfer.ServerDto;
import com.djrapitops.plan.delivery.domain.mutators.PlayerKillMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.extension.implementation.results.ExtensionTabData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerTableDataQuery;
import com.djrapitops.plan.gathering.ServerUptimeCalculator;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plan.storage.database.queries.objects.playertable.NetworkTablePlayersQuery;
import com.djrapitops.plan.storage.database.queries.objects.playertable.ServerTablePlayersQuery;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Factory with different JSON creation methods placed to a single class.
 *
 * @author AuroraLS3
 */
@Singleton
public class JSONFactory {

    private final PlanConfig config;
    private final Locale locale;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerUptimeCalculator serverUptimeCalculator;
    private final Graphs graphs;
    private final Formatters formatters;

    @Inject
    public JSONFactory(
            PlanConfig config,
            Locale locale,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ServerUptimeCalculator serverUptimeCalculator,
            Graphs graphs,
            Formatters formatters
    ) {
        this.config = config;
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.serverUptimeCalculator = serverUptimeCalculator;
        this.graphs = graphs;
        this.formatters = formatters;
    }

    public Map<String, Object> serverPlayersTableJSON(ServerUUID serverUUID) {
        Integer xMostRecentPlayers = config.get(DisplaySettings.PLAYERS_PER_SERVER_PAGE);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);
        boolean openPlayerLinksInNewTab = config.isTrue(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB);

        Database database = dbSystem.getDatabase();

        return new PlayersTableJSONCreator(
                database.query(new ServerTablePlayersQuery(serverUUID, System.currentTimeMillis(), playtimeThreshold, xMostRecentPlayers)),
                database.query(new ExtensionServerTableDataQuery(serverUUID, xMostRecentPlayers)),
                openPlayerLinksInNewTab,
                formatters, locale
        ).toJSONMap();
    }

    public Map<String, Object> networkPlayersTableJSON() {
        Integer xMostRecentPlayers = config.get(DisplaySettings.PLAYERS_PER_PLAYERS_PAGE);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);
        boolean openPlayerLinksInNewTab = config.isTrue(DisplaySettings.OPEN_PLAYER_LINKS_IN_NEW_TAB);

        Database database = dbSystem.getDatabase();

        ServerUUID mainServerUUID = database.query(ServerQueries.fetchProxyServerInformation()).map(Server::getUuid).orElse(serverInfo.getServerUUID());
        Map<UUID, ExtensionTabData> pluginData = database.query(new ExtensionServerTableDataQuery(mainServerUUID, xMostRecentPlayers));

        return new PlayersTableJSONCreator(
                database.query(new NetworkTablePlayersQuery(System.currentTimeMillis(), playtimeThreshold, xMostRecentPlayers)),
                pluginData,
                openPlayerLinksInNewTab,
                formatters, locale,
                true // players page
        ).toJSONMap();
    }

    public List<Map<String, Object>> serverSessionsAsJSONMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();

        Integer perPageLimit = config.get(DisplaySettings.SESSIONS_PER_PAGE);
        List<FinishedSession> sessions = db.query(SessionQueries.fetchLatestSessionsOfServer(serverUUID, perPageLimit));
        // Add online sessions
        if (serverUUID.equals(serverInfo.getServerUUID())) {
            addActiveSessions(sessions);
            sessions.sort(new SessionStartComparator());
            while (true) {
                int size = sessions.size();
                if (size <= perPageLimit) break;
                sessions.remove(size - 1); // Remove last until it fits.
            }
        }

        return new SessionsMutator(sessions).toPlayerNameJSONMaps(graphs, config.getWorldAliasSettings(), formatters);
    }

    public List<Map<String, Object>> networkSessionsAsJSONMap() {
        Database db = dbSystem.getDatabase();
        Integer perPageLimit = config.get(DisplaySettings.SESSIONS_PER_PAGE);

        List<FinishedSession> sessions = db.query(SessionQueries.fetchLatestSessions(perPageLimit));
        // Add online sessions
        if (serverInfo.getServer().isProxy()) {
            addActiveSessions(sessions);
            sessions.sort(new SessionStartComparator());
            while (true) {
                int size = sessions.size();
                if (size <= perPageLimit) break;
                sessions.remove(size - 1); // Remove last until it fits.
            }
        }

        List<Map<String, Object>> sessionMaps = new SessionsMutator(sessions).toPlayerNameJSONMaps(graphs, config.getWorldAliasSettings(), formatters);
        // Add network_server property so that sessions have a server page link
        sessionMaps.forEach(map -> map.put("network_server", map.get("server_name")));
        return sessionMaps;
    }

    public void addActiveSessions(List<FinishedSession> sessions) {
        for (ActiveSession activeSession : SessionCache.getActiveSessions()) {
            sessions.add(activeSession.toFinishedSessionFromStillActive());
        }
    }

    public List<Map<String, Object>> serverPlayerKillsAsJSONMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        List<PlayerKill> kills = db.query(KillQueries.fetchPlayerKillsOnServer(serverUUID, 100));
        return new PlayerKillMutator(kills).toJSONAsMap(formatters);
    }

    public Map<String, Object> serversAsJSONMaps() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);

        Formatter<Long> year = formatters.yearLong();
        Formatter<Double> decimals = formatters.decimals();
        Formatter<Long> timeAmount = formatters.timeAmount();

        Map<ServerUUID, Server> serverInformation = db.query(ServerQueries.fetchPlanServerInformation());
        ServerUUID proxyUUID = serverInformation.values().stream()
                .filter(Server::isProxy)
                .findFirst()
                .map(Server::getUuid).orElse(null);

        Map<ServerUUID, Integer> serverUuidToId = new HashMap<>();
        for (Server server : serverInformation.values()) {
            server.getId().ifPresent(serverId -> serverUuidToId.put(server.getUuid(), serverId));
        }

        Map<Integer, List<TPS>> tpsDataByServerId = db.query(
                TPSQueries.fetchTPSDataOfAllServersBut(weekAgo, now, proxyUUID)
        );
        Map<ServerUUID, Integer> totalPlayerCounts = db.query(PlayerCountQueries.newPlayerCounts(0, now));
        Map<ServerUUID, Integer> newPlayerCounts = db.query(PlayerCountQueries.newPlayerCounts(weekAgo, now));
        Map<ServerUUID, Integer> uniquePlayerCounts = db.query(PlayerCountQueries.uniquePlayerCounts(weekAgo, now));

        List<Map<String, Object>> servers = new ArrayList<>();
        serverInformation.entrySet()
                .stream() // Sort alphabetically
                .sorted(Comparator.comparing(entry -> entry.getValue().getIdentifiableName().toLowerCase()))
                .filter(entry -> entry.getValue().isNotProxy())
                .forEach(entry -> {
                    ServerUUID serverUUID = entry.getKey();
                    Map<String, Object> server = new HashMap<>();
                    server.put("name", entry.getValue().getIdentifiableName());

                    Optional<DateObj<Integer>> recentPeak = db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, now - TimeUnit.DAYS.toMillis(2L)));
                    Optional<DateObj<Integer>> allTimePeak = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID));
                    server.put("last_peak_date", recentPeak.map(DateObj::getDate).map(year).orElse("-"));
                    server.put("best_peak_date", allTimePeak.map(DateObj::getDate).map(year).orElse("-"));
                    server.put("last_peak_players", recentPeak.map(DateObj::getValue).orElse(0));
                    server.put("best_peak_players", allTimePeak.map(DateObj::getValue).orElse(0));

                    TPSMutator tpsMonth = new TPSMutator(tpsDataByServerId.getOrDefault(serverUuidToId.get(serverUUID), Collections.emptyList()));
                    server.put("playersOnline", tpsMonth.all().stream()
                            .map(tps -> new double[]{tps.getDate(), tps.getPlayers()})
                            .toArray(double[][]::new));
                    server.put("players", totalPlayerCounts.getOrDefault(serverUUID, 0));
                    server.put("new_players", newPlayerCounts.getOrDefault(serverUUID, 0));
                    server.put("unique_players", uniquePlayerCounts.getOrDefault(serverUUID, 0));
                    TPSMutator tpsWeek = tpsMonth.filterDataBetween(weekAgo, now);
                    double averageTPS = tpsWeek.averageTPS();
                    server.put("avg_tps", averageTPS != -1 ? decimals.apply(averageTPS) : locale.get(HtmlLang.UNIT_NO_DATA).toString());
                    server.put("low_tps_spikes", tpsWeek.lowTpsSpikeCount(config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)));
                    server.put("downtime", timeAmount.apply(tpsWeek.serverDownTime()));
                    server.put("current_uptime", serverUptimeCalculator.getServerUptimeMillis(serverUUID).map(timeAmount)
                            .orElse(locale.getString(GenericLang.UNAVAILABLE)));

                    Optional<TPS> online = tpsWeek.getLast();
                    server.put("online", online.map(point -> point.getDate() >= now - TimeUnit.MINUTES.toMillis(3L) ? point.getPlayers() : "Possibly offline")
                            .orElse(locale.get(HtmlLang.UNIT_NO_DATA).toString()));
                    servers.add(server);
                });
        return Collections.singletonMap("servers", servers);
    }

    public Map<String, Object> pingPerGeolocation(ServerUUID serverUUID) {
        Map<String, Ping> pingByGeolocation = dbSystem.getDatabase().query(PingQueries.fetchPingDataOfServerByGeolocation(serverUUID));
        return Maps.builder(String.class, Object.class)
                .put("table", turnToTableEntries(pingByGeolocation))
                .build();
    }

    public Map<String, Object> pingPerGeolocation() {
        Map<String, Ping> pingByGeolocation = dbSystem.getDatabase().query(PingQueries.fetchPingDataOfNetworkByGeolocation());
        return Maps.builder(String.class, Object.class)
                .put("table", turnToTableEntries(pingByGeolocation))
                .build();
    }

    private List<Map<String, Object>> turnToTableEntries(Map<String, Ping> pingByGeolocation) {
        List<Map<String, Object>> tableEntries = new ArrayList<>();
        for (Map.Entry<String, Ping> entry : pingByGeolocation.entrySet()) {
            String geolocation = entry.getKey();
            Ping ping = entry.getValue();

            tableEntries.add(Maps.builder(String.class, Object.class)
                    .put("country", geolocation)
                    .put("avg_ping", formatters.decimals().apply(ping.getAverage()) + " ms")
                    .put("min_ping", ping.getMin() + " ms")
                    .put("max_ping", ping.getMax() + " ms")
                    .build());
        }
        return tableEntries;
    }

    public Map<String, List<ServerDto>> listServers() {
        Collection<Server> servers = dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformationCollection());
        return Collections.singletonMap("servers", servers.stream()
                .map(ServerDto::fromServer)
                .collect(Collectors.toList()));
    }
}