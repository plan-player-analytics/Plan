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
package com.djrapitops.plan.system.info.request;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.gathering.domain.Session;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.paths.ExportSettings;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.system.storage.database.transactions.events.SessionEndTransaction;
import com.djrapitops.plan.system.storage.database.transactions.events.WorldNameStoreTransaction;
import com.jayway.awaitility.Awaitility;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test against Server JSON Export errors.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
class AnalysisExportTest {

    private PlanSystem system;

    @BeforeEach
    void setupPlanSystem(@TempDir Path dir) throws Exception {
        PluginMockComponent component = new PluginMockComponent(dir);

        system = component.getPlanSystem();
        PlanConfig config = system.getConfigSystem().getConfig();
        config.set(ExportSettings.JSON_EXPORT_PATH, "Test");
        config.set(ExportSettings.SERVER_JSON, true);
        system.enable();

        Database database = system.getDatabaseSystem().getDatabase();
        storeSomeData(database);
    }

    private void storeSomeData(Database database) {
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, () -> 1000L, "name"));
        Session session = new Session(uuid, TestConstants.SERVER_UUID, 1000L, "world", "SURVIVAL");
        session.endSession(11000L);
        database.executeTransaction(new WorldNameStoreTransaction(TestConstants.SERVER_UUID, "world"));
        database.executeTransaction(new SessionEndTransaction(session));
    }

    @AfterEach
    void tearDownSystem() {
        system.disable();
    }

    @Test
    void serverJSONIsExported() {

        Assume.assumeFalse(true);// TODO Fix test, Changes to server page handling

        File exportFolder = system.getPlanFiles().getFileFromPluginFolder("Test");

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> exportFolder.listFiles() != null);

        File[] folders = exportFolder.listFiles();
        assertNotNull(folders);

        boolean found = false;
        for (File folder : folders) {
            if (folder.isFile()) {
                continue;
            }
            if (folder.getName().equals("server")) {
                for (File file : folder.listFiles()) {
                    if (file.getName().contains("Plan.json")) {
                        found = true;
                    }
                }
            }
        }

        assertTrue(found);
    }

}