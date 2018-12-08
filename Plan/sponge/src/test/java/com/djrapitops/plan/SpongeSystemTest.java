/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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

    @Test
    public void testEnable() throws EnableException {
        PlanSystem spongeSystem = component.getPlanSystem();
        try {
            spongeSystem.getConfigSystem().getConfig().set(WebserverSettings.PORT, 9005);
            spongeSystem.enable();
        } finally {
            spongeSystem.disable();
        }
    }
}
