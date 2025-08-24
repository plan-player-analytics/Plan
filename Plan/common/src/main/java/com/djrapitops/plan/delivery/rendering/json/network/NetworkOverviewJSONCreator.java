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
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

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
 */
@Singleton
public class NetworkOverviewJSONCreator implements NetworkTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerSensor<?> serverSensor;
    private final ServerUptimeCalculator serverUptimeCalculator;

    @Inject
    public NetworkOverviewJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ServerSensor<?> serverSensor,
            ServerUptimeCalculator serverUptimeCalculator,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.serverSensor = serverSensor;
        this.serverUptimeCalculator = serverUptimeCalculator;
    }

    public Map<String, Object> createJSONAsMap() {
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

        sevenDays.put("unique_players_1d", db.query(PlayerCountQueries.uniquePlayerCount(dayAgo, now)));
        sevenDays.put("unique_players_7d", db.query(PlayerCountQueries.uniquePlayerCount(weekAgo, now)));
        sevenDays.put("unique_players_30d", db.query(PlayerCountQueries.uniquePlayerCount(monthAgo, now)));

        sevenDays.put("new_players_1d", db.query(PlayerCountQueries.newPlayerCount(dayAgo, now)));
        sevenDays.put("new_players_7d", db.query(PlayerCountQueries.newPlayerCount(weekAgo, now)));
        sevenDays.put("new_players_30d", db.query(PlayerCountQueries.newPlayerCount(monthAgo, now)));

        return sevenDays;
    }

    private Map<String, Object> createNumbersMap() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - TimeUnit.DAYS.toMillis(2L);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> numbers = new HashMap<>();

        Integer userCount = db.query(PlayerCountQueries.newPlayerCount(0L, now));
        numbers.put("total_players", userCount);
        numbers.put("regular_players", db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(now, playtimeThreshold)));
        numbers.put("online_players", serverSensor.getOnlinePlayerCount());
        ServerUUID serverUUID = serverInfo.getServerUUID();
        Optional<DateObj<Integer>> lastPeak = db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, twoDaysAgo));
        Optional<DateObj<Integer>> allTimePeak = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID));
        numbers.put("last_peak_date", lastPeak.map(DateObj::getDate).map(Object.class::cast).orElse("-"));
        numbers.put("last_peak_players", lastPeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        numbers.put("best_peak_date", allTimePeak.map(DateObj::getDate).map(Object.class::cast).orElse("-"));
        numbers.put("best_peak_players", allTimePeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        Long totalPlaytime = db.query(SessionQueries.playtime(0L, now));
        numbers.put("playtime", totalPlaytime);
        numbers.put("player_playtime", userCount != 0 ? totalPlaytime / userCount : "-");
        Long sessionCount = db.query(SessionQueries.sessionCount(0L, now));
        numbers.put("sessions", sessionCount);
        numbers.put("session_length_avg", sessionCount != 0 ? totalPlaytime / sessionCount : "-");
        numbers.put("current_uptime", serverUptimeCalculator.getServerUptimeMillis(serverUUID)
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
        weeks.put("unique_before", uniqueBefore);
        weeks.put("unique_after", uniqueAfter);
        weeks.put("unique_trend", uniqueTrend);

        Integer newBefore = db.query(PlayerCountQueries.newPlayerCount(twoWeeksAgo, oneWeekAgo));
        Integer newAfter = db.query(PlayerCountQueries.newPlayerCount(oneWeekAgo, now));
        Trend newTrend = new Trend(newBefore, newAfter, false);
        weeks.put("new_before", newBefore);
        weeks.put("new_after", newAfter);
        weeks.put("new_trend", newTrend);

        int regularBefore = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(oneWeekAgo, playtimeThreshold));
        int regularAfter = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(now, playtimeThreshold));
        weeks.put("regular_before", regularBefore);
        weeks.put("regular_after", regularAfter);
        weeks.put("regular_trend", new Trend(regularBefore, regularAfter, false));

        Long playtimeBefore = db.query(SessionQueries.playtime(twoWeeksAgo, oneWeekAgo));
        Long playtimeAfter = db.query(SessionQueries.playtime(oneWeekAgo, now));
        long avgPlaytimeBefore = uniqueBefore != 0 ? playtimeBefore / uniqueBefore : 0L;
        long avgPlaytimeAfter = uniqueAfter != 0 ? playtimeAfter / uniqueAfter : 0L;
        Trend avgPlaytimeTrend = new Trend(avgPlaytimeBefore, avgPlaytimeAfter, false);
        weeks.put("average_playtime_before", avgPlaytimeBefore);
        weeks.put("average_playtime_after", avgPlaytimeAfter);
        weeks.put("average_playtime_trend", avgPlaytimeTrend);

        Long sessionsBefore = db.query(SessionQueries.sessionCount(twoWeeksAgo, oneWeekAgo));
        Long sessionsAfter = db.query(SessionQueries.sessionCount(oneWeekAgo, now));
        Trend sessionsTrend = new Trend(sessionsBefore, sessionsAfter, false);
        weeks.put("sessions_before", sessionsBefore);
        weeks.put("sessions_after", sessionsAfter);
        weeks.put("sessions_trend", sessionsTrend);

        long avgSessionLengthBefore = sessionsBefore != 0 ? playtimeBefore / sessionsBefore : 0;
        long avgSessionLengthAfter = sessionsAfter != 0 ? playtimeAfter / sessionsAfter : 0;
        Trend avgSessionLengthTrend = new Trend(avgSessionLengthBefore, avgSessionLengthAfter, false);
        weeks.put("session_length_average_before", avgSessionLengthBefore);
        weeks.put("session_length_average_after", avgSessionLengthAfter);
        weeks.put("session_length_average_trend", avgSessionLengthTrend);

        return weeks;
    }
}