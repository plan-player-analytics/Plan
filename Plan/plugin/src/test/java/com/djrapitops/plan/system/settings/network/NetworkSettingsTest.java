package com.djrapitops.plan.system.settings.network;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import rules.BukkitComponentMocker;
import rules.ComponentMocker;
import utilities.RandomData;

public class NetworkSettingsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new BukkitComponentMocker(temporaryFolder);

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @AfterClass
    public static void tearDownClass() {
        component.getPlanSystem().disable();
    }

    @Test
    public void transferDoesNotProduceException() throws EnableException {
        PlanSystem system = component.getPlanSystem();
        system.getConfigSystem().getConfig().set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        system.enable();

        NetworkSettings networkSettings = system.getConfigSystem().getConfig().getNetworkSettings();
        networkSettings.placeToDatabase();
        networkSettings.loadFromDatabase();
    }

}