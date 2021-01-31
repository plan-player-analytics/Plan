/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.SessionEndTransaction;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import extension.SeleniumExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.WebDriver;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This test class is for catching any JavaScript errors.
 * <p>
 * Errors may have been caused by:
 * - Missing placeholders {@code ${placeholder}} inside {@code <script>} tags.
 * - Automatic formatting of plugin javascript (See https://github.com/plan-player-analytics/Plan/issues/820)
 * - Missing file definition in Mocker
 */
@ExtendWith(SeleniumExtension.class)
class JSErrorRegressionTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    public static PluginMockComponent component;

    private static PlanSystem bukkitSystem;
    private static UUID serverUUID;

    @BeforeAll
    static void setUpClass(@TempDir Path tempDir) throws Exception {
        component = new PluginMockComponent(tempDir);
        bukkitSystem = component.getPlanSystem();

        PlanConfig config = bukkitSystem.getConfigSystem().getConfig();
        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        bukkitSystem.enable();
        serverUUID = bukkitSystem.getServerInfo().getServerUUID();
        savePlayerData();
    }

    private static void savePlayerData() {
        DBSystem dbSystem = bukkitSystem.getDatabaseSystem();
        Database database = dbSystem.getDatabase();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, "name"));
        Session session = new Session(uuid, serverUUID, 1000L, "world", "SURVIVAL");
        session.endSession(11000L);
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, "world"));
        database.executeTransaction(new SessionEndTransaction(session));
    }

    @AfterAll
    static void tearDownClass() {
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
    }

    @AfterEach
    void tearDownTest(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @Test
    void playerPageDoesNotHaveJavascriptErrors(WebDriver driver) {
        System.out.println("Testing Player Page");
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/player/TestPlayer");
        assertNo500Error(driver);
    }

    private void assertNo500Error(WebDriver driver) {
        assertFalse(driver.getPageSource().contains("<span class=\"loader-text\">Error occurred"), driver.getPageSource());
    }

    @Test
    void playerPageAccessibleViaUUID(WebDriver driver) {
        System.out.println("Testing Player Page via UUID");
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID);
        assertNo500Error(driver);
    }

    @Test
    void serverPageDoesNotHaveJavascriptErrors(WebDriver driver) {
        System.out.println("Testing Server Page");
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/server");
        assertNo500Error(driver);
    }

    @Test
    void playersPageDoesNotHaveJavascriptErrors(WebDriver driver) {
        System.out.println("Testing Players Page");
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/players");
        assertNo500Error(driver);
    }

    @Test
    void debugPageDoesNotHaveJavascriptErrors(WebDriver driver) {
        System.out.println("Testing Debug Page");
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/debug");
        assertNo500Error(driver);
    }
}