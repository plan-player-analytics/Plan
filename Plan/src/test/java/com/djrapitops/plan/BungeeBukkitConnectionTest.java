/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.Settings;
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
    private static PlanComponent BUKKIT_COMPONENT;
    private static PlanBungeeComponent BUNGEE_COMPONENT;

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
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        BUKKIT_COMPONENT = DaggerPlanComponent.builder().plan(planBukkitMocker.getPlanMock()).build();

        PlanBungeeMocker planBungeeMocker = PlanBungeeMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withProxy();
        BUNGEE_COMPONENT = DaggerPlanBungeeComponent.builder().plan(planBungeeMocker.getPlanMock()).build();
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

    public void enable() throws Exception {
        bukkitSystem = BUKKIT_COMPONENT.system();
        bungeeSystem = BUNGEE_COMPONENT.system();

        bukkitSystem.getConfigSystem().getConfig().set(Settings.WEBSERVER_PORT, 9005);
        bungeeSystem.getConfigSystem().getConfig().set(Settings.WEBSERVER_PORT, 9250);

        DBSystem dbSystem = bungeeSystem.getDatabaseSystem();
        dbSystem.setActiveDatabase(dbSystem.getSqLiteFactory().usingDefaultFile());

        bukkitSystem.enable();
        bungeeSystem.enable();

        bukkitUUID = bukkitSystem.getServerInfo().getServerUUID();
        bungeeUUID = bungeeSystem.getServerInfo().getServerUUID();

        System.out.println("------------------------------");
        System.out.println("Enable Complete");
        System.out.println("Bukkit: " + bukkitUUID);
        System.out.println("Bungee: " + bungeeUUID);
        System.out.println("------------------------------");
    }

    @Test
    @Ignore("Causes next BungeeSystem test to fail")
    public void testRequest() throws Exception {
        enable();

        System.out.println("Sending request");
//        bungeeSystem.getInfoSystem().getConnectionSystem().sendWideInfoRequest(new GenerateInspectPluginsTabRequest(infoSystem, infoRequestFactory, TestConstants.PLAYER_ONE_UUID));
    }
}
