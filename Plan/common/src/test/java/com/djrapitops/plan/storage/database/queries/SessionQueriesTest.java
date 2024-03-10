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
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.containers.PlayerContainerQuery;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;
import com.djrapitops.plan.storage.database.queries.objects.playertable.ServerTablePlayersQuery;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.utilities.java.Maps;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public interface SessionQueriesTest extends DatabaseTestPreparer {

    @Test
    default void sessionStoreTransactionOutOfOrderDoesNotFailDueToMissingMainUser() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);

        db().executeTransaction(new StoreSessionTransaction(session));

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void sessionStoreTransactionOutOfOrderDoesNotFailDueToMissingKilledUser() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        // Query doesn't fetch kills since the uuid for killed player is missing
        session.getExtraData(PlayerKills.class)
                .map(PlayerKills::asList)
                .ifPresent(List::clear);

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void shutdownDataPreservationTransactionOutOfOrderDoesNotFailDueToMissingMainUser() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);

        db().executeTransaction(new ShutdownDataPreservationTransaction(List.of(session)));

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void shutdownDataPreservationTransactionOutOfOrderDoesNotFailDueToMissingKilledUser() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new ShutdownDataPreservationTransaction(List.of(session)));

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void killsAreAvailableAfter2ndUserRegisterEvenIfOutOfOrder() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));
        // killed user is registered after session already ended.
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void sessionStoreTransactionOutOfOrderUpdatesUserInformation() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        long registerDate = RandomData.randomTime();
        db().executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> registerDate, TestConstants.PLAYER_ONE_NAME));

        Optional<BaseUser> expected = Optional.of(new BaseUser(playerUUID, TestConstants.PLAYER_ONE_NAME, registerDate, 0));
        Optional<BaseUser> result = db().query(BaseUserQueries.fetchBaseUserOfPlayer(playerUUID));
        assertEquals(expected, result);
    }

    @Test
    default void sessionPlaytimeIsCalculatedCorrectlyAfterStorage() {
        prepareForSessionSave();

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        long expectedLength = session.getLength();
        long sessionEnd = session.getEnd();

        db().executeTransaction(new StoreSessionTransaction(session));

        forcePersistenceCheck();

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        assertTrue(sessions.containsKey(serverUUID()));

        SessionsMutator sessionsMutator = new SessionsMutator(sessions.get(serverUUID()));
        assertEquals(expectedLength, sessionsMutator.toPlaytime());
        assertEquals(1, sessionsMutator.count());

        SessionsMutator afterTimeSessionsMutator = sessionsMutator.filterSessionsBetween(sessionEnd + 1L, System.currentTimeMillis());
        assertEquals(0L, afterTimeSessionsMutator.toPlaytime());
        assertEquals(0, afterTimeSessionsMutator.count());
    }

    default void prepareForSessionSave() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, RandomData::randomTime,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
    }

    @Test
    default void sessionsAreStoredWithAllData() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        forcePersistenceCheck();

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void mostRecentSessionsCanBeQueried() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        List<FinishedSession> expected = Collections.singletonList(session);
        List<FinishedSession> result = db().query(SessionQueries.fetchLatestSessionsOfServer(serverUUID(), 1));
        assertEquals(expected, result);
    }

    @Test
    default void worldTimesAreSavedWithAllSessionSave() {
        prepareForSessionSave();

        WorldTimes worldTimes = RandomData.randomWorldTimes(worlds);
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID);
        session.getExtraData().put(WorldTimes.class, worldTimes);
        List<FinishedSession> sessions = Collections.singletonList(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        Map<ServerUUID, WorldTimes> saved = db().query(WorldTimesQueries.fetchPlayerWorldTimesOnServers(playerUUID));
        WorldTimes savedWorldTimes = saved.get(serverUUID());
        assertEquals(worldTimes, savedWorldTimes);
    }

    @Test
    default void worldTimesAreSavedWithSession() {
        prepareForSessionSave();

        WorldTimes worldTimes = RandomData.randomWorldTimes(worlds);
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID);
        session.getExtraData().put(WorldTimes.class, worldTimes);
        List<FinishedSession> sessions = new ArrayList<>();
        sessions.add(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        List<FinishedSession> allSessions = db().query(SessionQueries.fetchAllSessions());

        assertEquals(worldTimes, allSessions.get(0).getExtraData(WorldTimes.class)
                .orElseThrow(AssertionError::new));
    }

    @Test
    default void worldTimesAreSavedWithSessionWithoutWorlds() {
        prepareForSessionSave();
        // Remove the worlds from the database so that they need to also be saved.
        execute(new ExecStatement("DELETE FROM " + WorldTable.TABLE_NAME) {
            @Override
            public void prepare(PreparedStatement statement) {
                // Nothing needed
            }
        });

        WorldTimes worldTimes = RandomData.randomWorldTimes(worlds);
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID);
        session.getExtraData().put(WorldTimes.class, worldTimes);
        List<FinishedSession> sessions = new ArrayList<>();
        sessions.add(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        List<FinishedSession> allSessions = db().query(SessionQueries.fetchAllSessions());

        assertEquals(worldTimes, allSessions.get(0).getExtraData(WorldTimes.class)
                .orElseThrow(AssertionError::new));
    }

    @Test
    default void playersWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        FinishedSession session = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID)).get(serverUUID()).get(0);
        WorldTimes expected = session.getExtraData(WorldTimes.class).orElseThrow(AssertionError::new);
        WorldTimes worldTimesOfUser = db().query(WorldTimesQueries.fetchPlayerTotalWorldTimes(playerUUID));
        assertEquals(expected, worldTimesOfUser);
    }

    @Test
    default void serverWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        FinishedSession session = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID)).get(serverUUID()).get(0);
        WorldTimes expected = session.getExtraData(WorldTimes.class).orElseThrow(AssertionError::new);
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));
        assertEquals(expected, worldTimesOfServer);
    }

    @Test
    default void emptyServerWorldTimesIsEmpty() {
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));
        assertEquals(new WorldTimes(), worldTimesOfServer);
    }

    @Test
    default void playerSessionsAreFetchedByServerUUID() {
        prepareForSessionSave();

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        forcePersistenceCheck();

        Map<ServerUUID, List<FinishedSession>> expected = Collections.singletonMap(serverUUID(), Collections.singletonList(session));
        Map<ServerUUID, List<FinishedSession>> fetched = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        assertEquals(expected, fetched);
    }

    @Test
    default void testKillTableGetKillsOfServer() {
        prepareForSessionSave();

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        List<PlayerKill> expected = session.getExtraData(PlayerKills.class).map(PlayerKills::asList)
                .orElseThrow(AssertionError::new);
        db().executeTransaction(new StoreSessionTransaction(session));

        forcePersistenceCheck();

        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> savedSessions = sessions.get(serverUUID());
        assertNotNull(savedSessions);
        assertFalse(savedSessions.isEmpty());

        List<PlayerKill> got = savedSessions.get(0).getExtraData(PlayerKills.class).map(PlayerKills::asList)
                .orElseThrow(AssertionError::new);
        assertEquals(expected, got);
    }

    @Test
    default void sessionWorldTimesAreFetchedCorrectly() {
        prepareForSessionSave();

        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        WorldTimes expected = session.getExtraData(WorldTimes.class).orElseThrow(AssertionError::new);
        db().executeTransaction(new StoreSessionTransaction(session));

        // Fetch the session
        Map<ServerUUID, List<FinishedSession>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<FinishedSession> serverSessions = sessions.get(serverUUID());
        assertNotNull(serverSessions);
        assertFalse(serverSessions.isEmpty());

        WorldTimes got = serverSessions.get(0).getExtraData(WorldTimes.class).orElseThrow(AssertionError::new);
        assertEquals(expected, got);
    }

    @Test
    default void removeEverythingRemovesSessions() {
        sessionsAreStoredWithAllData();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(SessionQueries.fetchAllSessions()).isEmpty());
        assertEquals(0L, db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID())).getTotal());
        assertEquals(0, db().query(KillQueries.playerKillCount(0, System.currentTimeMillis(), serverUUID())));
    }

    @Test
    default void removeEverythingRemovesWorldNames() {
        prepareForSessionSave();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(LargeFetchQueries.fetchAllWorldNames()).isEmpty());
    }


    @Test
    default void worldNamesAreStored() {
        String[] expected = {"Test", "Test2", "Test3"};
        for (String worldName : expected) {
            db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worldName));
        }

        forcePersistenceCheck();

        Collection<String> result = db().query(LargeFetchQueries.fetchAllWorldNames()).get(serverUUID());
        assertEquals(new HashSet<>(Arrays.asList(expected)), result);
    }

    @RepeatedTest(value = 3, name = "Players table and Player page playtimes match {currentRepetition}/{totalRepetitions}")
    default void playersTableAndPlayerPagePlaytimeMatches() {
        prepareForSessionSave();
        List<FinishedSession> player1Sessions = RandomData.randomSessions(serverUUID(), worlds, playerUUID, player2UUID);
        List<FinishedSession> player2Sessions = RandomData.randomSessions(serverUUID(), worlds, player2UUID, playerUUID);
        player1Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));
        player2Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));

        long playtimeThreshold = RandomData.randomLong(TimeUnit.HOURS.toMillis(1L), TimeUnit.DAYS.toMillis(2L));

        PlayerContainer playerContainer = db().query(new PlayerContainerQuery(playerUUID));
        TablePlayer tablePlayer = db().query(new ServerTablePlayersQuery(serverUUID(), System.currentTimeMillis(), playtimeThreshold, 5))
                .stream().filter(player -> playerUUID.equals(player.getPlayerUUID())).findAny()
                .orElseThrow(AssertionError::new);

        long expected = SessionsMutator.forContainer(playerContainer).toActivePlaytime();
        long got = tablePlayer.getActivePlaytime().orElseThrow(AssertionError::new);
        assertEquals(expected, got);
    }

    @RepeatedTest(value = 3, name = "Players table and player page Activity Index calculations match {currentRepetition}/{totalRepetitions}")
    default void playersTableAndPlayerPageActivityIndexMatches() {
        prepareForSessionSave();
        List<FinishedSession> player1Sessions = RandomData.randomSessions(serverUUID(), worlds, playerUUID, player2UUID);
        List<FinishedSession> player2Sessions = RandomData.randomSessions(serverUUID(), worlds, player2UUID, playerUUID);
        player1Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));
        player2Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));

        long time = System.currentTimeMillis();
        long playtimeThreshold = RandomData.randomLong(TimeUnit.HOURS.toMillis(1L), TimeUnit.DAYS.toMillis(2L));

        PlayerContainer playerContainer = db().query(new PlayerContainerQuery(playerUUID));
        TablePlayer tablePlayer = db().query(new ServerTablePlayersQuery(serverUUID(), time, playtimeThreshold, 5))
                .stream().filter(player -> playerUUID.equals(player.getPlayerUUID())).findAny()
                .orElseThrow(AssertionError::new);

        SessionsMutator sessionsMutator = SessionsMutator.forContainer(playerContainer);
        long week = TimeAmount.WEEK.toMillis(1L);
        long weekAgo = time - week;
        long twoWeeksAgo = time - 2L * week;
        long threeWeeksAgo = time - 3L * week;
        SessionsMutator weekOne = sessionsMutator.filterSessionsBetween(weekAgo, time);
        SessionsMutator weekTwo = sessionsMutator.filterSessionsBetween(twoWeeksAgo, weekAgo);
        SessionsMutator weekThree = sessionsMutator.filterSessionsBetween(threeWeeksAgo, twoWeeksAgo);

        long playtime1 = weekOne.toActivePlaytime();
        long playtime2 = weekTwo.toActivePlaytime();
        long playtime3 = weekThree.toActivePlaytime();

        double expected = playerContainer.getActivityIndex(time, playtimeThreshold).getValue();
        double got = tablePlayer.getCurrentActivityIndex().orElseThrow(AssertionError::new).getValue();
        assertEquals(expected, got, 0.001,
                () -> "Activity Indexes between queries differed, expected: <" + expected + "> but was: <" + got + ">" +
                        ". Playtime for reference container: <w1:" + playtime1 + ", w2:" + playtime2 + ", w3:" + playtime3 + ">"
        );
    }

    @Test
    default void serverPreferencePieValuesAreCorrect() {
        prepareForSessionSave();
        List<FinishedSession> server1Sessions = RandomData.randomSessions(serverUUID(), worlds, playerUUID, player2UUID);
        server1Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));

        ServerUUID serverTwoUuid = TestConstants.SERVER_TWO_UUID;
        executeTransactions(new StoreServerInformationTransaction(new Server(serverTwoUuid, TestConstants.SERVER_TWO_NAME, "", TestConstants.VERSION)));
        db().executeTransaction(new StoreWorldNameTransaction(serverTwoUuid, worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverTwoUuid, worlds[1]));
        List<FinishedSession> server2Sessions = RandomData.randomSessions(serverTwoUuid, worlds, playerUUID, player2UUID);
        server2Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));

        Map<String, Long> expected = Maps.builder(String.class, Long.class)
                .put(TestConstants.SERVER_NAME, new SessionsMutator(server1Sessions).toPlaytime())
                .put(TestConstants.SERVER_TWO_NAME, new SessionsMutator(server2Sessions).toPlaytime())
                .build();
        Map<String, Long> results = db().query(SessionQueries.playtimePerServer(Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(expected, results);
    }

    @Test
    @DisplayName("Last seen query by server uuid groups last seen by player")
    default void lastSeenByServerIsGroupedByPlayer() {
        prepareForSessionSave();
        List<FinishedSession> player1Sessions = RandomData.randomSessions(serverUUID(), worlds, playerUUID, player2UUID);
        List<FinishedSession> player2Sessions = RandomData.randomSessions(serverUUID(), worlds, player2UUID, playerUUID);
        player1Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));
        player2Sessions.forEach(session -> db().executeTransaction(new StoreSessionTransaction(session)));

        long lastSeenP1 = new SessionsMutator(player1Sessions).toLastSeen();
        long lastSeenP2 = new SessionsMutator(player2Sessions).toLastSeen();

        Map<UUID, Long> expected = Map.of(playerUUID, lastSeenP1, player2UUID, lastSeenP2);
        Map<UUID, Long> result = db().query(SessionQueries.lastSeen(serverUUID()));
        assertEquals(expected, result);
    }
}
