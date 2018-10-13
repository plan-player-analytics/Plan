/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.PlanBukkitMocker;
import utilities.mocks.PlanBungeeMocker;

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

    private PlanSystem bukkitSystem;
    private PlanSystem bungeeSystem;

    private UUID bukkitUUID;
    private UUID bungeeUUID;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanBukkitMocker planBukkitMocker = PlanBukkitMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        bukkitMock = planBukkitMocker.getPlanMock();

        PlanBungeeMocker planBungeeMocker = PlanBungeeMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withLogging()
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withProxy();
        bungeeMock = planBungeeMocker.getPlanMock();
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
    }

    public void enable() throws EnableException {
//        Settings.WEBSERVER_PORT.setTemporaryValue(9005);

        bukkitSystem = null; // TODO
        bukkitSystem.enable();

        bukkitUUID = null;

        bungeeSystem = null; // TODO

//        Settings.WEBSERVER_PORT.setTemporaryValue(9250);
//        Settings.BUNGEE_IP.setTemporaryValue("localhost");
//        Settings.DB_TYPE.setTemporaryValue("sqlite");
//        bungeeSystem.setDatabaseSystem(new BukkitDBSystem(new Locale()));

        bungeeSystem.enable();

        bungeeUUID = null;

        System.out.println("------------------------------");
        System.out.println("Enable Complete");
        System.out.println("Bukkit: " + bukkitUUID);
        System.out.println("Bungee: " + bungeeUUID);
        System.out.println("------------------------------");
    }

    @Test
    @Ignore("Causes next BungeeSystem test to fail")
    public void testRequest() throws EnableException {
        enable();

        System.out.println("Sending request");
//        bungeeSystem.getInfoSystem().getConnectionSystem().sendWideInfoRequest(new GenerateInspectPluginsTabRequest(infoSystem, infoRequestFactory, TestConstants.PLAYER_ONE_UUID));
    }
}
