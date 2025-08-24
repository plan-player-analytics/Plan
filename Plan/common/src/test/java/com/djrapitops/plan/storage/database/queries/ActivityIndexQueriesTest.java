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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.playertable.NetworkTablePlayersQuery;
import com.djrapitops.plan.storage.database.queries.objects.playertable.ServerTablePlayersQuery;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static org.junit.jupiter.api.Assertions.*;

public interface ActivityIndexQueriesTest extends DatabaseTestPreparer {

    default void storeSessions(Predicate<FinishedSession> save) {
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        for (String world : worlds) {
            db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), world));
        }

        for (FinishedSession session : RandomData.randomSessions(serverUUID(), worlds, playerUUID, player2UUID)) {
            if (save.test(session)) db().executeTransaction(new StoreSessionTransaction(session));
        }
    }

//    @Test
//    default void activityIndexCoalesceSanityCheck() {
//        storeSessions();
//        Map<String, Integer> groupings = db().query(
//                ActivityIndexQueries.fetchActivityIndexGroupingsOn(System.currentTimeMillis(), serverUUID(), TimeUnit.HOURS.toMillis(2L))
//        );
//        Map<String, Integer> expected = Collections.singletonMap(ActivityIndex.getDefaultGroups()[4], 1); // Inactive
//        assertEquals(expected, groupings);
//    }

    @RepeatedTest(value = 3, name = "Activity Index calculations match {currentRepetition}/{totalRepetitions}")
    default void activityIndexCalculationsMatch() {
        storeSessions(session -> true);

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<FinishedSession> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new ServerTablePlayersQuery(serverUUID(), date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001, () -> {
            StringBuilder errorMsg = new StringBuilder("Activity indexes did not match\n");

            long week = TimeUnit.DAYS.toMillis(7L);
            long weekAgo = date - week;
            long twoWeeksAgo = date - 2L * week;
            long threeWeeksAgo = date - 3L * week;

            SessionsMutator mutator = new SessionsMutator(sessions);
            SessionsMutator w1 = mutator.filterSessionsBetween(weekAgo, date);
            SessionsMutator w2 = mutator.filterSessionsBetween(twoWeeksAgo, weekAgo);
            SessionsMutator w3 = mutator.filterSessionsBetween(threeWeeksAgo, twoWeeksAgo);

            Long dbW1 = db().query(SessionQueries.activePlaytime(weekAgo, date, serverUUID()));
            Long dbW2 = db().query(SessionQueries.activePlaytime(twoWeeksAgo, weekAgo, serverUUID()));
            Long dbW3 = db().query(SessionQueries.activePlaytime(threeWeeksAgo, twoWeeksAgo, serverUUID()));

            errorMsg.append("Java calculation playtimes: ")
                    .append(w1.toActivePlaytime()).append("ms,")
                    .append(w2.toActivePlaytime()).append("ms,")
                    .append(w3.toActivePlaytime()).append("ms\n")
                    .append("DB calculation playtimes: ")
                    .append(dbW1).append("ms,")
                    .append(dbW2).append("ms,")
                    .append(dbW3).append("ms");
            return errorMsg.toString();
        });
    }

    @RepeatedTest(value = 3, name = "Activity Index calculations match with missing data {currentRepetition}/{totalRepetitions}")
    default void activityIndexCalculationsMatchWithMissingData() {
        long keepAfter = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7L);
        storeSessions(session -> session.getDate() >= keepAfter && session.getEnd() >= keepAfter);

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<FinishedSession> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new ServerTablePlayersQuery(serverUUID(), date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001, () -> {
            StringBuilder errorMsg = new StringBuilder("Activity indexes did not match\n");

            long week = TimeUnit.DAYS.toMillis(7L);
            long weekAgo = date - week;
            long twoWeeksAgo = date - 2L * week;
            long threeWeeksAgo = date - 3L * week;

            SessionsMutator mutator = new SessionsMutator(sessions);
            SessionsMutator w1 = mutator.filterSessionsBetween(weekAgo, date);
            SessionsMutator w2 = mutator.filterSessionsBetween(twoWeeksAgo, weekAgo);
            SessionsMutator w3 = mutator.filterSessionsBetween(threeWeeksAgo, twoWeeksAgo);

            Long dbW1 = db().query(SessionQueries.activePlaytime(weekAgo, date, serverUUID()));
            Long dbW2 = db().query(SessionQueries.activePlaytime(twoWeeksAgo, weekAgo, serverUUID()));
            Long dbW3 = db().query(SessionQueries.activePlaytime(threeWeeksAgo, twoWeeksAgo, serverUUID()));

            errorMsg.append("Java calculation playtimes: ")
                    .append(w1.toActivePlaytime()).append("ms,")
                    .append(w2.toActivePlaytime()).append("ms,")
                    .append(w3.toActivePlaytime()).append("ms\n")
                    .append("DB calculation playtimes: ")
                    .append(dbW1).append("ms,")
                    .append(dbW2).append("ms,")
                    .append(dbW3).append("ms");
            return errorMsg.toString();
        });
    }

    @RepeatedTest(value = 3, name = "Network Activity Index calculations match {currentRepetition}/{totalRepetitions}")
    default void networkActivityIndexCalculationsMatch() {
        storeSessions(session -> true);

        long date = System.currentTimeMillis();
        long playtimeThreshold = TimeUnit.HOURS.toMillis(5L);
        List<FinishedSession> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID))
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        ActivityIndex javaCalculation = new ActivityIndex(sessions, date, playtimeThreshold);

        List<TablePlayer> players = db().query(new NetworkTablePlayersQuery(date, playtimeThreshold, 5));
        Optional<TablePlayer> found = players.stream().filter(tp -> playerUUID.equals(tp.getPlayerUUID())).findFirst();
        assertTrue(found.isPresent());
        Optional<ActivityIndex> currentActivityIndex = found.get().getCurrentActivityIndex();
        assertTrue(currentActivityIndex.isPresent());

        assertEquals(javaCalculation.getValue(), currentActivityIndex.get().getValue(), 0.001, () -> {
            StringBuilder errorMsg = new StringBuilder("Activity indexes did not match\n");

            long week = TimeUnit.DAYS.toMillis(7L);
            long weekAgo = date - week;
            long twoWeeksAgo = date - 2L * week;
            long threeWeeksAgo = date - 3L * week;

            SessionsMutator mutator = new SessionsMutator(sessions);
            SessionsMutator w1 = mutator.filterSessionsBetween(weekAgo, date);
            SessionsMutator w2 = mutator.filterSessionsBetween(twoWeeksAgo, weekAgo);
            SessionsMutator w3 = mutator.filterSessionsBetween(threeWeeksAgo, twoWeeksAgo);

            Long dbW1 = db().query(activePlaytime(weekAgo, date));
            Long dbW2 = db().query(activePlaytime(twoWeeksAgo, weekAgo));
            Long dbW3 = db().query(activePlaytime(threeWeeksAgo, twoWeeksAgo));

            errorMsg.append("Java calculation playtimes: ")
                    .append(w1.toActivePlaytime()).append("ms,")
                    .append(w2.toActivePlaytime()).append("ms,")
                    .append(w3.toActivePlaytime()).append("ms\n")
                    .append("DB calculation playtimes: ")
                    .append(dbW1).append("ms,")
                    .append(dbW2).append("ms,")
                    .append(dbW3).append("ms");
            return errorMsg.toString();
        });
    }

    default Query<Long> activePlaytime(long after, long before) {
        String sql = SELECT +
                "ux." + UsersTable.USER_UUID + ",COALESCE(active_playtime,0) AS active_playtime" +
                FROM + UsersTable.TABLE_NAME + " ux" +
                LEFT_JOIN + '(' + SELECT + SessionsTable.USER_ID +
                ",SUM(" + SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + '-' + SessionsTable.AFK_TIME + ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SESSION_END + ">=?" +
                AND + SessionsTable.SESSION_START + "<=?" +
                GROUP_BY + SessionsTable.USER_ID +
                ") sx on sx." + SessionsTable.USER_ID + "=ux." + UsersTable.ID;
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, after);
                statement.setLong(2, before);
            }

            @Override
            public Long processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getLong("active_playtime") : 0L;
            }
        };
    }

    @Test
    default void activeTurnedInactiveQueryHasAllParametersSet() {
        Integer result = db().query(ActivityIndexQueries.countRegularPlayersTurnedInactive(
                0, System.currentTimeMillis(), serverUUID(),
                TimeUnit.HOURS.toMillis(2L)
        ));
        assertNotNull(result);
    }

    @RepeatedTest(5)
    default void countRegularPlayers() {
        storeSessions(session -> true);
        long playtimeThreshold = TimeUnit.MILLISECONDS.toMillis(1L);
        Integer expected = 1; // All players are very active
        FinishedSession randomSession = RandomData.randomSession(serverUUID(), worlds, System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1L), playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(randomSession));
        Integer result = db().query(ActivityIndexQueries.fetchRegularPlayerCount(System.currentTimeMillis(), serverUUID(), playtimeThreshold));
        assertEquals(expected, result);
    }

    @Test
    default void noRegularPlayers() {
        storeSessions(session -> true);
        long playtimeThreshold = System.currentTimeMillis(); // Threshold is so high it's impossible to be regular
        Integer expected = 0;
        Integer result = db().query(ActivityIndexQueries.fetchRegularPlayerCount(System.currentTimeMillis(), serverUUID(), playtimeThreshold));
        assertEquals(expected, result);
    }
}