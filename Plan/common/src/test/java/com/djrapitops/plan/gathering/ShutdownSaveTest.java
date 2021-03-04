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
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import extension.PrintExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.dagger.DaggerPlanPluginComponent;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PlanPluginMocker;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test ensures that unsaved sessions are saved on server shutdown.
 *
 * @author AuroraLS3
 */
@ExtendWith(PrintExtension.class)
class ShutdownSaveTest {

    private boolean shutdownStatus;
    private ServerShutdownSave underTest;
    private Database database;
    private SessionCache sessionCache;

    @BeforeEach
    void setupShutdownSaveObject(@TempDir Path temporaryFolder) throws Exception {
        PlanPluginComponent pluginComponent = DaggerPlanPluginComponent.builder()
                .bindTemporaryDirectory(temporaryFolder)
                .plan(
                        PlanPluginMocker.setUp()
                                .withDataFolder(temporaryFolder.resolve("ShutdownSaveTest").toFile())
                                .withLogging()
                                .getPlanMock()
                ).build();
        PlanSystem system = pluginComponent.system();

        database = system.getDatabaseSystem().getSqLiteFactory().usingFileCalled("test");
        database.init();

        sessionCache = system.getCacheSystem().getSessionCache();

        storeNecessaryInformation();
        placeSessionToCache();

        DBSystem dbSystemMock = mock(DBSystem.class);
        when(dbSystemMock.getDatabase()).thenReturn(database);

        TestPluginLogger logger = new TestPluginLogger();
        underTest = new ServerShutdownSave(new Locale(), dbSystemMock, logger, system.getErrorLogger()) {
            @Override
            protected boolean checkServerShuttingDownStatus() {
                return shutdownStatus;
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

        UUID serverUUID = TestConstants.SERVER_UUID;
        UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
        String worldName = TestConstants.WORLD_ONE_NAME;

        database.executeTransaction(new StoreServerInformationTransaction(new Server(serverUUID, "-", "")));
        database.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> 0L, TestConstants.PLAYER_ONE_NAME));
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, worldName))
                .get();
    }

    @Test
    void sessionsAreNotSavedOnReload() {
        shutdownStatus = false;
        underTest.performSave();

        database.init();
        assertTrue(database.query(SessionQueries.fetchAllSessions()).isEmpty());
        database.close();
    }

    @Test
    void sessionsAreSavedOnServerShutdown() {
        shutdownStatus = true;
        underTest.performSave();

        database.init();
        assertFalse(database.query(SessionQueries.fetchAllSessions()).isEmpty());
        database.close();
    }

    @Test
    void sessionsAreSavedOnJVMShutdown() {
        ShutdownHook shutdownHook = new ShutdownHook(underTest);
        shutdownHook.run();

        database.init();
        assertFalse(database.query(SessionQueries.fetchAllSessions()).isEmpty());
        database.close();
    }

    private void placeSessionToCache() {
        UUID serverUUID = TestConstants.SERVER_UUID;
        UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
        String worldName = TestConstants.WORLD_ONE_NAME;

        Session session = new Session(playerUUID, serverUUID, 0L, worldName, GMTimes.getGMKeyArray()[0]);

        sessionCache.cacheSession(playerUUID, session);
    }

    @Test
    public void endedSessionsHaveSameEndTime() {
        for (int i = 0; i < 100; i++) {
            UUID playerUUID = UUID.randomUUID();
            Session session = RandomData.randomUnfinishedSession(
                    TestConstants.SERVER_UUID, new String[]{"w1", "w2"}, playerUUID
            );
            sessionCache.cacheSession(playerUUID, session);
        }
        long endTime = System.currentTimeMillis();
        Map<UUID, Session> activeSessions = SessionCache.getActiveSessions();
        underTest.prepareSessionsForStorage(activeSessions, endTime);
        for (Session session : activeSessions.values()) {
            assertEquals(endTime, session.getUnsafe(SessionKeys.END), () -> "One of the sessions had differing end time");
        }
    }
}