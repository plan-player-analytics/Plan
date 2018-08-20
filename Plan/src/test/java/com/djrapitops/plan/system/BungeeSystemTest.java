/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.database.ServerDBSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;
import utilities.mocks.BungeeMockUtil;

/**
 * Test for BukkitSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BungeeSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static PlanBungee planMock;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private BungeeSystem bungeeSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BungeeMockUtil mockUtil = BungeeMockUtil.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withProxy();
        planMock = mockUtil.getPlanMock();
    }


    @Before
    public void setUp() {
        Teardown.resetSettingsTempValues();
    }
    
    @After
    public void tearDown() {
        if (bungeeSystem != null) {
            bungeeSystem.disable();
        }
        Teardown.resetSettingsTempValues();
    }

    @Test
    @Ignore
    public void testEnable() throws EnableException {
        bungeeSystem = null; //TODO

        Settings.WEBSERVER_PORT.setTemporaryValue(9005);
        Settings.BUNGEE_IP.setTemporaryValue("8.8.8.8");
        Settings.DB_TYPE.setTemporaryValue("sqlite");
        bungeeSystem.setDatabaseSystem(new ServerDBSystem(Locale::new));

        bungeeSystem.enable();
    }

    @Test
    @Ignore
    public void testEnableDefaultIP() throws EnableException {
        thrown.expect(EnableException.class);
        thrown.expectMessage("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");

        bungeeSystem = null; //TODO

        Settings.WEBSERVER_PORT.setTemporaryValue(9005);
        Settings.DB_TYPE.setTemporaryValue("sqlite");
        bungeeSystem.setDatabaseSystem(new ServerDBSystem(Locale::new));

        bungeeSystem.enable();
    }

    @Test
    @Ignore("MySQL Driver unavailable for some reason.")
    public void testEnableNoMySQL() throws EnableException {
        thrown.expect(EnableException.class);
        thrown.expectMessage("Database failed to initialize");

        bungeeSystem = null; //TODO
        bungeeSystem.enable();
    }
}
