/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
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
    private PlanSystem bukkitSystem;
    private static PlanBukkitComponent COMPONENT;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanBukkitMocker mockUtil = PlanBukkitMocker.setUp()
                .withDataFolder(temporaryFolder.newFolder())
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        COMPONENT = DaggerPlanBukkitComponent.builder().plan(mockUtil.getPlanMock()).build();
    }

    @Before
    public void setUp() {
        bukkitSystem = COMPONENT.system();
    }

    @After
    public void tearDown() {
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
    }

    @Test
    public void testEnable() throws EnableException {
        PlanConfig config = bukkitSystem.getConfigSystem().getConfig();
        config.set(Settings.WEBSERVER_PORT, 9005);
        bukkitSystem.enable();
    }
}
