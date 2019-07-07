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

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.store.objects.DateObj;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.ServerAggregateQueries;
import com.djrapitops.plan.db.access.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.db.access.queries.objects.KillQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.TPSQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Parses JSON payload for /server-page Server Overview tab.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerOverviewJSONParser {

    private PlanConfig config;
    private DBSystem dbSystem;
    private ServerInfo serverInfo;

    private Formatter<Long> timeAmountFormatter;
    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;
    private Formatter<DateHolder> dateFormatter;

    @Inject
    public ServerOverviewJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;

        dateFormatter = formatters.year();
        timeAmountFormatter = formatters.timeAmount();
        decimalFormatter = formatters.decimals();
        percentageFormatter = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap(UUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("last_7_days", createLast7DaysMap(serverUUID));
        serverOverview.put("numbers", createNumbersMap(serverUUID));
        serverOverview.put("weeks", createWeeksMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createLast7DaysMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long sevenDaysAgo = now - TimeUnit.DAYS.toMillis(7L);

        Map<String, Object> sevenDays = new HashMap<>();

        sevenDays.put("unique_players", db.query(PlayerCountQueries.uniquePlayerCount(sevenDaysAgo, now, serverUUID)));
        sevenDays.put("unique_players_day", db.query(PlayerCountQueries.uniquePlayerCountPerDay(sevenDaysAgo, now, serverUUID))); // TODO
        sevenDays.put("new_players", db.query(PlayerCountQueries.newPlayerCount(sevenDaysAgo, now, serverUUID)));
        sevenDays.put("new_players_retention", 0); // TODO
        sevenDays.put("new_players_retention_perc", percentageFormatter.apply(-1.0)); // TODO
        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(sevenDaysAgo, now, serverUUID)));
        sevenDays.put("average_tps", decimalFormatter.apply(tpsMutator.averageTPS()));
        sevenDays.put("low_tps_spikes", tpsMutator.lowTpsSpikeCount(config.getNumber(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)));
        sevenDays.put("downtime", timeAmountFormatter.apply(tpsMutator.serverDownTime()));

        return sevenDays;
    }

    private Map<String, Object> createNumbersMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoDaysAgo = now - TimeUnit.DAYS.toMillis(2L);

        Map<String, Object> numbers = new HashMap<>();

        numbers.put("total_players", db.query(ServerAggregateQueries.serverUserCount(serverUUID)));
        numbers.put("regular_players", 0); // TODO
        numbers.put("online_players", getOnlinePlayers(serverUUID, db));
        Optional<DateObj<Integer>> lastPeak = db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, twoDaysAgo));
        Optional<DateObj<Integer>> allTimePeak = db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID));
        numbers.put("last_peak_date", lastPeak.map(dateFormatter).orElse("-"));
        numbers.put("last_peak_players", lastPeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        numbers.put("best_peak_date", allTimePeak.map(dateFormatter).orElse("-"));
        numbers.put("best_peak_players", allTimePeak.map(dateObj -> dateObj.getValue().toString()).orElse("-"));
        numbers.put("playtime", timeAmountFormatter.apply(db.query(SessionQueries.playtime(0L, now, serverUUID))));
        numbers.put("player_playtime", "-"); // TODO
        numbers.put("sessions", db.query(SessionQueries.sessionCount(0L, now, serverUUID)));
        numbers.put("player_kills", db.query(KillQueries.playerKillCount(0L, now, serverUUID)));
        numbers.put("mob_kills", db.query(KillQueries.mobKillCount(0L, now, serverUUID)));
        numbers.put("deaths", db.query(KillQueries.deathCount(0L, now, serverUUID)));

        return numbers;
    }

    private Object getOnlinePlayers(UUID serverUUID, Database db) {
        return serverUUID.equals(serverInfo.getServerUUID())
                ? serverInfo.getServerProperties().getOnlinePlayers()
                : db.query(TPSQueries.fetchLatestTPSEntryForServer(serverUUID))
                .map(TPS::getPlayers).map(Object::toString)
                .orElse("Unknown");
    }

    private Map<String, Object> createWeeksMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long oneWeekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long twoWeeksAgo = now - TimeUnit.DAYS.toMillis(14L);

        Map<String, Object> weeks = new HashMap<>();

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

        weeks.put("regular_before", "-"); // TODO
        weeks.put("regular_after", "-");
        weeks.put("regular_trend", new Trend(0, 0, false));

        Long playtimeBefore = db.query(SessionQueries.playtime(twoWeeksAgo, oneWeekAgo, serverUUID));
        Long playtimeAfter = db.query(SessionQueries.playtime(oneWeekAgo, now, serverUUID));
        Trend playtimeTrend = new Trend(playtimeBefore, playtimeAfter, false, timeAmountFormatter);
        weeks.put("playtime_before", timeAmountFormatter.apply(playtimeBefore));
        weeks.put("playtime_after", timeAmountFormatter.apply(playtimeAfter));
        weeks.put("playtime_trend", playtimeTrend);

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