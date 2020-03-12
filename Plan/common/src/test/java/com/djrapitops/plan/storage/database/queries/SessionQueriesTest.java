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

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerServerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import org.junit.jupiter.api.Test;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public interface SessionQueriesTest extends DatabaseTestPreparer {

    @Test
    default void sessionPlaytimeIsCalculatedCorrectlyAfterStorage() {
        prepareForSessionSave();

        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        long expectedLength = session.getLength();
        long sessionEnd = session.getValue(SessionKeys.END).orElseThrow(AssertionError::new);

        execute(DataStoreQueries.storeSession(session));

        forcePersistenceCheck();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        assertTrue(sessions.containsKey(serverUUID()));

        SessionsMutator sessionsMutator = new SessionsMutator(sessions.get(serverUUID()));
        assertEquals(expectedLength, sessionsMutator.toPlaytime());
        assertEquals(1, sessionsMutator.count());

        SessionsMutator afterTimeSessionsMutator = sessionsMutator.filterSessionsBetween(sessionEnd + 1L, System.currentTimeMillis());
        assertEquals(0L, afterTimeSessionsMutator.toPlaytime());
        assertEquals(0, afterTimeSessionsMutator.count());
    }

    default void prepareForSessionSave() {
        db().executeTransaction(new WorldNameStoreTransaction(serverUUID(), worlds[0]));
        db().executeTransaction(new WorldNameStoreTransaction(serverUUID(), worlds[1]));
        db().executeTransaction(new PlayerServerRegisterTransaction(playerUUID, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME, serverUUID()));
        db().executeTransaction(new PlayerServerRegisterTransaction(player2UUID, RandomData::randomTime, TestConstants.PLAYER_TWO_NAME, serverUUID()));
    }

    @Test
    default void sessionsAreStoredWithAllData() {
        prepareForSessionSave();
        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        execute(DataStoreQueries.storeSession(session));

        forcePersistenceCheck();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> savedSessions = sessions.get(serverUUID());

        assertNotNull(savedSessions);
        assertEquals(1, savedSessions.size());

        assertEquals(session, savedSessions.get(0));
    }

    @Test
    default void mostRecentSessionsCanBeQueried() {
        prepareForSessionSave();
        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        execute(DataStoreQueries.storeSession(session));

        List<Session> expected = Collections.singletonList(session);
        List<Session> result = db().query(SessionQueries.fetchLatestSessionsOfServer(serverUUID(), 1));
        assertEquals(expected, result);
    }

    @Test
    default void worldTimesAreSavedWithAllSessionSave() {
        prepareForSessionSave();

        WorldTimes worldTimes = RandomData.randomWorldTimes(worlds);
        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID);
        session.setWorldTimes(worldTimes);
        List<Session> sessions = Collections.singletonList(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        Map<UUID, WorldTimes> saved = db().query(WorldTimesQueries.fetchPlayerWorldTimesOnServers(playerUUID));
        WorldTimes savedWorldTimes = saved.get(serverUUID());
        assertEquals(worldTimes, savedWorldTimes);
    }

    @Test
    default void worldTimesAreSavedWithSession() {
        prepareForSessionSave();

        WorldTimes worldTimes = RandomData.randomWorldTimes(worlds);
        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID);
        session.setWorldTimes(worldTimes);
        List<Session> sessions = new ArrayList<>();
        sessions.add(session);
        db().executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(LargeStoreQueries.storeAllSessionsWithKillAndWorldData(sessions));
            }
        });

        List<Session> allSessions = db().query(SessionQueries.fetchAllSessions());

        assertEquals(worldTimes, allSessions.get(0).getUnsafe(SessionKeys.WORLD_TIMES));
    }

    @Test
    default void playersWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        Session session = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID)).get(serverUUID()).get(0);
        WorldTimes expected = session.getValue(SessionKeys.WORLD_TIMES).orElseThrow(AssertionError::new);
        WorldTimes worldTimesOfUser = db().query(WorldTimesQueries.fetchPlayerTotalWorldTimes(playerUUID));
        assertEquals(expected, worldTimesOfUser);
    }

    @Test
    default void serverWorldTimesMatchTotal() {
        worldTimesAreSavedWithSession();
        Session session = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID)).get(serverUUID()).get(0);
        WorldTimes expected = session.getValue(SessionKeys.WORLD_TIMES).orElseThrow(AssertionError::new);
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));
        assertEquals(expected, worldTimesOfServer);
    }

    @Test
    default void emptyServerWorldTimesIsEmpty() {
        WorldTimes worldTimesOfServer = db().query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID()));
        assertEquals(new WorldTimes(), worldTimesOfServer);
    }

    @Test
    default void serverSessionsAreFetchedByPlayerUUID() {
        prepareForSessionSave();
        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        execute(DataStoreQueries.storeSession(session));

        forcePersistenceCheck();

        Map<UUID, List<Session>> expected = Collections.singletonMap(playerUUID, Collections.singletonList(session));
        Map<UUID, List<Session>> fetched = db().query(SessionQueries.fetchSessionsOfServer(serverUUID()));

        assertEquals(expected, fetched);
    }

    @Test
    default void playerSessionsAreFetchedByServerUUID() {
        prepareForSessionSave();

        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        execute(DataStoreQueries.storeSession(session));

        forcePersistenceCheck();

        Map<UUID, List<Session>> expected = Collections.singletonMap(serverUUID(), Collections.singletonList(session));
        Map<UUID, List<Session>> fetched = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        assertEquals(expected, fetched);
    }

    @Test
    default void testKillTableGetKillsOfServer() {
        prepareForSessionSave();

        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        List<PlayerKill> expected = session.getPlayerKills();
        execute(DataStoreQueries.storeSession(session));

        forcePersistenceCheck();

        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> savedSessions = sessions.get(serverUUID());
        assertNotNull(savedSessions);
        assertFalse(savedSessions.isEmpty());

        List<PlayerKill> got = savedSessions.get(0).getPlayerKills();
        assertEquals(expected, got);
    }

    @Test
    default void sessionWorldTimesAreFetchedCorrectly() {
        prepareForSessionSave();

        Session session = RandomData.randomSession(serverUUID(), worlds, playerUUID, player2UUID);
        WorldTimes expected = session.getValue(SessionKeys.WORLD_TIMES).orElseThrow(AssertionError::new);
        execute(DataStoreQueries.storeSession(session));

        // Fetch the session
        Map<UUID, List<Session>> sessions = db().query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        List<Session> serverSessions = sessions.get(serverUUID());
        assertNotNull(serverSessions);
        assertFalse(serverSessions.isEmpty());

        WorldTimes got = serverSessions.get(0).getValue(SessionKeys.WORLD_TIMES).orElseThrow(AssertionError::new);
        assertEquals(expected, got);
    }
}
