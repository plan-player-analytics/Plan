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

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.mocks.VelocityMockComponent;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test for Velocity PlanSystem.
 *
 * @author AuroraLS3
 */
class VelocitySystemTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @Test
    void velocityEnables(@TempDir Path temp) {
        PlanSystem velocitySystem = new VelocityMockComponent(temp).getPlanSystem();
        try {
            PlanConfig config = velocitySystem.getConfigSystem().getConfig();
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "8.8.8.8");

            DBSystem dbSystem = velocitySystem.getDatabaseSystem();
            SQLiteDB db = dbSystem.getSqLiteFactory().usingDefaultFile();
            db.setTransactionExecutorServiceProvider(MoreExecutors::newDirectExecutorService);
            dbSystem.setActiveDatabase(db);

            velocitySystem.enable();
            assertTrue(velocitySystem.isEnabled());
        } finally {
            velocitySystem.disable();
        }
    }
}
