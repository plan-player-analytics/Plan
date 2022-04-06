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
import com.djrapitops.plan.gathering.domain.DataMap;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.LangCode;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.SessionEndTransaction;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import extension.SeleniumExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private static final int TEST_PORT_NUMBER = 9091;

    public static PluginMockComponent component;

    private static PlanSystem planSystem;
    private static ServerUUID serverUUID;

    @BeforeAll
    static void setUpClass(@TempDir Path tempDir) throws Exception {
        component = new PluginMockComponent(tempDir);
        planSystem = component.getPlanSystem();

        PlanConfig config = planSystem.getConfigSystem().getConfig();
        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        config.set(ProxySettings.IP, "localhost:" + TEST_PORT_NUMBER);
        config.set(PluginSettings.FRONTEND_BETA, true);

        // Avoid accidentally DDoS:ing head image service during tests.
        config.set(DisplaySettings.PLAYER_HEAD_IMG_URL, "http://localhost:" + TEST_PORT_NUMBER + "/${playerUUID}/img/Flaticon_circle.png");

        planSystem.enable();
        serverUUID = planSystem.getServerInfo().getServerUUID();
        savePlayerData();
    }

    private static void savePlayerData() {
        DBSystem dbSystem = planSystem.getDatabaseSystem();
        Database database = dbSystem.getDatabase();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, "world"));
        database.executeTransaction(new SessionEndTransaction(session));
    }

    @AfterAll
    static void tearDownClass() {
        if (planSystem != null) {
            planSystem.disable();
        }
    }

    @AfterEach
    void tearDownTest(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> javascriptRegressionTest(ChromeDriver driver) {
        String[] addresses = new String[]{
                "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_NAME,
                "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID_STRING,
                "http://localhost:" + TEST_PORT_NUMBER + "/network",
                "http://localhost:" + TEST_PORT_NUMBER + "/server/Server 1",
                "http://localhost:" + TEST_PORT_NUMBER + "/players",
                "http://localhost:" + TEST_PORT_NUMBER + "/query"
        };

        LangCode[] languages = LangCode.values();
        return Arrays.stream(languages)
                .filter(lang -> lang != LangCode.CUSTOM)
                .flatMap(lang ->
                        Arrays.stream(addresses)
                                .map(link -> testAddress(link, lang, driver)))
                .collect(Collectors.toList());
    }

    private DynamicTest testAddress(String address, LangCode language, ChromeDriver driver) {
        return DynamicTest.dynamicTest("Page should not log anything on js console (" + language.name() + "): " + address, () -> {
            Locale locale = planSystem.getLocaleSystem().getLocale();
            try {
                locale.loadFromAnotherLocale(Locale.forLangCode(language, planSystem.getPlanFiles()));

                driver.get(address);
                Thread.sleep(250);

                List<LogEntry> logs = new ArrayList<>();
                logs.addAll(driver.manage().logs().get(LogType.CLIENT).getAll());
                logs.addAll(driver.manage().logs().get(LogType.BROWSER).getAll());

                assertNoLogs("'" + address + "' (in " + language.name() + "),", logs);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                locale.clear(); // Reset locale after test
            }
        });
    }

    @DisplayName("Links on page function: ")
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_NAME,
            "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID_STRING,
            "http://localhost:" + TEST_PORT_NUMBER + "/network",
            "http://localhost:" + TEST_PORT_NUMBER + "/server/Server 1",
            "http://localhost:" + TEST_PORT_NUMBER + "/players",
            "http://localhost:" + TEST_PORT_NUMBER + "/query"
    })
    void linkFunctionRegressionTest(String address, ChromeDriver driver) {
        driver.get(address);

        List<String> anchorLinks = getLinks(driver, 0);

        for (String href : anchorLinks) {
            driver.get(address);
            Awaitility.await()
                    .atMost(3, TimeUnit.SECONDS)
                    .until(() -> "complete".equals(driver.executeScript("return document.readyState")));

            List<LogEntry> logs = new ArrayList<>();
            logs.addAll(driver.manage().logs().get(LogType.CLIENT).getAll());
            logs.addAll(driver.manage().logs().get(LogType.BROWSER).getAll());

            assertNoLogs("Page link '" + address + "'->'" + href + "' issue: ", logs);
            System.out.println("'" + address + "' has link to " + href);
        }
    }

    private List<String> getLinks(ChromeDriver driver, int attempt) {
        if (attempt >= 5) return Collections.emptyList();

        try {
            return driver.findElements(By.tagName("a")).stream()
                    .map(anchorLink -> anchorLink.getAttribute("href"))
                    .filter(Objects::nonNull)
                    .filter(href -> href.contains("localhost") && !href.contains("logout"))
                    .map(href -> href.split("#")[0])
                    .distinct()
                    .collect(Collectors.toList());
        } catch (StaleElementReferenceException e) {
            return getLinks(driver, attempt + 1);
        }
    }


    private void assertNoLogs(String testName, List<LogEntry> logs) {
        assertTrue(logs.isEmpty(), () -> testName + "Browser console included " + logs.size() + " logs: " + logs.stream()
                .map(log -> "\n" + log.getLevel().getName() + " " + log.getMessage())
                .collect(Collectors.toList()));
    }
}