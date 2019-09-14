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
package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.db.access.transactions.events.SessionEndTransaction;
import com.djrapitops.plan.db.access.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.jayway.awaitility.Awaitility;
import extension.SeleniumExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * This test class is for catching any JavaScript errors.
 * <p>
 * Errors may have been caused by:
 * - Missing placeholders {@code ${placeholder}} inside {@code <script>} tags.
 * - Automatic formatting of plugin javascript (See https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/820)
 * - Missing file definition in Mocker
 */
@RunWith(JUnitPlatform.class)
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
        database.executeTransaction(new PlayerRegisterTransaction(uuid, () -> 1000L, "name"));
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
        assertFalse(driver.getPageSource().contains("500 Internal Error occurred"), driver.getPageSource());
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
        // Open the page that has refreshing info
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/server");
        assertNo500Error(driver);

        // Wait until Plan caches analysis results
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> ResponseCache.loadResponse(PageId.SERVER.of(serverUUID)) != null);

        // Open the page with analysis stuff
        SeleniumExtension.newTab(driver);
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