package com.djrapitops.plan.system.webserver;

import com.djrapitops.plan.DaggerPlanBukkitComponent;
import com.djrapitops.plan.PlanBukkitComponent;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.operation.SaveOperations;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.jayway.awaitility.Awaitility;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;
import rules.SeleniumDriver;
import utilities.TestConstants;
import utilities.mocks.PlanBukkitMocker;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;

/**
 * This test class is for catching any JavaScript errors.
 * <p>
 * Errors may have been caused by:
 * - Missing placeholders {@code ${placeholder}} inside {@code <script>} tags.
 * - Automatic formatting of plugin javascript (See https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/820)
 * - Missing file definition in {@link utilities.mocks.Mocker}
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class JSErrorRegressionTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    @ClassRule
    public static SeleniumDriver seleniumDriver = new SeleniumDriver();

    private static PlanSystem bukkitSystem;

    @BeforeClass
    public static void setUpClass() throws Exception {
        PlanBukkitMocker mocker = PlanBukkitMocker.setUp()
                .withDataFolder(temporaryFolder.getRoot())
                .withPluginDescription()
                .withResourceFetchingFromJar()
                .withServer();
        PlanBukkitComponent component = DaggerPlanBukkitComponent.builder().plan(mocker.getPlanMock()).build();

        bukkitSystem = component.system();

        PlanConfig config = bukkitSystem.getConfigSystem().getConfig();
        config.set(Settings.WEBSERVER_PORT, 9005);

        bukkitSystem.enable();
        savePlayerData();
    }

    private static void savePlayerData() {
        DBSystem dbSystem = bukkitSystem.getDatabaseSystem();
        SaveOperations save = dbSystem.getDatabase().save();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        save.registerNewUser(uuid, 1000L, "TestPlayer");
        Session session = new Session(uuid, TestConstants.SERVER_UUID, 1000L, "world", "SURVIVAL");
        session.endSession(11000L);
        save.session(uuid, session);
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
        driver.get("http://localhost:9005/player/TestPlayer");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }

    @Test
    public void serverPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Server Page");
        WebDriver driver = seleniumDriver.getDriver();
        // Open the page that has refreshing info
        driver.get("http://localhost:9005/server");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));

        // Wait until Plan caches analysis results
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> ResponseCache.isCached(PageId.SERVER.of(TestConstants.SERVER_UUID)));

        // Open the page with analysis stuff
        seleniumDriver.newTab();
        driver.get("http://localhost:9005/server");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }

    @Test
    public void playersPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Players Page");
        WebDriver driver = seleniumDriver.getDriver();
        driver.get("http://localhost:9005/players");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }

    @Test
    public void debugPageDoesNotHaveJavascriptErrors() {
        System.out.println("Testing Debug Page");
        WebDriver driver = seleniumDriver.getDriver();
        driver.get("http://localhost:9005/debug");
        assertFalse(driver.getPageSource(), driver.getPageSource().contains("500 Internal Error occurred"));
    }
}