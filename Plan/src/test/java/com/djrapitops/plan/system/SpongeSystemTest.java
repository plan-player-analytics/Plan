/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.settings.Settings;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;
import utilities.mocks.SpongeMockUtil;

/**
 * Test for BukkitSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SpongeSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static PlanSponge planMock;
    private SpongeSystem spongeSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        SpongeMockUtil mockUtil = SpongeMockUtil.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withResourceFetchingFromJar();
        planMock = mockUtil.getPlanMock();
    }

    @Before
    public void setUp() {
        Teardown.resetSettingsTempValues();
    }

    @After
    public void tearDown() {
        if (spongeSystem != null) {
            spongeSystem.disable();
        }
        Teardown.resetSettingsTempValues();
    }

    @Test
    @Ignore("Sponge mock required")
    public void testEnable() throws EnableException {
        Settings.WEBSERVER_PORT.setTemporaryValue(9005);

        spongeSystem = null; //TODO
        spongeSystem.enable();
    }
}
