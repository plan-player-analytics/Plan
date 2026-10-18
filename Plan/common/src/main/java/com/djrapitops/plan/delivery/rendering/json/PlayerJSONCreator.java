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

import com.djrapitops.plan.delivery.domain.ServerMinecraftStatistics;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionsDto;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.*;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.PingGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.PlayerContainerQuery;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.StatisticsQueries;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Singleton
public class PlayerJSONCreator {

    private final PlanConfig config;
    private final Theme theme;
    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final Formatters formatters;

    private final Formatter<Double> decimals;
    private final Formatter<Long> year;

    @Inject
    public PlayerJSONCreator(
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            Formatters formatters,
            Graphs graphs
    ) {
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;

        this.formatters = formatters;
        decimals = formatters.decimals();
        year = formatters.yearLong();
        this.graphs = graphs;
    }

    public Map<String, Object> createJSONAsMap(UUID playerUUID, Predicate<WebPermission> hasPermission) {
        Database db = dbSystem.getDatabase();

        Map<ServerUUID, String> serverNames = db.query(ServerQueries.fetchServerNames());
        PlayerContainer player = db.query(new PlayerContainerQuery(playerUUID));
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);

        PingMutator.forContainer(player).addPingToSessions(sessionsMutator.all());

        Map<String, Object> data = new HashMap<>();

        long now = System.currentTimeMillis();
        data.put("timestamp", now);
        data.put("timestampF", year.apply(now));

