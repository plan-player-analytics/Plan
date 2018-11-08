/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.PlanVelocityMocker;

/**
 * Test for Velocity PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class VelocitySystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static PlanVelocity PLUGIN_MOCK;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private PlanVelocityComponent component;
    private PlanSystem velocitySystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanVelocityMocker mocker = PlanVelocityMocker.setUp()
                .withDataFolder(temporaryFolder.newFolder())
                .withResourceFetchingFromJar()
                .withProxy();
        PLUGIN_MOCK = mocker.getPlanMock();
    }

    @Before
    public void setUp() {
        component = DaggerPlanVelocityComponent.builder().plan(PLUGIN_MOCK).build();
    }

    @After
    public void tearDown() {
        if (velocitySystem != null) {
            velocitySystem.disable();
        }
    }

    @Test
    public void velocityEnables() throws Exception {
        velocitySystem = component.system();

        PlanConfig config = velocitySystem.getConfigSystem().getConfig();
        config.set(Settings.WEBSERVER_PORT, 9005);
        config.set(Settings.BUNGEE_IP, "8.8.8.8");

        DBSystem dbSystem = velocitySystem.getDatabaseSystem();
        dbSystem.setActiveDatabase(dbSystem.getSqLiteFactory().usingDefaultFile());

        velocitySystem.enable();
    }
}
