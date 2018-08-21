/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.request.GenerateInspectPluginsTabRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;
import utilities.TestConstants;
import utilities.mocks.BukkitMockUtil;
import utilities.mocks.BungeeMockUtil;

import java.util.UUID;

/**
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BungeeBukkitConnectionTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static Plan bukkitMock;
    private static PlanBungee bungeeMock;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private BukkitSystem bukkitSystem;
    private BungeeSystem bungeeSystem;

    private UUID bukkitUUID;
    private UUID bungeeUUID;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BukkitMockUtil bukkitMockUtil = BukkitMockUtil.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        bukkitMock = bukkitMockUtil.getPlanMock();

        BungeeMockUtil bungeeMockUtil = BungeeMockUtil.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withProxy();
        bungeeMock = bungeeMockUtil.getPlanMock();
    }

    @Before
    public void setUp() {
        Teardown.resetSettingsTempValues();
        Settings.DEBUG.setTemporaryValue("console");
        Settings.DEV_MODE.setTemporaryValue(true);
    }

    @After
    public void tearDown() {
        System.out.println("------------------------------");
        System.out.println("Disable");
        System.out.println("------------------------------");
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
        if (bungeeSystem != null) {
            bungeeSystem.disable();
        }
        Teardown.resetSettingsTempValues();
    }

    public void enable() throws EnableException {
        Settings.WEBSERVER_PORT.setTemporaryValue(9005);

        bukkitSystem = null; // TODO
        bukkitSystem.enable();

        bukkitUUID = ServerInfo.getServerUUID_Old();

        bungeeSystem = null; // TODO

        Settings.WEBSERVER_PORT.setTemporaryValue(9250);
        Settings.BUNGEE_IP.setTemporaryValue("localhost");
        Settings.DB_TYPE.setTemporaryValue("sqlite");
//        bungeeSystem.setDatabaseSystem(new BukkitDBSystem(new Locale()));

        bungeeSystem.enable();

        bungeeUUID = ServerInfo.getServerUUID_Old();

        System.out.println("------------------------------");
        System.out.println("Enable Complete");
        System.out.println("Bukkit: " + bukkitUUID);
        System.out.println("Bungee: " + bungeeUUID);
        System.out.println("------------------------------");
    }

    @Test
    @Ignore("Causes next BungeeSystem test to fail")
    public void testRequest() throws EnableException, WebException {
        enable();

        System.out.println("Sending request");
        bungeeSystem.getInfoSystem().getConnectionSystem().sendWideInfoRequest(new GenerateInspectPluginsTabRequest(TestConstants.PLAYER_ONE_UUID));
    }
}