        if (hasPermission.test(WebPermission.PAGE_PLAYER_OVERVIEW)) {
            data.put("info", createInfoJSONMap(player, serverNames));
            data.put("onlineActivity", createOnlineActivityJSONMap(sessionsMutator));
            data.put("nicknames", player.getValue(PlayerKeys.NICKNAMES)
                    .map(nicks -> Nickname.fromDataNicknames(nicks, serverNames))
                    .orElse(Collections.emptyList()));
            data.put("connections", player.getValue(PlayerKeys.GEO_INFO)
                    .map(ConnectionInfo::fromGeoInfo)
                    .orElse(Collections.emptyList()));
            data.put("punchcardSeries", graphs.special().punchCard(sessionsMutator).getDots());
        } else {
            data.put("info", createLimitedInfoMap(player));
        }
        if (hasPermission.test(WebPermission.PAGE_PLAYER_SESSIONS)) {
            data.put("sessions", sessionsMutator.sort(new DateHolderRecentComparator()).toServerNameJSONMaps(graphs, config.getWorldAliasSettings(), formatters));
            data.put("sessionsPerPage", 10000);
            WorldPie worldPie = graphs.pie().worldPie(player.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes()));
            data.put("worldPieSeries", worldPie.getSlices());
            data.put("gmSeries", worldPie.toHighChartsDrillDownMaps());
            data.put("firstDay", 1); // Monday
            data.put("calendarSeries", graphs.calendar().playerCalendar(player).getEntries());
        }
        if (hasPermission.test(WebPermission.PAGE_PLAYER_VERSUS)) {
            List<PlayerKill> kills = player.getValue(PlayerKeys.PLAYER_KILLS).orElse(Collections.emptyList());
            List<PlayerKill> deaths = player.getValue(PlayerKeys.PLAYER_DEATHS_KILLS).orElse(Collections.emptyList());

            data.put("killData", createPvPPvEMap(player));
            data.put("playerKills", new PlayerKillMutator(kills).filterNonSelfKills().toJSONAsMap(formatters));
            data.put("playerDeaths", new PlayerKillMutator(deaths).toJSONAsMap(formatters));
        }
        if (hasPermission.test(WebPermission.PAGE_PLAYER_SERVERS)) {
            List<Map<String, Object>> serverAccordion = new ServerAccordion(player, serverNames, graphs, GenericLang.UNKNOWN.getKey()).asMaps();
            Map<ServerUUID, WorldTimes> worldTimesPerServer = PerServerMutator.forContainer(player).worldTimesPerServer();
            String[] pieColors = theme.getWorldPieColors();

            data.put("pingGraph", createPingGraphJson(player));
            data.put("servers", serverAccordion);
            data.put("serverPieSeries", graphs.pie().serverPreferencePie(serverNames, worldTimesPerServer).getSlices());
            data.put("serverPieColors", pieColors);
        }
        if (hasPermission.test(WebPermission.PAGE_PLAYER_STATISTICS)) {
            data.put("statistics", playerStatistics(playerUUID));
        } else {
            data.put("statistics", List.of());
        }
        if (hasPermission.test(WebPermission.PAGE_PLAYER_PLUGINS)) {
            data.put("extensions", playerExtensionData(playerUUID));
        } else {
            data.put("extensions", List.of());
        }

        return data;
    }

    private List<ServerMinecraftStatistics> playerStatistics(UUID playerUUID) {
        return dbSystem.getDatabase().query(StatisticsQueries.fetchStatistics(playerUUID));
    }

    private Map<String, Object> createPingGraphJson(PlayerContainer player) {
        PingGraph pingGraph = graphs.line().pingGraph(player.getUnsafe(PlayerKeys.PING));
        return Maps.builder(String.class, Object.class)
                .put("minPingSeries", pingGraph.getMinGraph().getPointArrays())
                .put("avgPingSeries", pingGraph.getAvgGraph().getPointArrays())
                .put("maxPingSeries", pingGraph.getMaxGraph().getPointArrays())
                .put("colors", Maps.builder(String.class, String.class)
                        .put("min", "#ffd54f")
                        .put("avg", "#ffc107")
                        .put("max", "#ffa000")
                        .build())
                .build();
    }

    private Map<String, Object> createOnlineActivityJSONMap(SessionsMutator sessionsMutator) {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        SessionsMutator sessions30d = sessionsMutator.filterSessionsBetween(monthAgo, now);
        SessionsMutator sessions7d = sessions30d.filterSessionsBetween(weekAgo, now);

        Map<String, Object> onlineActivity = new HashMap<>();

        onlineActivity.put("playtime30d", sessions30d.toPlaytime());
        onlineActivity.put("activePlaytime30d", sessions30d.toActivePlaytime());
        onlineActivity.put("afkTime30d", sessions30d.toAfkTime());
        onlineActivity.put("averageSessionLength30d", sessions30d.toAverageSessionLength());
        onlineActivity.put("medianSessionLength30d", sessions30d.toMedianSessionLength());
        onlineActivity.put("sessionCount30d", sessions30d.count());
        onlineActivity.put("playerKillCount30d", sessions30d.toPlayerKillCount());
        onlineActivity.put("mobKillCount30d", sessions30d.toMobKillCount());
        onlineActivity.put("deathCount30d", sessions30d.toDeathCount());

        onlineActivity.put("playtime7d", sessions7d.toPlaytime());
        onlineActivity.put("activePlaytime7d", sessions7d.toActivePlaytime());
        onlineActivity.put("afkTime7d", sessions7d.toAfkTime());
        onlineActivity.put("averageSessionLength7d", sessions7d.toAverageSessionLength());
        onlineActivity.put("medianSessionLength7d", sessions7d.toMedianSessionLength());
        onlineActivity.put("sessionCount7d", sessions7d.count());
        onlineActivity.put("playerKillCount7d", sessions7d.toPlayerKillCount());
        onlineActivity.put("mobKillCount7d", sessions7d.toMobKillCount());
        onlineActivity.put("deathCount7d", sessions7d.toDeathCount());

        return onlineActivity;
    }

    private Map<String, Object> createInfoJSONMap(PlayerContainer player, Map<ServerUUID, String> serverNames) {
        SessionsMutator sessions = SessionsMutator.forContainer(player);
        ActivityIndex activityIndex = player.getActivityIndex(System.currentTimeMillis(), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD));
        PerServerMutator perServer = PerServerMutator.forContainer(player);
        PingMutator ping = PingMutator.forContainer(player);

        Map<String, Object> info = new HashMap<>();

        info.put("name", player.getValue(PlayerKeys.NAME).orElse(player.getUnsafe(PlayerKeys.UUID).toString()));
        info.put("uuid", player.getUnsafe(PlayerKeys.UUID).toString());
        info.put("online", SessionCache.getCachedSession(player.getUnsafe(PlayerKeys.UUID)).isPresent());
        info.put("operator", player.getValue(PlayerKeys.OPERATOR).orElse(false));
        info.put("banned", player.getValue(PlayerKeys.BANNED).orElse(false));
        info.put("kickCount", player.getValue(PlayerKeys.KICK_COUNT).orElse(0));
        info.put("playerKillCount", player.getValue(PlayerKeys.PLAYER_KILL_COUNT).orElse(0));
        info.put("mobKillCount", player.getValue(PlayerKeys.MOB_KILL_COUNT).orElse(0));
        info.put("deathCount", player.getValue(PlayerKeys.DEATH_COUNT).orElse(0));
        info.put("playtime", sessions.toPlaytime());
        info.put("activePlaytime", sessions.toActivePlaytime());
        info.put("afkTime", sessions.toAfkTime());
        info.put("sessionCount", sessions.count());
        info.put("longestSessionLength", sessions.toLongestSessionLength());
        info.put("sessionMedian", sessions.toMedianSessionLength());
        info.put("activityIndex", activityIndex.getValue());
        info.put("activityIndexGroup", activityIndex.getGroupLang());
        info.put("favoriteServer", perServer.favoriteServer().map(favoriteServer -> serverNames.getOrDefault(favoriteServer, favoriteServer.toString())).orElse(GenericLang.UNKNOWN.getKey()));
        info.put("latestJoinAddress", sessions.latestSession()
                .flatMap(session -> session.getExtraData(JoinAddress.class))
                .map(JoinAddress::getAddress)
                .orElse("-"));
        double averagePing = ping.average();
        int worstPing = ping.max();
        int bestPing = ping.min();

        String unavailable = GenericLang.UNAVAILABLE.getKey();
        info.put("averagePing", averagePing != -1.0 ? averagePing : unavailable);
        info.put("worstPing", worstPing != -1.0 ? worstPing : unavailable);
        info.put("bestPing", bestPing != -1.0 ? bestPing : unavailable);
        info.put("registered", player.getValue(PlayerKeys.REGISTERED).map(Object.class::cast).orElse("-"));
        info.put("lastSeen", player.getValue(PlayerKeys.LAST_SEEN).map(Object.class::cast).orElse("-"));
        info.put("lastSeenRawValue", player.getValue(PlayerKeys.LAST_SEEN).orElse(0L));

        return info;
    }

    private Map<String, Object> createLimitedInfoMap(PlayerContainer player) {
        Map<String, Object> info = new HashMap<>();

        info.put("name", player.getValue(PlayerKeys.NAME).orElse(player.getUnsafe(PlayerKeys.UUID).toString()));
        info.put("uuid", player.getUnsafe(PlayerKeys.UUID).toString());

        return info;
    }

    private Map<String, Object> createPvPPvEMap(PlayerContainer playerContainer) {
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        PlayerVersusMutator playerVersus = PlayerVersusMutator.forContainer(playerContainer);
        PlayerVersusMutator playerVersus30d = playerVersus.filterBetween(monthAgo, now);
        PlayerVersusMutator playerVersus7d = playerVersus30d.filterBetween(weekAgo, now);

        Map<String, Object> killData = new HashMap<>();
        int pks = playerVersus.toPlayerKillCount();
        int pks7d = playerVersus7d.toPlayerKillCount();
        int pks30d = playerVersus30d.toPlayerKillCount();
        killData.put("playerKillsTotal", pks);
        killData.put("playerKills30d", pks30d);
        killData.put("playerKills7d", pks7d);

        int playerDeaths = playerVersus.toPlayerDeathCount();
        int playerDeaths30d = playerVersus30d.toPlayerDeathCount();
        int playerDeaths7d = playerVersus7d.toPlayerDeathCount();
        killData.put("playerDeathsTotal", playerDeaths);
        killData.put("playerDeaths30d", playerDeaths30d);
        killData.put("playerDeaths7d", playerDeaths7d);

        double kdr = playerDeaths != 0 ? (double) pks / playerDeaths : pks;
        double kdr30d = playerDeaths30d != 0 ? (double) pks30d / playerDeaths30d : pks30d;
        double krd7d = playerDeaths7d != 0 ? (double) pks7d / playerDeaths7d : pks7d;
        killData.put("playerKdrTotal", decimals.apply(kdr));
        killData.put("playerKdr30d", decimals.apply(kdr30d));
        killData.put("playerKdr7d", decimals.apply(krd7d));

        int mobKills = playerVersus.toMobKillCount();
        int mobKills30d = playerVersus30d.toMobKillCount();
        int mobKills7d = playerVersus7d.toMobKillCount();
        killData.put("mobKillsTotal", mobKills);
        killData.put("mobKills30d", mobKills30d);
        killData.put("mobKills7d", mobKills7d);

        int deaths = playerVersus.toDeathCount();
        int deaths30d = playerVersus30d.toDeathCount();
        int deaths7d = playerVersus7d.toDeathCount();
        killData.put("deathsTotal", deaths);
        killData.put("deaths30d", deaths30d);
        killData.put("deaths7d", deaths7d);

        int mobDeaths = deaths - playerDeaths;
        int mobDeaths30d = deaths30d - playerDeaths30d;
        int mobDeaths7d = deaths7d - playerDeaths7d;

        killData.put("mobDeathsTotal", mobDeaths);
        killData.put("mobDeaths30d", mobDeaths30d);
        killData.put("mobDeaths7d", mobDeaths7d);

        double mobKdr = mobDeaths != 0 ? (double) mobKills / mobDeaths : mobKills;
        double mobKdr30d = mobDeaths30d != 0 ? (double) mobKills30d / mobDeaths30d : mobKills30d;
        double mobKdr7d = mobDeaths7d != 0 ? (double) mobKills7d / mobDeaths7d : mobKills7d;
        killData.put("mobKdrTotal", decimals.apply(mobKdr));
        killData.put("mobKdr30d", decimals.apply(mobKdr30d));
        killData.put("mobKdr7d", decimals.apply(mobKdr7d));

        List<String> topWeapons = playerVersus.toTopWeapons(3);
        killData.put("weapon1st", getWeapon(topWeapons, 0).orElse("-"));
        killData.put("weapon2nd", getWeapon(topWeapons, 1).orElse("-"));
        killData.put("weapon3rd", getWeapon(topWeapons, 2).orElse("-"));

        return killData;
    }

    private <T> Optional<T> getWeapon(List<T> list, int index) {
        return list.size() <= index ? Optional.empty() : Optional.of(list.get(index));
    }


    public List<ExtensionsDto> playerExtensionData(UUID playerUUID) {
        Database database = dbSystem.getDatabase();
        Map<ServerUUID, List<ExtensionData>> extensionPlayerData = database.query(new ExtensionPlayerDataQuery(playerUUID));
        Map<ServerUUID, Server> servers = database.query(ServerQueries.fetchPlanServerInformation());

        List<ExtensionsDto> playerData = new ArrayList<>();
        for (Map.Entry<ServerUUID, Server> entry : servers.entrySet()) {
            ServerUUID serverUUID = entry.getKey();
            playerData.add(new ExtensionsDto(
                    playerUUID.toString(), serverUUID.toString(),
                    entry.getValue().getIdentifiableName(),
                    extensionPlayerData.getOrDefault(serverUUID, Collections.emptyList())
            ));
        }
        return playerData;
    }

    public static class Nickname {
        final String nickname;
        final String server;
        final long date;

        public Nickname(String nickname, String server, long date) {
            this.nickname = nickname;
            this.server = server;
            this.date = date;
        }

        public static List<Nickname> fromDataNicknames(
                List<com.djrapitops.plan.delivery.domain.Nickname> nicknames,
                Map<ServerUUID, String> serverNames
        ) {
            nicknames.sort(new DateHolderRecentComparator());
            List<Nickname> mapped = new ArrayList<>();
            for (com.djrapitops.plan.delivery.domain.Nickname nickname : nicknames) {
                mapped.add(new Nickname(
                        nickname.name(),
                        serverNames.getOrDefault(nickname.serverUUID(), nickname.serverUUID().toString()),
                        nickname.date()
                ));
            }
            return mapped;
        }
    }

    public static class ConnectionInfo {
        final String geolocation;
        final long date;

        public ConnectionInfo(String geolocation, long date) {
            this.geolocation = geolocation;
            this.date = date;
        }

        public static List<ConnectionInfo> fromGeoInfo(List<GeoInfo> geoInfo) {
            return Lists.map(geoInfo, i -> new ConnectionInfo(i.geolocation(), i.date()));
        }
    }

}
