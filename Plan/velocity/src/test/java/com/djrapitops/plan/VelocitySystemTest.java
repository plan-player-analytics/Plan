/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.ComponentMocker;
import rules.VelocityComponentMocker;

/**
 * Test for Velocity PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class VelocitySystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new VelocityComponentMocker(temporaryFolder);

    @Test
    public void velocityEnables() throws Exception {
        PlanSystem velocitySystem = component.getPlanSystem();
        try {
            PlanConfig config = velocitySystem.getConfigSystem().getConfig();
            config.set(Settings.WEBSERVER_PORT, 9005);
            config.set(Settings.BUNGEE_IP, "8.8.8.8");

            DBSystem dbSystem = velocitySystem.getDatabaseSystem();
            dbSystem.setActiveDatabase(dbSystem.getSqLiteFactory().usingDefaultFile());

            velocitySystem.enable();
        } finally {
            velocitySystem.disable();
        }
    }
}
