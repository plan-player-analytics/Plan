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

import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.delivery.domain.ServerName;
import com.djrapitops.plan.delivery.domain.TablePlayer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.containers.PlayerContainerQuery;
import com.djrapitops.plan.storage.database.queries.objects.BaseUserQueries;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;
import com.djrapitops.plan.storage.database.queries.objects.playertable.ServerTablePlayersQuery;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.utilities.java.Maps;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import org.junit.jupiter.api.*;
import utilities.RandomData;
import utilities.TestConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
        long sessionEnd = session.end();

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
    default void sessionFetchWithoutExtraData() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        session.extraData().remove(PlayerKills.class);
        session.extraData().remove(WorldTimes.class);
        session.extraData().remove(PlayerDeaths.class);
        session.extraData().remove(JoinAddress.class);
        session.extraData().remove(PlayerName.class);
        session.extraData().remove(ServerName.class);
        var expected = List.of(session);
        var sessions = db().query(SessionQueries.fetchServerSessionsWithoutKillOrWorldData(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(expected, sessions);
    }

    @Test
    default void sessionCountOfServer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = 1L;
        var count = db().query(SessionQueries.sessionCount(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(expected, count);
    }

    @Test
    default void sessionCountOfNetwork() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = 1L;
        var count = db().query(SessionQueries.sessionCount(0L, Long.MAX_VALUE));
        assertEquals(expected, count);
    }

    @Test
    default void playtimeOfServer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.getLength();
        var result = db().query(SessionQueries.playtime(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(expected, result);
    }

    @Test
    default void playtimeOfServers() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.getLength();
        var result = db().query(SessionQueries.playtime(0L, Long.MAX_VALUE, List.of(serverUUID())));
        assertEquals(expected, result);
    }

    @Test
    default void playtimeOfPlayer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.getLength();
        var result = db().query(SessionQueries.playtime(0L, Long.MAX_VALUE, playerUUID, List.of()));
        assertEquals(expected, result);
        result = db().query(SessionQueries.playtime(0L, Long.MAX_VALUE, playerUUID, List.of(serverUUID())));
        assertEquals(expected, result);
    }

    @Test
    default void playtimePerDay() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var result = db().query(SessionQueries.playtimePerDay(0L, Long.MAX_VALUE, 0L, serverUUID()));
        assertFalse(result.isEmpty());
        result = db().query(SessionQueries.playtimePerDay(0L, Long.MAX_VALUE, 0L));
        assertFalse(result.isEmpty());
    }

    @Test
    default void averagePlaytimePerDay() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var result = db().query(SessionQueries.averagePlaytimePerDay(0L, Long.MAX_VALUE, 0L, serverUUID()));
        assertNotEquals(0L, result);
    }

    @Test
    default void averagePlaytimePerPlayer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var result = db().query(SessionQueries.averagePlaytimePerPlayer(0L, Long.MAX_VALUE, serverUUID()));
        assertNotEquals(0L, result);
        result = db().query(SessionQueries.averagePlaytimePerPlayer(0L, Long.MAX_VALUE));
        assertNotEquals(0L, result);
    }

    @Test
    default void averageAfkPerPlayer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var result = db().query(SessionQueries.averageAfkPerPlayer(0L, Long.MAX_VALUE, serverUUID()));
        assertNotEquals(0L, result);
        result = db().query(SessionQueries.averageAfkPerPlayer(0L, Long.MAX_VALUE));
        assertNotEquals(0L, result);
    }

    @Test
    default void afkTimeOfPlayer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.afkTime();
        var result = db().query(SessionQueries.afkTime(0L, Long.MAX_VALUE, playerUUID, List.of(serverUUID())));
        assertEquals(expected, result);
    }

    @Test
    default void afkTimeOfServer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.afkTime();
        var result = db().query(SessionQueries.afkTime(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(expected, result);
    }

    @Test
    default void afkTimePercentageOfServer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.afkTime() * 1.0 / session.getLength();
        var result = db().query(SessionQueries.afkTimePercentage(0L, Long.MAX_VALUE, List.of(serverUUID())));
        assertEquals(
                BigDecimal.valueOf(expected).setScale(4, RoundingMode.HALF_UP).doubleValue(),
                BigDecimal.valueOf(result).setScale(4, RoundingMode.HALF_UP).doubleValue(),
                0.0001
        );
    }

    @Test
    default void activePlaytimeOfServer() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.getLength() - session.afkTime();
        var result = db().query(SessionQueries.activePlaytime(0L, Long.MAX_VALUE, serverUUID()));
        assertEquals(expected, result);
    }

    @Test
    default void activePlaytimeOfNetwork() {
        prepareForSessionSave();
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session));

        var expected = session.getLength() - session.afkTime();
        var result = db().query(SessionQueries.activePlaytime(0L, Long.MAX_VALUE));
        assertEquals(expected, result);
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
        session.extraData().put(WorldTimes.class, worldTimes);
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
        session.extraData().put(WorldTimes.class, worldTimes);
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
        session.extraData().put(WorldTimes.class, worldTimes);
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
        WorldTimes worldTimesOfUserOnServer = db().query(WorldTimesQueries.fetchPlayerTotalWorldTimes(0L, Long.MAX_VALUE, playerUUID, List.of(serverUUID())));
        assertEquals(expected, worldTimesOfUser);
        assertEquals(expected, worldTimesOfUserOnServer);
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
    default void networkWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        FinishedSession session = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID)).get(serverUUID()).get(0);
        WorldTimes expected = session.getExtraData(WorldTimes.class).orElseThrow(AssertionError::new);
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(0L, Long.MAX_VALUE, List.of()));
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

    @TestFactory
    @DisplayName("Sessions within timeframe are fetched correctly")
    default Stream<DynamicTest> sessionsWithinTimeframe() {
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, RandomData::randomTime,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        FinishedSession session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        db().executeTransaction(new StoreSessionTransaction(session)).join();

        List<FinishedSession> expected = List.of(session);
        List<FinishedSession> none = List.of();
        long beforeSession = session.start() - TimeUnit.MINUTES.toMillis(1);
        long afterSession = session.end() + TimeUnit.MINUTES.toMillis(1);
        return Stream.of(
                DynamicTest.dynamicTest("Player sessions", () -> assertEquals(expected, db().query(SessionQueries.sessionsOfPlayer(playerUUID, beforeSession, afterSession, List.of())))),
                DynamicTest.dynamicTest("Server sessions", () -> assertEquals(expected, db().query(SessionQueries.sessionsOfServers(beforeSession, afterSession, List.of(serverUUID()))))),
                DynamicTest.dynamicTest("Network sessions", () -> assertEquals(expected, db().query(SessionQueries.sessionsOfServers(beforeSession, afterSession, List.of())))),
                DynamicTest.dynamicTest("Player sessions - none (before)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfPlayer(playerUUID, beforeSession - TimeUnit.MINUTES.toMillis(1), beforeSession, List.of())))),
                DynamicTest.dynamicTest("Player sessions - none (after)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfPlayer(playerUUID, afterSession, afterSession + TimeUnit.MINUTES.toMillis(1), List.of())))),
                DynamicTest.dynamicTest("Player sessions - none (wrong order)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfPlayer(playerUUID, afterSession, beforeSession, List.of())))),
                DynamicTest.dynamicTest("Server sessions - none (before)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfServers(beforeSession - TimeUnit.MINUTES.toMillis(1), beforeSession, List.of(serverUUID()))))),
                DynamicTest.dynamicTest("Server sessions - none (after)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfServers(afterSession, afterSession + TimeUnit.MINUTES.toMillis(1), List.of(serverUUID()))))),
                DynamicTest.dynamicTest("Server sessions - none (wrong order)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfServers(afterSession, beforeSession, List.of(serverUUID()))))),
                DynamicTest.dynamicTest("Network sessions - none (before)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfServers(beforeSession - TimeUnit.MINUTES.toMillis(1), beforeSession, List.of())))),
                DynamicTest.dynamicTest("Network sessions - none (after)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfServers(afterSession, afterSession + TimeUnit.MINUTES.toMillis(1), List.of())))),
                DynamicTest.dynamicTest("Network sessions - none (wrong order)", () -> assertEquals(none, db().query(SessionQueries.sessionsOfServers(afterSession, beforeSession, List.of()))))
        );
    }

    private FinishedSession sessionForLightweightTest(ServerUUID server, String world, long start, long end, long afk, String joinAddress, boolean withWorldTimes) {
        DataMap extraData = new DataMap();
        WorldTimes worldTimes = new WorldTimes();
        if (withWorldTimes) {
            Map<String, Long> gmTimes = new HashMap<>();
            gmTimes.put("SURVIVAL", end - start);
            worldTimes.setGMTimesForWorld(world, new GMTimes(gmTimes));
        }
        extraData.put(WorldTimes.class, worldTimes);
        extraData.put(PlayerKills.class, new PlayerKills());
        extraData.put(MobKillCounter.class, new MobKillCounter());
        extraData.put(DeathCounter.class, new DeathCounter());
        extraData.put(JoinAddress.class, new JoinAddress(joinAddress));
        return new FinishedSession(playerUUID, server, start, end, afk, extraData);
    }

    private long storeSessionsForLightweightTest() {
        long now = System.currentTimeMillis();
        long day = TimeUnit.DAYS.toMillis(1L);
        long hour = TimeUnit.HOURS.toMillis(1L);
        ServerUUID serverTwo = TestConstants.SERVER_TWO_UUID;
        db().executeTransaction(new StoreServerInformationTransaction(new Server(serverTwo, TestConstants.SERVER_TWO_NAME, "", TestConstants.VERSION)));
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreWorldNameTransaction(serverTwo, worlds[1]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> now - 60 * day,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreServerPlayerTransaction(player2UUID, () -> now - 60 * day,
                TestConstants.PLAYER_TWO_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        // Every session has a different length and afk time so that sums of different subsets can't collide.
        List<FinishedSession> sessions = List.of(
                // Outside the month window
                sessionForLightweightTest(serverUUID(), worlds[0], now - 45 * day, now - 45 * day + 7 * hour, hour, "old.example.com", true),
                // Starts before the month window, ends inside it: counted in full
                sessionForLightweightTest(serverUUID(), worlds[0], now - 30 * day - 3 * hour, now - 30 * day + 2 * hour, 11 * 60_000L, "boundary.example.com", true),
                // Inside month, outside week
                sessionForLightweightTest(serverTwo, worlds[1], now - 10 * day, now - 10 * day + 5 * hour, 3 * 60_000L, "two.example.com", true),
                // Inside week, outside day
                sessionForLightweightTest(serverUUID(), worlds[0], now - 3 * day, now - 3 * day + 2 * hour, 17 * 60_000L, "week.example.com", true),
                // Inside day, on server two: the latest session that has world times
                sessionForLightweightTest(serverTwo, worlds[1], now - 5 * hour, now - 4 * hour, 0L, "latest.example.com", true),
                // Latest start, but without world times rows: not visible to the full session query either
                sessionForLightweightTest(serverUUID(), worlds[0], now - 2 * hour, now - hour - 13 * 60_000L, 0L, "noworldtimes.example.com", false)
        );
        for (FinishedSession session : sessions) {
            db().executeTransaction(new StoreSessionTransaction(session));
        }
        // Another player's sessions must not be included
        db().executeTransaction(new StoreSessionTransaction(new FinishedSession(player2UUID, serverUUID(), now - 2 * hour, now - hour, 0L,
                sessionForLightweightTest(serverUUID(), worlds[0], now - 2 * hour, now - hour, 0L, "other.example.com", true).extraData())));
        forcePersistenceCheck();
        return now;
    }

    @TestFactory
    default Collection<DynamicTest> lightweightActivePlaytimeMatchesFullSessionLoad() {
        long now = storeSessionsForLightweightTest();
        long hour = TimeUnit.HOURS.toMillis(1L);
        PlayerContainer container = db().query(new PlayerContainerQuery(playerUUID));

        Map<String, Long> windows = new LinkedHashMap<>();
        windows.put("day", now - TimeUnit.DAYS.toMillis(1L));
        windows.put("week", now - TimeUnit.DAYS.toMillis(7L));
        windows.put("month", now - TimeUnit.DAYS.toMillis(30L));
        windows.put("all", 0L);

        Map<String, Long> expectedByHand = new HashMap<>();
        expectedByHand.put("day", hour);
        expectedByHand.put("week", hour + 2 * hour - 17 * 60_000L);
        expectedByHand.put("month", expectedByHand.get("week") + 5 * hour - 3 * 60_000L + 5 * hour - 11 * 60_000L);
        expectedByHand.put("all", expectedByHand.get("month") + 7 * hour - hour);

        List<DynamicTest> tests = new ArrayList<>();
        for (Map.Entry<String, Long> window : windows.entrySet()) {
            long after = window.getValue();
            tests.add(DynamicTest.dynamicTest("Active playtime " + window.getKey(), () -> {
                long fullLoad = SessionsMutator.forContainer(container)
                        .filterSessionsBetween(after, now)
                        .toActivePlaytime();
                long lightweight = new SessionsMutator(db().query(SessionQueries.fetchSessionsOfPlayerWithoutExtraData(playerUUID, after, now)))
                        .filterSessionsBetween(after, now)
                        .toActivePlaytime();
                assertEquals(expectedByHand.get(window.getKey()), fullLoad);
                assertEquals(fullLoad, lightweight);
            }));
        }
        return tests;
    }

    @Test
    default void lightweightLatestSessionMatchesFullSessionLoad() {
        storeSessionsForLightweightTest();
        PlayerContainer container = db().query(new PlayerContainerQuery(playerUUID));

        Optional<String> fullLoad = SessionsMutator.forContainer(container).latestSession()
                .flatMap(session -> session.getExtraData(JoinAddress.class))
                .map(JoinAddress::getAddress);
        Optional<FinishedSession> latest = db().query(SessionQueries.fetchLatestSessionOfPlayerWithoutExtraData(playerUUID));
        Optional<String> lightweight = latest
                .flatMap(session -> session.getExtraData(JoinAddress.class))
                .map(JoinAddress::getAddress);

        assertEquals(Optional.of("latest.example.com"), fullLoad);
        assertEquals(fullLoad, lightweight);
        assertEquals(TestConstants.SERVER_TWO_UUID, latest.map(FinishedSession::serverUUID).orElse(null));
    }

    @Test
    default void lightweightLatestSessionIsEmptyWithoutSessions() {
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, System::currentTimeMillis,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        forcePersistenceCheck();
        assertEquals(Optional.empty(), db().query(SessionQueries.fetchLatestSessionOfPlayerWithoutExtraData(playerUUID)));
        assertEquals(List.of(), db().query(SessionQueries.fetchSessionsOfPlayerWithoutExtraData(playerUUID, 0L, System.currentTimeMillis())));
    }

    @Test
    default void lightweightSessionsIncludeSessionsWithoutJoinAddress() {
        long now = System.currentTimeMillis();
        long hour = TimeUnit.HOURS.toMillis(1L);
        db().executeTransaction(new StoreWorldNameTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new StoreServerPlayerTransaction(playerUUID, () -> now - 2 * hour,
                TestConstants.PLAYER_ONE_NAME, serverUUID(), TestConstants.GET_PLAYER_HOSTNAME));
        db().executeTransaction(new StoreSessionTransaction(
                sessionForLightweightTest(serverUUID(), worlds[0], now - 2 * hour, now - hour, 0L, "missing.example.com", true)));
        forcePersistenceCheck();
        // Some sessions point to a join address row that doesn't exist. Done after the persistence check,
        // because db init runs AfterBadJoinAddressDataCorrectionPatch, which would point it to "unknown" again.
        execute(new ExecStatement("UPDATE " + SessionsTable.TABLE_NAME + " SET " + SessionsTable.JOIN_ADDRESS_ID + "=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, -1);
            }
        });

        List<FinishedSession> sessions = db().query(SessionQueries.fetchSessionsOfPlayerWithoutExtraData(playerUUID, 0L, now));
        assertEquals(1, sessions.size());
        assertEquals(hour, new SessionsMutator(sessions).toActivePlaytime());
        assertEquals(Optional.empty(), sessions.get(0).getExtraData(JoinAddress.class));

        Optional<FinishedSession> latest = db().query(SessionQueries.fetchLatestSessionOfPlayerWithoutExtraData(playerUUID));
        assertTrue(latest.isPresent());
        assertEquals(Optional.empty(), latest.get().getExtraData(JoinAddress.class));
    }}
