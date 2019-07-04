package com.djrapitops.plan.system.json;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.PlayersOnlineResolver;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.ServerAggregateQueries;
import com.djrapitops.plan.db.access.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.TPSQueries;
import com.djrapitops.plan.db.access.queries.objects.UserInfoQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Parses JSON payload for /server-page Online Activity Overview tab.
 *
 * @author Rsl1122
 */
@Singleton
public class OnlineActivityOverviewJSONParser {

    private PlanConfig config;
    private DBSystem dbSystem;

    private Formatter<Long> timeAmountFormatter;
    private Formatter<Double> decimalFormatter;
    private Formatter<Double> percentageFormatter;

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

        Map<String, Object> numbers = new HashMap<>();

        numbers.put("unique_players_30d", db.query(PlayerCountQueries.uniquePlayerCount(monthAgo, now, serverUUID)));
        numbers.put("unique_players_30d_trend", new Trend(
                db.query(PlayerCountQueries.uniquePlayerCount(monthAgo, halfMonthAgo, serverUUID)),
                db.query(PlayerCountQueries.uniquePlayerCount(halfMonthAgo, now, serverUUID)),
                false
        ));
        numbers.put("unique_players_7d", db.query(PlayerCountQueries.uniquePlayerCount(weekAgo, now, serverUUID)));
        numbers.put("unique_players_24h", db.query(PlayerCountQueries.uniquePlayerCount(dayAgo, now, serverUUID)));

        // TODO
        numbers.put("unique_players_30d_avg", -1);
        numbers.put("unique_players_30d_avg_trend", new Trend(
                -1,
                -1,
                false
        ));
        numbers.put("unique_players_7d_avg", -1);
        numbers.put("unique_players_24h_avg", -1);

        numbers.put("new_players_30d", db.query(PlayerCountQueries.newPlayerCount(monthAgo, now, serverUUID)));
        numbers.put("new_players_30d_trend", new Trend(
                db.query(PlayerCountQueries.newPlayerCount(monthAgo, halfMonthAgo, serverUUID)),
                db.query(PlayerCountQueries.newPlayerCount(halfMonthAgo, now, serverUUID)),
                false
        ));
        numbers.put("new_players_7d", db.query(PlayerCountQueries.newPlayerCount(weekAgo, now, serverUUID)));
        numbers.put("new_players_24h", db.query(PlayerCountQueries.newPlayerCount(dayAgo, now, serverUUID)));

        // TODO
        numbers.put("new_players_30d_avg", -1);
        numbers.put("new_players_30d_avg_trend", new Trend(
                -1,
                -1,
                false
        ));
        numbers.put("new_players_7d_avg", -1);
        numbers.put("new_players_24h_avg", -1);

        numbers.put("new_players_retention_30d", 0); // TODO
        numbers.put("new_players_retention_30d_perc", percentageFormatter.apply(-1.0)); // TODO
        numbers.put("new_players_retention_7d", 0); // TODO
        numbers.put("new_players_retention_7d_perc", percentageFormatter.apply(-1.0)); // TODO
        numbers.put("new_players_retention_24h", 0); // TODO
        numbers.put("new_players_retention_24h_perc", percentageFormatter.apply(-1.0)); // TODO

        Long playtimeMonth = db.query(ServerAggregateQueries.totalPlaytime(monthAgo, now, serverUUID));
        Long playtimeWeek = db.query(ServerAggregateQueries.totalPlaytime(weekAgo, now, serverUUID));
        Long playtimeDay = db.query(ServerAggregateQueries.totalPlaytime(dayAgo, now, serverUUID));
        Long playtimeBefore = db.query(ServerAggregateQueries.totalPlaytime(monthAgo, halfMonthAgo, serverUUID));
        Long playtimeAfter = db.query(ServerAggregateQueries.totalPlaytime(halfMonthAgo, now, serverUUID));
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

        Long sessionsMonth = db.query(ServerAggregateQueries.sessionCount(monthAgo, now, serverUUID));
        Long sessionsWeek = db.query(ServerAggregateQueries.sessionCount(weekAgo, now, serverUUID));
        Long sessionsDay = db.query(ServerAggregateQueries.sessionCount(dayAgo, now, serverUUID));
        Long sessionsBefore = db.query(ServerAggregateQueries.sessionCount(monthAgo, halfMonthAgo, serverUUID));
        Long sessionsAfter = db.query(ServerAggregateQueries.sessionCount(halfMonthAgo, now, serverUUID));
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