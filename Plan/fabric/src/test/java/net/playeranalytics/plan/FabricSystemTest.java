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
package net.playeranalytics.plan;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.TestSettings;
import utilities.mocks.FabricMockComponent;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test for Fabric PlanSystem.
 *
 * @author AuroraLS3
 * @author Kopo942
 */
class FabricSystemTest {

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    private PlanSystem system;

    @BeforeEach
    void prepareSystem(@TempDir Path temp) throws Exception {
        system = new FabricMockComponent(temp).getPlanSystem();
        system.getConfigSystem().getConfig()
                .set(WebserverSettings.PORT, TEST_PORT_NUMBER);
    }

    @Test
    void fabricSystemEnables() {
        try {
            system.enable();
            assertTrue(system.isEnabled());
        } finally {
            system.disable();
        }
    }

    @Test
    void fabricSystemHasDefaultConfigValuesAfterEnable() throws IllegalAccessException {
        try {
            system.enable();
            PlanConfig config = system.getConfigSystem().getConfig();

            TestSettings.assertValidDefaultValuesForAllSettings(config, TestSettings.getServerSettings());
        } finally {
            system.disable();
        }
    }
}
