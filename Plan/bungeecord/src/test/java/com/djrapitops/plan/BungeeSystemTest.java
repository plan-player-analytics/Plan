/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plan.system.settings.paths.ProxySettings;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import rules.BungeeComponentMocker;
import rules.ComponentMocker;
import utilities.RandomData;

/**
 * Test for Bungee PlanSystem.
 *
 * @author Rsl1122
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class BungeeSystemTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @Rule
    public ComponentMocker component = new BungeeComponentMocker(temporaryFolder);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void bungeeEnables() throws Exception {
        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "8.8.8.8");

            DBSystem dbSystem = bungeeSystem.getDatabaseSystem();
            dbSystem.setActiveDatabase(dbSystem.getSqLiteFactory().usingDefaultFile());

            bungeeSystem.enable();
        } finally {
            bungeeSystem.disable();
        }
    }

    @Test
    public void bungeeDoesNotEnableWithDefaultIP() throws Exception {
        thrown.expect(EnableException.class);
        thrown.expectMessage("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");

        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            config.set(DatabaseSettings.MYSQL_PORT, "6000");
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "0.0.0.0");

            DBSystem dbSystem = bungeeSystem.getDatabaseSystem();
            dbSystem.setActiveDatabase(dbSystem.getSqLiteFactory().usingDefaultFile());

            bungeeSystem.enable();
        } finally {
            bungeeSystem.disable();
        }
    }

    @Test
    public void testEnableNoMySQL() throws EnableException {
        thrown.expect(EnableException.class);
        thrown.expectMessage("Failed to initialize pool: Communications link failure");

        PlanSystem bungeeSystem = component.getPlanSystem();
        try {
            PlanConfig config = bungeeSystem.getConfigSystem().getConfig();
            config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
            config.set(ProxySettings.IP, "8.8.8.8");

            bungeeSystem.enable();
        } finally {
            bungeeSystem.disable();
        }
    }
}
