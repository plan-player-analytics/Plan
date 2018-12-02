/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.BukkitComponentMocker;
import rules.ComponentMocker;

/**
 * Test for Bukkit PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BukkitSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new BukkitComponentMocker(temporaryFolder);

    @Test
    public void testEnable() throws EnableException {
        PlanSystem bukkitSystem = component.getPlanSystem();
        try {
            PlanConfig config = bukkitSystem.getConfigSystem().getConfig();
            config.set(Settings.WEBSERVER_PORT, 9005);
            bukkitSystem.enable();
        } finally {
            bukkitSystem.disable();
        }
    }
}
