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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestErrorLogger;
import utilities.TestPluginLogger;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test ensures that unsaved sessions are saved on server shutdown.
 *
 * @author AuroraLS3
 */
class ShutdownSaveTest {

    private boolean shutdownStatus;
    private ServerShutdownSave underTest;
    private Database database;
    private SessionCache sessionCache;

    @BeforeEach
    void setupShutdownSaveObject(@TempDir Path temporaryFolder) throws Exception {
        PluginMockComponent pluginMockComponent = new PluginMockComponent(temporaryFolder);
        PlanSystem system = pluginMockComponent.getPlanSystem();

        database = system.getDatabaseSystem().getSqLiteFactory().usingFileCalled("test");
        database.init();

        sessionCache = system.getCacheSystem().getSessionCache();

        storeNecessaryInformation();
        placeSessionToCache();

        DBSystem dbSystemMock = mock(DBSystem.class);
        when(dbSystemMock.getDatabase()).thenReturn(database);

        TestPluginLogger logger = new TestPluginLogger();
        underTest = new ServerShutdownSave(new Locale(), dbSystemMock, logger, new TestErrorLogger()) {
            @Override
            protected boolean checkServerShuttingDownStatus() {
                return shutdownStatus;
            }

            @Override
            public Optional<AFKTracker> getAfkTracker() {
                return Optional.empty();
            }
        };

        shutdownStatus = false;
    }

    @AfterEach
    void tearDownPluginComponent() {
        database.close();
        SessionCache.clear();
    }

    private void storeNecessaryInformation() throws Exception {
        database.executeTransaction(new RemoveEverythingTransaction());

        ServerUUID serverUUID = TestConstants.SERVER_UUID;
        UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
        String worldName = TestConstants.WORLD_ONE_NAME;

        database.executeTransaction(new StoreServerInformationTransaction(new Server(serverUUID, "-", "", TestConstants.VERSION)));
        database.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 0L, TestConstants.PLAYER_ONE_NAME));
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, worldName))
                .get();
    }

    @Test
    void sessionsAreNotSavedOnReload() {
        shutdownStatus = false;
        Optional<Future<?>> future = underTest.performSave();
        assertTrue(future.isEmpty());

        assertTrue(database.query(SessionQueries.fetchAllSessions()).isEmpty());
    }

    @Test
    void sessionsAreNotSavedIfDatabaseIsClosed() {
        shutdownStatus = true;
        database.close();
        Optional<Future<?>> future = underTest.performSave();
        assertTrue(future.isEmpty());
    }

    @Test
    void sessionsAreSavedOnServerShutdown() throws Exception {
        shutdownStatus = true;
        Optional<Future<?>> save = underTest.performSave();
        assertTrue(save.isPresent());
        save.get().get(); // Wait for save to be done, test fails without.

        assertFalse(database.query(SessionQueries.fetchAllSessions()).isEmpty());
    }

    private void placeSessionToCache() {
        ServerUUID serverUUID = TestConstants.SERVER_UUID;
        UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
        String worldName = TestConstants.WORLD_ONE_NAME;

        ActiveSession session = new ActiveSession(playerUUID, serverUUID, 0L, worldName, GMTimes.getGMKeyArray()[0]);

        sessionCache.cacheSession(playerUUID, session);
    }

    @Test
    void endedSessionsHaveSameEndTime() {
        for (int i = 0; i < 100; i++) {
            UUID playerUUID = UUID.randomUUID();
            ActiveSession session = RandomData.randomUnfinishedSession(
                    TestConstants.SERVER_UUID, new String[]{"w1", "w2"}, playerUUID
            );
            sessionCache.cacheSession(playerUUID, session);
        }
        long endTime = System.currentTimeMillis();
        Collection<ActiveSession> activeSessions = SessionCache.getActiveSessions();
        for (FinishedSession session : underTest.finishSessions(activeSessions, endTime)) {
            assertEquals(endTime, session.getEnd(), "One of the sessions had differing end time");
        }
    }

    @Test
    void sessionsAreNotSavedWhenNotShuttingDown() {
        for (int i = 0; i < 100; i++) {
            UUID playerUUID = UUID.randomUUID();
            ActiveSession session = RandomData.randomUnfinishedSession(
                    TestConstants.SERVER_UUID, new String[]{"w1", "w2"}, playerUUID
            );
            sessionCache.cacheSession(playerUUID, session);
        }

        Optional<Future<?>> save = underTest.performSave();
        assertFalse(save.isPresent());

        List<FinishedSession> sessions = database.query(SessionQueries.fetchAllSessions());
        assertEquals(0, sessions.size());
    }
}