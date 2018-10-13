/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.Settings;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.PlanSpongeMocker;

/**
 * Test for Sponge PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SpongeSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static PlanSponge planMock;
    private PlanSystem spongeSystem;
    private PlanSpongeComponent component;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanSpongeMocker mockUtil = PlanSpongeMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withResourceFetchingFromJar()
                .withGame();
        planMock = mockUtil.getPlanMock();
    }

    @Before
    public void setUp() {
        component = DaggerPlanSpongeComponent.builder().plan(planMock).build();
    }

    @After
    public void tearDown() {
        if (spongeSystem != null) {
            spongeSystem.disable();
        }
    }

    @Test
    public void testEnable() throws EnableException {
        spongeSystem = component.system();
        spongeSystem.getConfigSystem().getConfig().set(Settings.WEBSERVER_PORT, 9005);

        spongeSystem.enable();
    }
}
