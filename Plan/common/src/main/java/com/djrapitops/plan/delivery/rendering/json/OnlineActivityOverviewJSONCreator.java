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

import com.djrapitops.plan.delivery.domain.mutators.PlayersOnlineResolver;
import com.djrapitops.plan.delivery.domain.mutators.RetentionData;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.utilities.analysis.Percentage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /server-page Online Activity Overview tab.
 *
 * @author AuroraLS3
 */
@Singleton
public class OnlineActivityOverviewJSONCreator implements ServerTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final Formatter<Double> decimalFormatter;
    private final Formatter<Double> percentageFormatter;

    @Inject
    public OnlineActivityOverviewJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        decimalFormatter = formatters.decimals();
        percentageFormatter = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap(ServerUUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("numbers", createNumbersMap(serverUUID));
        serverOverview.put("insights", createInsightsMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createNumbersMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long dayAgo = now - TimeUnit.DAYS.toMillis(1L);
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(15L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        int timeZoneOffset = config.getTimeZone().getOffset(now);
        Long playThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

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
        Integer new1d = db.query(PlayerCountQueries.newPlayerCount(dayAgo, now, serverUUID));
        numbers.put("new_players_30d", new30d);
        numbers.put("new_players_30d_trend", new Trend(
                db.query(PlayerCountQueries.newPlayerCount(monthAgo, halfMonthAgo, serverUUID)),
                db.query(PlayerCountQueries.newPlayerCount(halfMonthAgo, now, serverUUID)),
                false
        ));
        numbers.put("new_players_7d", new7d);
        numbers.put("new_players_24h", new1d);

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
        double retentionPercentage30d = Percentage.calculate(retained30d, new30d, -1);
        double retentionPercentage7d = Percentage.calculate(retained7d, new7d, -1);
        numbers.put("new_players_retention_30d", retained30d);
        numbers.put("new_players_retention_30d_perc", percentageFormatter.apply(retentionPercentage30d));
        numbers.put("new_players_retention_7d", retained7d);
        numbers.put("new_players_retention_7d_perc", percentageFormatter.apply(retentionPercentage7d));

        int prediction1d = RetentionData.countRetentionPrediction(
                db.query(ActivityIndexQueries.activityIndexForNewPlayers(dayAgo, now, serverUUID, playThreshold)),
                db.query(ActivityIndexQueries.averageActivityIndexForRetainedPlayers(monthAgo, now, serverUUID, playThreshold)),
                db.query(ActivityIndexQueries.averageActivityIndexForNonRetainedPlayers(monthAgo, now, serverUUID, playThreshold))
        );
        double retentionPercentage1d = Percentage.calculate(prediction1d, new1d, -1);
        numbers.put("new_players_retention_24h", prediction1d);
        numbers.put("new_players_retention_24h_perc", percentageFormatter.apply(retentionPercentage1d));

        Long playtimeMonth = db.query(SessionQueries.playtime(monthAgo, now, serverUUID));
        Long playtimeWeek = db.query(SessionQueries.playtime(weekAgo, now, serverUUID));
        Long playtimeDay = db.query(SessionQueries.playtime(dayAgo, now, serverUUID));
        Long playtimeBefore = db.query(SessionQueries.playtime(monthAgo, halfMonthAgo, serverUUID));
        Long playtimeAfter = db.query(SessionQueries.playtime(halfMonthAgo, now, serverUUID));
        numbers.put("playtime_30d", playtimeMonth);
        numbers.put("playtime_30d_trend", new Trend(playtimeBefore, playtimeAfter, false));
        numbers.put("playtime_7d", playtimeWeek);
        numbers.put("playtime_24h", playtimeDay);

        numbers.put("playtime_30d_avg", db.query(SessionQueries.averagePlaytimePerDay(monthAgo, now, timeZoneOffset, serverUUID)));
        numbers.put("playtime_30d_avg_trend", new Trend(
                db.query(SessionQueries.averagePlaytimePerDay(monthAgo, halfMonthAgo, timeZoneOffset, serverUUID)),
                db.query(SessionQueries.averagePlaytimePerDay(halfMonthAgo, now, timeZoneOffset, serverUUID)),
                false
        ));
        numbers.put("playtime_7d_avg", db.query(SessionQueries.averagePlaytimePerDay(weekAgo, now, timeZoneOffset, serverUUID)));
        numbers.put("playtime_24h_avg", db.query(SessionQueries.playtime(dayAgo, now, serverUUID)));

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
        numbers.put("session_length_30d_avg", sessionLengthAvgMonth);
        numbers.put("session_length_30d_trend", new Trend(
                sessionsBefore != 0 ? playtimeBefore / sessionsBefore : 0,
                sessionsAfter != 0 ? playtimeAfter / sessionsAfter : 0,
                false
        ));
        numbers.put("session_length_7d_avg", sessionLengthAvgWeek);
        numbers.put("session_length_24h_avg", sessionLengthAvgDay);

        return numbers;
    }

    private Map<String, Object> createInsightsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(15L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> insights = new HashMap<>();

        SessionsMutator sessions = new SessionsMutator(db.query(SessionQueries.fetchServerSessionsWithoutKillOrWorldData(monthAgo, now, serverUUID)));
        List<TPS> tpsData = db.query(TPSQueries.fetchTPSDataOfServer(monthAgo, now, serverUUID));
        Map<UUID, Long> registerDates = db.query(UserInfoQueries.fetchRegisterDates(monthAgo, now, serverUUID));

        PlayersOnlineResolver playersOnlineResolver = new PlayersOnlineResolver(new TPSMutator(tpsData));
        SessionsMutator firstSessions = sessions.filterBy(session -> {
            long registered = registerDates.getOrDefault(session.getPlayerUUID(), -501L);
            long start = session.getDate();
            return Math.abs(registered - start) < 500L;
        });
        SessionsMutator firstSessionsBefore = firstSessions.filterSessionsBetween(monthAgo, halfMonthAgo);
        SessionsMutator firstSessionsAfter = firstSessions.filterSessionsBetween(halfMonthAgo, now);

        long avgFirstSessionLength = firstSessions.toAverageSessionLength();
        long avgFirstSessionLengthBefore = firstSessionsBefore.toAverageSessionLength();
        long avgFirstSessionLengthAfter = firstSessionsAfter.toAverageSessionLength();
        insights.put("first_session_length_avg", avgFirstSessionLength);
        insights.put("first_session_length_trend", new Trend(avgFirstSessionLengthBefore, avgFirstSessionLengthAfter, false));

        long medianFirstSessionLength = firstSessions.toMedianSessionLength();
        long medianFirstSessionLengthBefore = firstSessionsBefore.toMedianSessionLength();
        long medianFirstSessionLengthAfter = firstSessionsAfter.toMedianSessionLength();
        insights.put("first_session_length_median", medianFirstSessionLength);
        insights.put("first_session_length_median_trend", new Trend(medianFirstSessionLengthBefore, medianFirstSessionLengthAfter, false));

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