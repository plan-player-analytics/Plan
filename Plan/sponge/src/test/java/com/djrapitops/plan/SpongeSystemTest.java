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
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.ComponentMocker;
import rules.SpongeComponentMocker;
import utilities.RandomData;

/**
 * Test for Sponge PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SpongeSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new SpongeComponentMocker(temporaryFolder);

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @Test
    public void testEnable() throws EnableException {
        PlanSystem spongeSystem = component.getPlanSystem();
        try {
            spongeSystem.getConfigSystem().getConfig().set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            spongeSystem.enable();
        } finally {
            spongeSystem.disable();
        }
    }
}
