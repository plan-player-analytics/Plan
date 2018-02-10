/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.settings.Settings;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;
import utilities.mocks.BukkitMockUtil;

/**
 * Test for BukkitSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BukkitSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static Plan planMock;
    private BukkitSystem bukkitSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BukkitMockUtil mockUtil = BukkitMockUtil.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        planMock = mockUtil.getPlanMock();
    }

    @Before
    public void setUp() {
        Teardown.resetSettingsTempValues();
    }
    
    @After
    public void tearDown() {
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
        Teardown.resetSettingsTempValues();
    }

    @Test
    public void testEnable() throws EnableException {
        Settings.WEBSERVER_PORT.setTemporaryValue(9000);

        bukkitSystem = new BukkitSystem(planMock);
        bukkitSystem.enable();
    }
}