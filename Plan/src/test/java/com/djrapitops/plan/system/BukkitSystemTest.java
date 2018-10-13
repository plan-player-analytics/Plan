/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.EnableException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.PlanBukkitMocker;

/**
 * Test for Bukkit PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BukkitSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static Plan planMock;
    private PlanSystem bukkitSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanBukkitMocker mockUtil = PlanBukkitMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        planMock = mockUtil.getPlanMock();
    }

    @After
    public void tearDown() {
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
    }

    @Test
    @Ignore
    public void testEnable() throws EnableException {
//        Settings.WEBSERVER_PORT.setTemporaryValue(9005);

        bukkitSystem = null; //TODO
        bukkitSystem.enable();
    }
}
