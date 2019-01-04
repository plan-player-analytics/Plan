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

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.ConfigSettingKeyTest;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.ComponentMocker;
import rules.SpongeComponentMocker;
import utilities.RandomData;

import java.util.Collection;

import static org.junit.Assert.assertTrue;

/**
 * Test for Sponge PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SpongeSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ComponentMocker component = new SpongeComponentMocker(temporaryFolder);

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private PlanSystem system;

    @Before
    public void prepareSpongeSystem() {
        system = component.getPlanSystem();
        system.getConfigSystem().getConfig()
                .set(WebserverSettings.PORT, TEST_PORT_NUMBER);
    }

    @Test
    public void spongeSystemEnables() throws EnableException {
        try {
            system.enable();
            assertTrue(system.isEnabled());
        } finally {
            system.disable();
        }
    }

    @Test
    public void spongeSystemHasDefaultConfigValuesAfterEnable() throws EnableException, IllegalAccessException {
        try {
            system.enable();
            PlanConfig config = system.getConfigSystem().getConfig();

            Collection<Setting> serverSettings = ConfigSettingKeyTest.getServerSettings();
            ConfigSettingKeyTest.assertValidDefaultValuesForAllSettings(config, serverSettings);
        } finally {
            system.disable();
        }
    }
}
