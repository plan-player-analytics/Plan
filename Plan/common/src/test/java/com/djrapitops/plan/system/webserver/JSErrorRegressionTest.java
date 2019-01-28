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
import com.djrapitops.plan.db.access.queries.DataStoreQueries;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.access.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.WebserverSettings;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.jayway.awaitility.Awaitility;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;
import rules.ComponentMocker;
import rules.PluginComponentMocker;
import rules.SeleniumDriver;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;

/**
 * This test class is for catching any JavaScript errors.
 * <p>
 * Errors may have been caused by:
 * - Missing placeholders {@code ${placeholder}} inside {@code <script>} tags.
 * - Automatic formatting of plugin javascript (See https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/820)
 * - Missing file definition in Mocker
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class JSErrorRegressionTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static ComponentMocker component = new PluginComponentMocker(temporaryFolder);
    @ClassRule
    public static SeleniumDriver seleniumDriver = new SeleniumDriver();

    private static PlanSystem bukkitSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        bukkitSystem = component.getPlanSystem();

        PlanConfig config = bukkitSystem.getConfigSystem().getConfig();
        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        bukkitSystem.enable();
        savePlayerData();
    }

    private static void savePlayerData() {
        DBSystem dbSystem = bukkitSystem.getDatabaseSystem();
        Database database = dbSystem.getDatabase();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.save().registerNewUser(uuid, 1000L, "TestPlayer");
        Session session = new Session(uuid, TestConstants.SERVER_UUID, 1000L, "world", "SURVIVAL");
        session.endSession(11000L);
        database.executeTransaction(new WorldNameStoreTransaction(TestConstants.SERVER_UUID, "world"));
        database.executeTransaction(new Transaction() {
            @Override
            protected void performOperations() {
                execute(DataStoreQueries.storeSession(session));
            }
        });
        
        // TODO Refactor to use Event transactions when available.
    }

    @After
    public void tearDownTest() {
        seleniumDriver.newTab();
    }

    @AfterClass
    public static void tearDownClass() {
        if (bukkitSystem != null) {
            bukkitSystem.disable();
        }
    }

    @Test
    public void playerPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Player Page");
        WebDriver driver = seleniumDriver.getDriver();
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/player/TestPlayer");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }

    @Test
    public void playerPageAccessibleViaUUID() {
        System.out.println("Testing Player Page via UUID");
        WebDriver driver = seleniumDriver.getDriver();
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID);
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("404 "));
    }

    @Test
    @Ignore("PlanPluginMocker displays network page for some reason. Investigate")
    public void serverPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Server Page");
        WebDriver driver = seleniumDriver.getDriver();
        // Open the page that has refreshing info
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/server");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));

        // Wait until Plan caches analysis results
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> ResponseCache.isCached(PageId.SERVER.of(TestConstants.SERVER_UUID)));

        // Open the page with analysis stuff
        seleniumDriver.newTab();
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/server");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }

    @Test
    @Ignore("PlanPluginMocker displays network page for some reason. Investigate")
    public void playersPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Players Page");
        WebDriver driver = seleniumDriver.getDriver();
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/players");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }

    @Test
    public void debugPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Debug Page");
        WebDriver driver = seleniumDriver.getDriver();
        driver.get("http://localhost:" + TEST_PORT_NUMBER + "/debug");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }
}