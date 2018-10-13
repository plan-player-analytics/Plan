package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.DaggerPlanBukkitComponent;
import com.djrapitops.plan.PlanBukkitComponent;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.mocks.PlanBukkitMocker;

public class NetworkSettingsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static PlanBukkitComponent COMPONENT;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanBukkitMocker mocker = PlanBukkitMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        COMPONENT = DaggerPlanBukkitComponent.builder().plan(mocker.getPlanMock()).build();
    }

    @AfterClass
    public static void tearDownClass() {
        COMPONENT.system().disable();
    }

    @Test
    public void transferDoesNotProduceException() throws EnableException {
        PlanSystem system = COMPONENT.system();
        system.enable();

        NetworkSettings networkSettings = system.getConfigSystem().getConfig().getNetworkSettings();
        networkSettings.placeToDatabase();
        networkSettings.loadFromDatabase();
    }

}