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

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.PerServerMutator;
import com.djrapitops.plan.data.store.mutators.PingMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.PlayerContainerQuery;
import com.djrapitops.plan.db.access.queries.objects.ServerQueries;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class PlayerJSONParser {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final Formatters formatters;

    private final Formatter<Long> timeAmount;
    private final Formatter<Double> decimals;
    private final Formatter<Long> year;

    @Inject
    public PlayerJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters,
            Graphs graphs
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        this.formatters = formatters;
        timeAmount = formatters.timeAmount();
        decimals = formatters.decimals();
        year = formatters.yearLong();
        this.graphs = graphs;
    }

    public Map<String, Object> createJSONAsMap(UUID playerUUID) {
        Database db = dbSystem.getDatabase();

        Map<UUID, String> serverNames = db.query(ServerQueries.fetchServerNames());
        PlayerContainer player = db.query(new PlayerContainerQuery(playerUUID));
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);

        Map<String, Object> data = new HashMap<>();
        data.put("info", createInfoJSONMap(player, serverNames));
        data.put("online_activity", createOnlineActivityJSONMap(sessionsMutator));

        data.put("nicknames", player.getValue(PlayerKeys.NICKNAMES)
                .map(nicks -> Nickname.fromDataNicknames(nicks, serverNames, year))
                .orElse(Collections.emptyList()));
        data.put("connections", player.getValue(PlayerKeys.GEO_INFO)
                .map(geoInfo -> ConnectionInfo.fromGeoInfo(geoInfo, year))
                .orElse(Collections.emptyList()));
        data.put("player_kills", player.getValue(PlayerKeys.PLAYER_KILLS).orElse(Collections.emptyList()));
        data.put("player_deaths", player.getValue(PlayerKeys.PLAYER_DEATHS_KILLS).orElse(Collections.emptyList()));
        data.put("sessions", sessionsMutator.toServerNameJSONMaps(graphs, config.getWorldAliasSettings(), formatters));
        data.put("punchcard_series", graphs.special().punchCard(sessionsMutator).getDots());
        WorldPie worldPie = graphs.pie().worldPie(player.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes()));
        data.put("world_pie_series", worldPie.getSlices());
        data.put("gm_series", worldPie.toHighChartsDrillDownMaps());
        data.put("calendar_series", graphs.calendar().playerCalendar(player).getEntries());
        data.put("first_day", 1); // Monday
        return data;
    }

    private Map<String, Object> createOnlineActivityJSONMap(SessionsMutator sessionsMutator) {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        SessionsMutator sessionsMonth = sessionsMutator.filterSessionsBetween(monthAgo, now);
        SessionsMutator sessionsWeek = sessionsMutator.filterSessionsBetween(weekAgo, now);

        Map<String, Object> onlineActivity = new HashMap<>();

        onlineActivity.put("playtime_30d", timeAmount.apply(sessionsMonth.toPlaytime()));
        onlineActivity.put("active_playtime_30d", timeAmount.apply(sessionsMonth.toActivePlaytime()));
        onlineActivity.put("afk_time_30d", timeAmount.apply(sessionsMonth.toAfkTime()));
        onlineActivity.put("average_session_length_30d", timeAmount.apply(sessionsMonth.toAverageSessionLength()));
        onlineActivity.put("session_count_30d", sessionsMonth.count());
        onlineActivity.put("player_kill_count_30d", sessionsMonth.toPlayerKillCount());
        onlineActivity.put("mob_kill_count_30d", sessionsMonth.toMobKillCount());
        onlineActivity.put("death_count_30d", sessionsMonth.toDeathCount());

        onlineActivity.put("playtime_7d", timeAmount.apply(sessionsWeek.toPlaytime()));
        onlineActivity.put("active_playtime_7d", timeAmount.apply(sessionsWeek.toActivePlaytime()));
        onlineActivity.put("afk_time_7d", timeAmount.apply(sessionsWeek.toAfkTime()));
        onlineActivity.put("average_session_length_7d", timeAmount.apply(sessionsWeek.toAverageSessionLength()));
        onlineActivity.put("session_count_7d", sessionsWeek.count());
        onlineActivity.put("player_kill_count_7d", sessionsWeek.toPlayerKillCount());
        onlineActivity.put("mob_kill_count_7d", sessionsWeek.toMobKillCount());
        onlineActivity.put("death_count_7d", sessionsWeek.toDeathCount());

        return onlineActivity;
    }

    private Map<String, Object> createInfoJSONMap(PlayerContainer player, Map<UUID, String> serverNames) {
        SessionsMutator sessions = SessionsMutator.forContainer(player);
        ActivityIndex activityIndex = player.getActivityIndex(System.currentTimeMillis(), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD));
        PerServerMutator perServer = PerServerMutator.forContainer(player);
        PingMutator ping = PingMutator.forContainer(player);

        Map<String, Object> info = new HashMap<>();

        info.put("online", SessionCache.getCachedSession(player.getUnsafe(PlayerKeys.UUID)).isPresent());
        info.put("operator", player.getValue(PlayerKeys.OPERATOR).orElse(false));
        info.put("banned", player.getValue(PlayerKeys.BANNED).orElse(false));
        info.put("kick_count", player.getValue(PlayerKeys.KICK_COUNT).orElse(0));
        info.put("player_kill_count", player.getValue(PlayerKeys.PLAYER_KILL_COUNT).orElse(0));
        info.put("mob_kill_count", player.getValue(PlayerKeys.MOB_KILL_COUNT).orElse(0));
        info.put("death_count", player.getValue(PlayerKeys.DEATH_COUNT).orElse(0));
        info.put("playtime", timeAmount.apply(sessions.toPlaytime()));
        info.put("active_playtime", timeAmount.apply(sessions.toActivePlaytime()));
        info.put("afk_time", timeAmount.apply(sessions.toAfkTime()));
        info.put("session_count", sessions.count());
        info.put("longest_session_length", timeAmount.apply(sessions.toLongestSessionLength()));
        info.put("session_median", timeAmount.apply(sessions.toMedianSessionLength()));
        info.put("activity_index", decimals.apply(activityIndex.getValue()));
        info.put("activity_index_group", activityIndex.getGroup());
        UUID favoriteServer = perServer.favoriteServer();
        info.put("favorite_server", serverNames.getOrDefault(favoriteServer, favoriteServer.toString()));
        info.put("average_ping", decimals.apply(ping.average()) + " ms");
        info.put("worst_ping", ping.max() + " ms");
        info.put("best_ping", ping.min() + " ms");
        info.put("registered", player.getValue(PlayerKeys.REGISTERED).map(year).orElse("-"));
        info.put("last_seen", player.getValue(PlayerKeys.LAST_SEEN).map(year).orElse("-"));

        return info;
    }

    public static class Nickname {
        private String nickname;
        private String server;
        private String date;

        public Nickname(String nickname, String server, String date) {
            this.nickname = nickname;
            this.server = server;
            this.date = date;
        }

        public static List<Nickname> fromDataNicknames(
                List<com.djrapitops.plan.data.store.objects.Nickname> nicknames,
                Map<UUID, String> serverNames,
                Formatter<Long> dateFormatter
        ) {
            List<Nickname> mapped = new ArrayList<>();
            for (com.djrapitops.plan.data.store.objects.Nickname nickname : nicknames) {
                mapped.add(new Nickname(
                        nickname.getName(),
                        serverNames.getOrDefault(nickname.getServerUUID(), nickname.getServerUUID().toString()),
                        dateFormatter.apply(nickname.getDate())
                ));
            }
            return mapped;
        }
    }

    public static class ConnectionInfo {
        private String geolocation;
        private String date;

        public ConnectionInfo(String geolocation, String date) {
            this.geolocation = geolocation;
            this.date = date;
        }

        public static List<ConnectionInfo> fromGeoInfo(List<GeoInfo> geoInfo, Formatter<Long> dateFormatter) {
            return geoInfo.stream()
                    .map(i -> new ConnectionInfo(i.getGeolocation(), dateFormatter.apply(i.getDate())))
                    .collect(Collectors.toList());
        }
    }

}
