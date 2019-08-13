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
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.PlayersOnlineResolver;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.TPSQueries;
import com.djrapitops.plan.db.access.queries.objects.UserInfoQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Parses JSON payload for /server-page Online Activity Overview tab.
 *
 * @author Rsl1122
 */
@Singleton
public class OnlineActivityOverviewJSONParser implements TabJSONParser<Map<String, Object>> {

    private PlanConfig config;
    private DBSystem dbSystem;

    private Formatter<Long> timeAmountFormatter;
    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;
    private final TimeZone timeZone;

    @Inject
    public OnlineActivityOverviewJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        timeAmountFormatter = formatters.timeAmount();
        decimalFormatter = formatters.decimals();
        percentageFormatter = formatters.percentage();
        this.timeZone = config.get(TimeSettings.USE_SERVER_TIME) ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT");
    }

    public Map<String, Object> createJSONAsMap(UUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("numbers", createNumbersMap(serverUUID));
        serverOverview.put("insights", createInsightsMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createNumbersMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(15L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        int timeZoneOffset = timeZone.getOffset(now);

        Map<String, Object> numbers = new HashMap<>();

        numbers.put("unique_players_30d", db.query(PlayerCountQueries.uniquePlayerCount(monthAgo, now, serverUUID)));
        numbers.put("unique_players_30d_trend", new Trend(
                db.query(PlayerCountQueries.uniquePlayerCount(monthAgo, halfMonthAgo, serverUUID)),
                db.query(PlayerCountQueries.uniquePlayerCount(halfMonthAgo, now, serverUUID)),
                false
        ));
        numbers.put("unique_players_7d", db.query(PlayerCountQueries.uniquePlayerCount(weekAgo, now, serverUUID)));
        numbers.put("unique_players_24h", db.query(PlayerCountQueries.uniquePlayerCount(dayAgo, now, serverUUID)));

        numbers.put("unique_players_30d_avg", db.query(PlayerCountQueries.averageUniquePlayerCount(monthAgo, now, timeZoneOffset, serverUUID)));
        numbers.put("unique_players_30d_avg_trend", new Trend(
                db.query(PlayerCountQueries.averageUniquePlayerCount(monthAgo, halfMonthAgo, timeZoneOffset, serverUUID)),
                db.query(PlayerCountQueries.averageUniquePlayerCount(halfMonthAgo, now, timeZoneOffset, serverUUID)),
                false
        ));
        numbers.put("unique_players_7d_avg", db.query(PlayerCountQueries.averageUniquePlayerCount(weekAgo, now, timeZoneOffset, serverUUID)));
        numbers.put("unique_players_24h_avg", db.query(PlayerCountQueries.averageUniquePlayerCount(dayAgo, now, timeZoneOffset, serverUUID)));

        Integer new30d = db.query(PlayerCountQueries.newPlayerCount(monthAgo, now, serverUUID));
        Integer new7d = db.query(PlayerCountQueries.newPlayerCount(weekAgo, now, serverUUID));
        numbers.put("new_players_30d", new30d);
        numbers.put("new_players_30d_trend", new Trend(
                db.query(PlayerCountQueries.newPlayerCount(monthAgo, halfMonthAgo, serverUUID)),
                db.query(PlayerCountQueries.newPlayerCount(halfMonthAgo, now, serverUUID)),
                false
        ));
        numbers.put("new_players_7d", new7d);
        numbers.put("new_players_24h", db.query(PlayerCountQueries.newPlayerCount(dayAgo, now, serverUUID)));

        numbers.put("new_players_30d_avg", db.query(PlayerCountQueries.averageNewPlayerCount(monthAgo, now, timeZoneOffset, serverUUID)));
        numbers.put("new_players_30d_avg_trend", new Trend(
                db.query(PlayerCountQueries.averageNewPlayerCount(monthAgo, halfMonthAgo, timeZoneOffset, serverUUID)),
                db.query(PlayerCountQueries.averageNewPlayerCount(halfMonthAgo, now, timeZoneOffset, serverUUID)),
                false
        ));
        numbers.put("new_players_7d_avg", db.query(PlayerCountQueries.averageNewPlayerCount(weekAgo, now, timeZoneOffset, serverUUID)));
        numbers.put("new_players_24h_avg", db.query(PlayerCountQueries.averageNewPlayerCount(dayAgo, now, timeZoneOffset, serverUUID)));

        int retained30d = db.query(PlayerCountQueries.retainedPlayerCount(monthAgo, now, serverUUID));
        int retained7d = db.query(PlayerCountQueries.retainedPlayerCount(weekAgo, now, serverUUID));
        double retentionPerc30d = new30d != 0 ? (double) retained30d / new30d : -1;
        double retentionPerc7d = new7d != 0 ? (double) retained7d / new7d : -1;
        numbers.put("new_players_retention_30d", retained30d);
        numbers.put("new_players_retention_30d_perc", percentageFormatter.apply(retentionPerc30d));
        numbers.put("new_players_retention_7d", retained7d);
        numbers.put("new_players_retention_7d_perc", percentageFormatter.apply(retentionPerc7d));
        numbers.put("new_players_retention_24h", 0); // TODO
        numbers.put("new_players_retention_24h_perc", percentageFormatter.apply(-1.0)); // TODO

        Long playtimeMonth = db.query(SessionQueries.playtime(monthAgo, now, serverUUID));
        Long playtimeWeek = db.query(SessionQueries.playtime(weekAgo, now, serverUUID));
        Long playtimeDay = db.query(SessionQueries.playtime(dayAgo, now, serverUUID));
        Long playtimeBefore = db.query(SessionQueries.playtime(monthAgo, halfMonthAgo, serverUUID));
        Long playtimeAfter = db.query(SessionQueries.playtime(halfMonthAgo, now, serverUUID));
        numbers.put("playtime_30d", timeAmountFormatter.apply(playtimeMonth));
        numbers.put("playtime_30d_trend", new Trend(playtimeBefore, playtimeAfter, false, timeAmountFormatter));
        numbers.put("playtime_7d", timeAmountFormatter.apply(playtimeWeek));
        numbers.put("playtime_24h", timeAmountFormatter.apply(playtimeDay));

        // TODO
        numbers.put("playtime_30d_avg", timeAmountFormatter.apply(-1L));
        numbers.put("playtime_30d_avg_trend", new Trend(
                -1,
                -1,
                false,
                timeAmountFormatter
        ));
        numbers.put("playtime_7d_avg", timeAmountFormatter.apply(-1L));
        numbers.put("playtime_24h_avg", timeAmountFormatter.apply(-1L));

        Long sessionsMonth = db.query(SessionQueries.sessionCount(monthAgo, now, serverUUID));
        Long sessionsWeek = db.query(SessionQueries.sessionCount(weekAgo, now, serverUUID));
        Long sessionsDay = db.query(SessionQueries.sessionCount(dayAgo, now, serverUUID));
        Long sessionsBefore = db.query(SessionQueries.sessionCount(monthAgo, halfMonthAgo, serverUUID));
        Long sessionsAfter = db.query(SessionQueries.sessionCount(halfMonthAgo, now, serverUUID));
        numbers.put("sessions_30d", sessionsMonth);
        numbers.put("sessions_30d_trend", new Trend(sessionsBefore, sessionsAfter, false));
        numbers.put("sessions_7d", sessionsWeek);
        numbers.put("sessions_24h", sessionsDay);

        Long sessionLengthAvgMonth = sessionsMonth != 0 ? playtimeMonth / sessionsMonth : 0;
        Long sessionLengthAvgWeek = sessionsWeek != 0 ? playtimeWeek / sessionsWeek : 0;
        Long sessionLengthAvgDay = sessionsDay != 0 ? playtimeDay / sessionsDay : 0;
        numbers.put("session_length_30d_avg", timeAmountFormatter.apply(sessionLengthAvgMonth));
        numbers.put("session_length_30d_trend", new Trend(
                sessionsBefore != 0 ? playtimeBefore / sessionsBefore : 0,
                sessionsAfter != 0 ? playtimeAfter / sessionsAfter : 0,
                false,
                timeAmountFormatter
        ));
        numbers.put("session_length_7d_avg", timeAmountFormatter.apply(sessionLengthAvgWeek));
        numbers.put("session_length_24h_avg", timeAmountFormatter.apply(sessionLengthAvgDay));

        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(monthAgo, now, serverUUID)));
        numbers.put("average_tps", decimalFormatter.apply(tpsMutator.averageTPS()));
        numbers.put("low_tps_spikes", tpsMutator.lowTpsSpikeCount(config.getNumber(DisplaySettings.GRAPH_TPS_THRESHOLD_MED)));
        numbers.put("downtime", timeAmountFormatter.apply(tpsMutator.serverDownTime()));

        return numbers;
    }

    private Map<String, Object> createInsightsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> insights = new HashMap<>();

        SessionsMutator sessions = new SessionsMutator(db.query(SessionQueries.fetchServerSessionsWithoutKillOrWorldData(monthAgo, now, serverUUID)));
        List<TPS> tpsData = db.query(TPSQueries.fetchTPSDataOfServer(monthAgo, now, serverUUID));
        Map<UUID, Long> registerDates = db.query(UserInfoQueries.fetchRegisterDates(monthAgo, now, serverUUID));

        PlayersOnlineResolver playersOnlineResolver = new PlayersOnlineResolver(new TPSMutator(tpsData));
        SessionsMutator firstSessions = sessions.filterBy(session -> {
            long registered = registerDates.getOrDefault(session.getValue(SessionKeys.UUID).orElse(null), -501L);
            long start = session.getDate();
            return Math.abs(registered - start) < 500L;
        });
        SessionsMutator firstSessionsBefore = firstSessions.filterSessionsBetween(monthAgo, halfMonthAgo);
        SessionsMutator firstSessionsAfter = firstSessions.filterSessionsBetween(halfMonthAgo, now);

        long avgSessionLength = firstSessions.toAverageSessionLength();
        long avgSessionLengthBefore = firstSessionsBefore.toAverageSessionLength();
        long avgSessionLengthAfter = firstSessionsAfter.toAverageSessionLength();
        insights.put("first_session_length_avg", timeAmountFormatter.apply(avgSessionLength));
        insights.put("first_session_length_trend", new Trend(avgSessionLengthBefore, avgSessionLengthAfter, false, timeAmountFormatter));

        int lonelyJoins = playersOnlineResolver.findLonelyJoins(sessions.toSessionStarts());
        int loneJoinsBefore = playersOnlineResolver.findLonelyJoins(sessions.filterSessionsBetween(monthAgo, halfMonthAgo).toSessionStarts());
        int loneJoinsAfter = playersOnlineResolver.findLonelyJoins(sessions.filterSessionsBetween(halfMonthAgo, now).toSessionStarts());
        insights.put("lone_joins", lonelyJoins);
        insights.put("lone_joins_trend", new Trend(loneJoinsBefore, loneJoinsAfter, true));

        int newLonelyJoins = playersOnlineResolver.findLonelyJoins(firstSessions.toSessionStarts());
        int newLoneJoinsBefore = playersOnlineResolver.findLonelyJoins(firstSessionsBefore.toSessionStarts());
        int newLoneJoinsAfter = playersOnlineResolver.findLonelyJoins(firstSessionsAfter.toSessionStarts());
        insights.put("lone_new_joins", newLonelyJoins);
        insights.put("lone_new_joins_trend", new Trend(newLoneJoinsBefore, newLoneJoinsAfter, true));

        double playersOnlineOnRegister = firstSessions.toAveragePlayersOnline(playersOnlineResolver);
        double playersOnlineOnRegisterBefore = firstSessionsBefore.toAveragePlayersOnline(playersOnlineResolver);
        double playersOnlineOnRegisterAfter = firstSessionsAfter.toAveragePlayersOnline(playersOnlineResolver);
        insights.put("players_first_join_avg", decimalFormatter.apply(playersOnlineOnRegister));
        insights.put("players_first_join_trend", new Trend(playersOnlineOnRegisterBefore, playersOnlineOnRegisterAfter, false, decimalFormatter));

        return insights;
    }
}