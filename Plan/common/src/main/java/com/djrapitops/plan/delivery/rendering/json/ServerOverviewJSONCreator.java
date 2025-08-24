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
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.ServerUptimeCalculator;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.ServerAggregateQueries;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.utilities.analysis.Percentage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /server-page Server Overview tab.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerOverviewJSONCreator implements ServerTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerSensor<?> serverSensor;

    private final Formatter<Double> decimals;
    private final Formatter<Double> percentage;
    private final ServerUptimeCalculator serverUptimeCalculator;

    @Inject
    public ServerOverviewJSONCreator(
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

        decimals = formatters.decimals();
        percentage = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap(ServerUUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("last_7_days", createLast7DaysMap(serverUUID));
        serverOverview.put("numbers", createNumbersMap(serverUUID));
        serverOverview.put("weeks", createWeeksMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createLast7DaysMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);

        Map<String, Object> sevenDays = new HashMap<>();

        sevenDays.put("unique_players", db.query(PlayerCountQueries.uniquePlayerCount(weekAgo, now, serverUUID)));
        sevenDays.put("unique_players_day", db.query(PlayerCountQueries.averageUniquePlayerCount(weekAgo, now, config.getTimeZone().getOffset(now), serverUUID)));

        int new7d = db.query(PlayerCountQueries.newPlayerCount(weekAgo, now, serverUUID));
        int retained7d = db.query(PlayerCountQueries.retainedPlayerCount(weekAgo, now, serverUUID));
        double retentionPercentage7d = Percentage.calculate(retained7d, new7d, -1);

        sevenDays.put("new_players", new7d);
        sevenDays.put("new_players_retention", retained7d);
        sevenDays.put("new_players_retention_perc", percentage.apply(retentionPercentage7d));
        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(weekAgo, now, serverUUID)));
        double averageTPS = tpsMutator.averageTPS();
        sevenDays.put("average_tps", averageTPS != -1 ? decimals.apply(averageTPS) : GenericLang.UNAVAILABLE.getKey());
        sevenDays.put("low_tps_spikes", tpsMutator.lowTpsSpikeCount(config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)));
        sevenDays.put("downtime", tpsMutator.serverDownTime());

        return sevenDays;
    }

    private Map<String, Object> createNumbersMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - TimeUnit.DAYS.toMillis(2L);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> numbers = new HashMap<>();

        Integer userCount = db.query(ServerAggregateQueries.serverUserCount(serverUUID));
        numbers.put("total_players", userCount);
        numbers.put("regular_players", db.query(ActivityIndexQueries.fetchRegularPlayerCount(now, serverUUID, playtimeThreshold)));
        numbers.put("online_players", getOnlinePlayers(serverUUID, db));
        Optional<DateObj<Integer>> lastPeak = db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, twoDaysAgo));
        Optional<DateObj<Integer>> allTimePeak = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID));
        numbers.put("last_peak_date", lastPeak.map(DateObj::getDate).map(Object.class::cast).orElse("-"));
        numbers.put("last_peak_players", lastPeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        numbers.put("best_peak_date", allTimePeak.map(DateObj::getDate).map(Object.class::cast).orElse("-"));
        numbers.put("best_peak_players", allTimePeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        Long totalPlaytime = db.query(SessionQueries.playtime(0L, now, serverUUID));
        numbers.put("playtime", totalPlaytime);
        numbers.put("player_playtime", userCount != 0 ? totalPlaytime / userCount : "-");
        numbers.put("sessions", db.query(SessionQueries.sessionCount(0L, now, serverUUID)));
        numbers.put("player_kills", db.query(KillQueries.playerKillCount(0L, now, serverUUID)));
        numbers.put("mob_kills", db.query(KillQueries.mobKillCount(0L, now, serverUUID)));
        numbers.put("deaths", db.query(KillQueries.deathCount(0L, now, serverUUID)));
        numbers.put("current_uptime", serverUptimeCalculator.getServerUptimeMillis(serverUUID)
                .map(Object.class::cast)
                .orElse(GenericLang.UNAVAILABLE.getKey()));

        return numbers;
    }

    private Object getOnlinePlayers(ServerUUID serverUUID, Database db) {
        return serverUUID.equals(serverInfo.getServerUUID())
                ? serverSensor.getOnlinePlayerCount()
                : db.query(TPSQueries.fetchLatestTPSEntryForServer(serverUUID))
                .map(TPS::getPlayers).map(Object::toString)
                .orElse(GenericLang.UNKNOWN.getKey());
    }

    private Map<String, Object> createWeeksMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long oneWeekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long twoWeeksAgo = now - TimeUnit.DAYS.toMillis(14L);
        Long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> weeks = new HashMap<>();

        weeks.put("start", twoWeeksAgo);
        weeks.put("midpoint", oneWeekAgo);
        weeks.put("end", now);

        Integer uniqueBefore = db.query(PlayerCountQueries.uniquePlayerCount(twoWeeksAgo, oneWeekAgo, serverUUID));
        Integer uniqueAfter = db.query(PlayerCountQueries.uniquePlayerCount(oneWeekAgo, now, serverUUID));
        Trend uniqueTrend = new Trend(uniqueBefore, uniqueAfter, false);
        weeks.put("unique_before", uniqueBefore);
        weeks.put("unique_after", uniqueAfter);
        weeks.put("unique_trend", uniqueTrend);

        Integer newBefore = db.query(PlayerCountQueries.newPlayerCount(twoWeeksAgo, oneWeekAgo, serverUUID));
        Integer newAfter = db.query(PlayerCountQueries.newPlayerCount(oneWeekAgo, now, serverUUID));
        Trend newTrend = new Trend(newBefore, newAfter, false);
        weeks.put("new_before", newBefore);
        weeks.put("new_after", newAfter);
        weeks.put("new_trend", newTrend);

        int regularBefore = db.query(ActivityIndexQueries.fetchRegularPlayerCount(oneWeekAgo, serverUUID, playtimeThreshold));
        int regularAfter = db.query(ActivityIndexQueries.fetchRegularPlayerCount(now, serverUUID, playtimeThreshold));
        weeks.put("regular_before", regularBefore);
        weeks.put("regular_after", regularAfter);
        weeks.put("regular_trend", new Trend(regularBefore, regularAfter, false));

        Long playtimeBefore = db.query(SessionQueries.playtime(twoWeeksAgo, oneWeekAgo, serverUUID));
        Long playtimeAfter = db.query(SessionQueries.playtime(oneWeekAgo, now, serverUUID));
        long avgPlaytimeBefore = uniqueBefore != 0 ? playtimeBefore / uniqueBefore : 0L;
        long avgPlaytimeAfter = uniqueAfter != 0 ? playtimeAfter / uniqueAfter : 0L;
        Trend avgPlaytimeTrend = new Trend(avgPlaytimeBefore, avgPlaytimeAfter, false);
        weeks.put("average_playtime_before", avgPlaytimeBefore);
        weeks.put("average_playtime_after", avgPlaytimeAfter);
        weeks.put("average_playtime_trend", avgPlaytimeTrend);

        Long sessionsBefore = db.query(SessionQueries.sessionCount(twoWeeksAgo, oneWeekAgo, serverUUID));
        Long sessionsAfter = db.query(SessionQueries.sessionCount(oneWeekAgo, now, serverUUID));
        Trend sessionsTrend = new Trend(sessionsBefore, sessionsAfter, false);
        weeks.put("sessions_before", sessionsBefore);
        weeks.put("sessions_after", sessionsAfter);
        weeks.put("sessions_trend", sessionsTrend);

        Long pksBefore = db.query(KillQueries.playerKillCount(twoWeeksAgo, oneWeekAgo, serverUUID));
        Long pksAfter = db.query(KillQueries.playerKillCount(oneWeekAgo, now, serverUUID));
        Trend pksTrend = new Trend(pksBefore, pksAfter, false);
        weeks.put("player_kills_before", pksBefore);
        weeks.put("player_kills_after", pksAfter);
        weeks.put("player_kills_trend", pksTrend);

        Long mkBefore = db.query(KillQueries.mobKillCount(twoWeeksAgo, oneWeekAgo, serverUUID));
        Long mkAfter = db.query(KillQueries.mobKillCount(oneWeekAgo, now, serverUUID));
        Trend mkTrend = new Trend(mkBefore, mkAfter, false);
        weeks.put("mob_kills_before", mkBefore);
        weeks.put("mob_kills_after", mkAfter);
        weeks.put("mob_kills_trend", mkTrend);

        Long deathsBefore = db.query(KillQueries.deathCount(twoWeeksAgo, oneWeekAgo, serverUUID));
        Long deathsAfter = db.query(KillQueries.deathCount(oneWeekAgo, now, serverUUID));
        Trend deathTrend = new Trend(deathsBefore, deathsAfter, true);
        weeks.put("deaths_before", deathsBefore);
        weeks.put("deaths_after", deathsAfter);
        weeks.put("deaths_trend", deathTrend);

        return weeks;
    }
}