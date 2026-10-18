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
package com.djrapitops.plan.delivery.rendering.json.network;

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.Trend;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.ServerUptimeCalculator;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /network-page Network Overview tab.
 *
 * @author AuroraLS3
 * @deprecated Use /v1/datapoint instead (types UNIQUE_PLAYERS, NEW_PLAYERS, REGULAR_PLAYERS, UPTIME_CURRENT, PLAYERS_ONLINE, PLAYTIME, PLAYTIME_PER_PLAYER_AVERAGE, SESSION_LENGTH_AVERAGE, PLAYER_KILLS, MOB_KILLS, DEATHS).
 */
@Singleton
@Deprecated(since = "2026-04-19 / 5.7 build 3392")
public class NetworkOverviewJSONCreator implements NetworkTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerSensor<?> serverSensor;
    private final ServerUptimeCalculator serverUptimeCalculator;
    private final PluginLogger logger;
    private final Locale locale;

    @Inject
    public NetworkOverviewJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ServerSensor<?> serverSensor,
            ServerUptimeCalculator serverUptimeCalculator,
            Formatters formatters,
            PluginLogger logger, Locale locale
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.serverSensor = serverSensor;
        this.serverUptimeCalculator = serverUptimeCalculator;
        this.logger = logger;
        this.locale = locale;
    }

    public Map<String, Object> createJSONAsMap() {
        logger.warn(locale.getString(PluginLang.DEPRECATED_ENDPOINT_CALL, "/v1/network/overview", "/v1/datapoint"));

        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("players", createPlayersMap());
        serverOverview.put("numbers", createNumbersMap());
        serverOverview.put("weeks", createWeeksMap());
        return serverOverview;
    }

    private Map<String, Object> createPlayersMap() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> sevenDays = new HashMap<>();

        sevenDays.put("uniquePlayers1d", db.query(PlayerCountQueries.uniquePlayerCount(dayAgo, now)));
        sevenDays.put("uniquePlayers7d", db.query(PlayerCountQueries.uniquePlayerCount(weekAgo, now)));
        sevenDays.put("uniquePlayers30d", db.query(PlayerCountQueries.uniquePlayerCount(monthAgo, now)));

        sevenDays.put("newPlayers1d", db.query(PlayerCountQueries.newPlayerCount(dayAgo, now)));
        sevenDays.put("newPlayers7d", db.query(PlayerCountQueries.newPlayerCount(weekAgo, now)));
        sevenDays.put("newPlayers30d", db.query(PlayerCountQueries.newPlayerCount(monthAgo, now)));

        return sevenDays;
    }

    private Map<String, Object> createNumbersMap() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - TimeUnit.DAYS.toMillis(2L);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> numbers = new HashMap<>();

        Integer userCount = db.query(PlayerCountQueries.newPlayerCount(0L, now));
        numbers.put("totalPlayers", userCount);
        numbers.put("regularPlayers", db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(now, playtimeThreshold)));
        numbers.put("onlinePlayers", serverSensor.getOnlinePlayerCount());
        ServerUUID serverUUID = serverInfo.getServerUUID();
        Optional<DateObj<Integer>> lastPeak = db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, twoDaysAgo, Long.MAX_VALUE));
        Optional<DateObj<Integer>> allTimePeak = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID));
        numbers.put("lastPeakDate", lastPeak.map(DateObj::date).map(Object.class::cast).orElse("-"));
        numbers.put("lastPeakPlayers", lastPeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        numbers.put("bestPeakDate", allTimePeak.map(DateObj::date).map(Object.class::cast).orElse("-"));
        numbers.put("bestPeakPlayers", allTimePeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        Long totalPlaytime = db.query(SessionQueries.playtime(0L, now));
        numbers.put("playtime", totalPlaytime);
        numbers.put("playerPlaytime", userCount != 0 ? totalPlaytime / userCount : "-");
        Long sessionCount = db.query(SessionQueries.sessionCount(0L, now));
        numbers.put("sessions", sessionCount);
        numbers.put("sessionLengthAvg", sessionCount != 0 ? totalPlaytime / sessionCount : "-");
        numbers.put("currentUptime", serverUptimeCalculator.getServerUptimeMillis(serverUUID)
                .map(Object.class::cast)
                .orElse(GenericLang.UNAVAILABLE.getKey()));

        return numbers;
    }

    private Map<String, Object> createWeeksMap() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long oneWeekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long twoWeeksAgo = now - TimeUnit.DAYS.toMillis(14L);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> weeks = new HashMap<>();

        weeks.put("start", twoWeeksAgo);
        weeks.put("midpoint", oneWeekAgo);
        weeks.put("end", now);

        Integer uniqueBefore = db.query(PlayerCountQueries.uniquePlayerCount(twoWeeksAgo, oneWeekAgo));
        Integer uniqueAfter = db.query(PlayerCountQueries.uniquePlayerCount(oneWeekAgo, now));
        Trend uniqueTrend = new Trend(uniqueBefore, uniqueAfter, false);
        weeks.put("uniqueBefore", uniqueBefore);
        weeks.put("uniqueAfter", uniqueAfter);
        weeks.put("uniqueTrend", uniqueTrend);

        Integer newBefore = db.query(PlayerCountQueries.newPlayerCount(twoWeeksAgo, oneWeekAgo));
        Integer newAfter = db.query(PlayerCountQueries.newPlayerCount(oneWeekAgo, now));
        Trend newTrend = new Trend(newBefore, newAfter, false);
        weeks.put("newBefore", newBefore);
        weeks.put("newAfter", newAfter);
        weeks.put("newTrend", newTrend);

        int regularBefore = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(oneWeekAgo, playtimeThreshold));
        int regularAfter = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(now, playtimeThreshold));
        weeks.put("regularBefore", regularBefore);
        weeks.put("regularAfter", regularAfter);
        weeks.put("regularTrend", new Trend(regularBefore, regularAfter, false));

        Long playtimeBefore = db.query(SessionQueries.playtime(twoWeeksAgo, oneWeekAgo));
        Long playtimeAfter = db.query(SessionQueries.playtime(oneWeekAgo, now));
        long avgPlaytimeBefore = uniqueBefore != 0 ? playtimeBefore / uniqueBefore : 0L;
        long avgPlaytimeAfter = uniqueAfter != 0 ? playtimeAfter / uniqueAfter : 0L;
        Trend avgPlaytimeTrend = new Trend(avgPlaytimeBefore, avgPlaytimeAfter, false);
        weeks.put("averagePlaytimeBefore", avgPlaytimeBefore);
        weeks.put("averagePlaytimeAfter", avgPlaytimeAfter);
        weeks.put("averagePlaytimeTrend", avgPlaytimeTrend);

        Long sessionsBefore = db.query(SessionQueries.sessionCount(twoWeeksAgo, oneWeekAgo));
        Long sessionsAfter = db.query(SessionQueries.sessionCount(oneWeekAgo, now));
        Trend sessionsTrend = new Trend(sessionsBefore, sessionsAfter, false);
        weeks.put("sessionsBefore", sessionsBefore);
        weeks.put("sessionsAfter", sessionsAfter);
        weeks.put("sessionsTrend", sessionsTrend);

        long avgSessionLengthBefore = sessionsBefore != 0 ? playtimeBefore / sessionsBefore : 0;
        long avgSessionLengthAfter = sessionsAfter != 0 ? playtimeAfter / sessionsAfter : 0;
        Trend avgSessionLengthTrend = new Trend(avgSessionLengthBefore, avgSessionLengthAfter, false);
        weeks.put("sessionLengthAverageBefore", avgSessionLengthBefore);
        weeks.put("sessionLengthAverageAfter", avgSessionLengthAfter);
        weeks.put("sessionLengthAverageTrend", avgSessionLengthTrend);

        return weeks;
    }
}