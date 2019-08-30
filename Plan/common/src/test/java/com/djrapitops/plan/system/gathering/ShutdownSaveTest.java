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
package com.djrapitops.plan.system.gathering;

import com.djrapitops.plan.system.gathering.cache.SessionCache;
import com.djrapitops.plan.system.gathering.domain.GMTimes;
import com.djrapitops.plan.system.gathering.domain.Session;
import com.djrapitops.plan.system.identification.Server;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.system.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.system.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.system.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.system.storage.database.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import extension.PrintExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import utilities.TestConstants;
import utilities.dagger.DaggerPlanPluginComponent;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PlanPluginMocker;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test ensures that unsaved sessions are saved on server shutdown.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
@ExtendWith(PrintExtension.class)
class ShutdownSaveTest {

    private boolean shutdownStatus;
    private ServerShutdownSave underTest;
    private Database database;
    private SessionCache sessionCache;

    @BeforeEach
    void setupShutdownSaveObject(@TempDir Path temporaryFolder) throws Exception {
        PlanPluginComponent pluginComponent = DaggerPlanPluginComponent.builder().plan(
                PlanPluginMocker.setUp()
                        .withDataFolder(temporaryFolder.resolve("ShutdownSaveTest").toFile())
                        .withLogging()
                        .getPlanMock()
        ).build();

        database = pluginComponent.system().getDatabaseSystem().getSqLiteFactory().usingFileCalled("test");
        database.init();

        sessionCache = pluginComponent.system().getCacheSystem().getSessionCache();

        storeNecessaryInformation();
        placeSessionToCache();

        DBSystem dbSystemMock = mock(DBSystem.class);
        when(dbSystemMock.getDatabase()).thenReturn(database);

        TestPluginLogger logger = new TestPluginLogger();
        underTest = new ServerShutdownSave(new Locale(), dbSystemMock, logger, new ConsoleErrorLogger(logger)) {
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

        database.executeTransaction(new StoreServerInformationTransaction(new Server(-1, serverUUID, "-", "", 0)));
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
}