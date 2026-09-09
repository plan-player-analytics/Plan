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
package com.djrapitops.plan;

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.DBPreparer;
import utilities.RandomData;
import utilities.mocks.BungeeMockComponent;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test for Bungee PlanSystem.
 *
 * @author AuroraLS3
 */
class BungeeSystemTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private BungeeMockComponent component;
    private DBPreparer dbPreparer;

    @BeforeEach
    void prepareSystem(@TempDir Path temp) {
        component = new BungeeMockComponent(temp);
        dbPreparer = new DBPreparer(new BungeeSystemTestDependencies(component.getPlanSystem()), TEST_PORT_NUMBER);
    }

    @Test
    void bungeeEnables() {
        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "8.8.8.8");

            DBSystem dbSystem = bungeeSystem.getDatabaseSystem();
            SQLiteDB db = dbSystem.getSqLiteFactory().usingDefaultFile();
            db.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
            dbSystem.setActiveDatabase(db);

            bungeeSystem.enable();
            assertTrue(bungeeSystem.isEnabled());
        } finally {
            bungeeSystem.disable();
        }
    }

    @Test
    void bungeeEnablesWithDefaultIP() {
        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "0.0.0.0");

            DBSystem dbSystem = bungeeSystem.getDatabaseSystem();
            SQLiteDB db = dbSystem.getSqLiteFactory().usingDefaultFile();
            db.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
            dbSystem.setActiveDatabase(db);

            bungeeSystem.enable();
            assertTrue(bungeeSystem.isEnabled());
        } finally {
            bungeeSystem.disable();
        }

    }

    @Test
    void testEnableNoMySQL() {
        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "8.8.8.8");

            assertThrows(EnableException.class, bungeeSystem::enable);
        } finally {
            bungeeSystem.disable();
        }
    }

    @Test
    void testEnableWithMySQL() {
        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            // MySQL settings might not be available.
            assumeTrue(dbPreparer.setUpMySQLSettings(config).isPresent());

            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "8.8.8.8");

            bungeeSystem.enable();
            assertTrue(bungeeSystem.isEnabled());
        } finally {
            bungeeSystem.disable();
        }
    }
}
