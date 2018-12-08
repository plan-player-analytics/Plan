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

public class NetworkSettingsTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new BukkitComponentMocker(temporaryFolder);

    @AfterClass
    public static void tearDownClass() {
        component.getPlanSystem().disable();
    }

    @Test
    public void transferDoesNotProduceException() throws EnableException {
        PlanSystem system = component.getPlanSystem();
        system.getConfigSystem().getConfig().set(WebserverSettings.PORT, 9005);
        system.enable();

        NetworkSettings networkSettings = system.getConfigSystem().getConfig().getNetworkSettings();
        networkSettings.placeToDatabase();
        networkSettings.loadFromDatabase();
    }

}