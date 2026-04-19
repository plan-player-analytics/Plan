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
import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.component.ComponentService;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.ComponentProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.gathering.domain.DataMap;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogType;
import utilities.RandomData;
import utilities.TestConstants;

import java.util.*;
import java.util.stream.Stream;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.assertNoLogs;

/**
 * This test class is for catching any JavaScript errors.
 * <p>
 * Errors may have been caused by:
 * - Javascript mistakes / build issues
 * - Missed console.log statements
 * - Missing file definition in Mocker
 */
@ExtendWith({FullSystemExtension.class, SeleniumExtension.class})
class JSErrorRegressionTest {

    private static final int TEST_PORT_NUMBER = 9091;

    @BeforeAll
    static void setUpClass(PlanSystem system) {
        system.getConfigSystem().getConfig()
                .set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        system.enable();
        savePlayerData(system);
    }

    private static void savePlayerData(PlanSystem system) {
        ServerUUID serverUUID = system.getServerInfo().getServerUUID();
        DBSystem dbSystem = system.getDatabaseSystem();
        Database database = dbSystem.getDatabase();
        UUID uuid = TestConstants.PLAYER_ONE_UUID;
        database.executeTransaction(new PlayerRegisterTransaction(uuid, RandomData::randomTime, TestConstants.PLAYER_ONE_NAME));
        FinishedSession session = new FinishedSession(uuid, serverUUID, 1000L, 11000L, 500L, new DataMap());
        database.executeTransaction(new StoreWorldNameTransaction(serverUUID, "world"));
        database.executeTransaction(new StoreSessionTransaction(session));
    }

    @AfterAll
    static void tearDownClass(PlanSystem system) {
        if (system != null) {
            system.disable();
        }
    }

    @AfterEach
    void tearDownTest(WebDriver driver) {
        SeleniumExtension.newTab(driver);
    }

    @TestFactory
    Collection<DynamicTest> javascriptRegressionTest(ChromeDriver driver, PlanSystem system) {
        String[] addresses = new String[]{
                "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_NAME,
                "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID_STRING,
                "http://localhost:" + TEST_PORT_NUMBER + "/network",
                "http://localhost:" + TEST_PORT_NUMBER + "/server/Server 1",
                "http://localhost:" + TEST_PORT_NUMBER + "/players",
                "http://localhost:" + TEST_PORT_NUMBER + "/query"
        };

        return Arrays.stream(addresses)
                .map(link -> testAddress(link, driver, system))
                .toList();
    }

    @TestFactory
    Stream<DynamicTest> componentJsRegressionTest(ChromeDriver driver, PlanSystem system) {
        system.getApiServices().getExtensionService()
                .register(new ComponentExtension())
                .orElseThrow(AssertionError::new)
                .updatePlayerData(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);

        String address = "http://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + "/plugins/" + system.getServerInfo().getServerIdentifier().getName().replaceAll(" ", "%20");
        return Stream.of(testAddress(address, driver, system));
    }

    private DynamicTest testAddress(String address, ChromeDriver driver, PlanSystem system) {
        return DynamicTest.dynamicTest("Page should not log anything on js console: " + address, () -> {
            Locale locale = system.getLocaleSystem().getLocale();
            try {
                driver.get(address);
                SeleniumExtension.waitForPageLoadForSeconds(5, driver);
                SeleniumExtension.waitForElementToBeVisible(By.className("load-in"), driver);
                assertNoLogs(driver.manage().logs().get(LogType.BROWSER).getAll(), address);
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
            SeleniumExtension.waitForPageLoadForSeconds(3, driver);

            assertNoLogs(driver, "Page link '" + address + "'->'" + href + "'");
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
                    .toList();
        } catch (StaleElementReferenceException _) {
            return getLinks(driver, attempt + 1);
        }
    }

    @PluginInfo(name = "Component-regression")
    static class ComponentExtension implements DataExtension {
        @ComponentProvider(text = "regression")
        public Component component(UUID playerUUID) {
            @Language("JSON")
            String json = """
                    {
                      "extra": [
                        {
                          "color": "gray",
                          "extra": [
                            {
                              "color": "#9D50BB",
                              "extra": [
                                {
                                  "color": "#914EB7",
                                  "extra": [
                                    {
                                      "color": "#864CB3",
                                      "extra": [
                                        {
                                          "color": "#7A4AAE",
                                          "extra": [
                                            {
                                              "color": "#6E48AA",
                                              "extra": [
                                                {
                                                  "color": "gray",
                                                  "extra": [
                                                    {
                                                      "color": "dark_aqua",
                                                      "text": "Executive Advisor"
                                                    }
                                                  ],
                                                  "text": "] "
                                                }
                                              ],
                                              "text": "f"
                                            }
                                          ],
                                          "text": "f"
                                        }
                                      ],
                                      "text": "a"
                                    }
                                  ],
                                  "text": "t"
                                }
                              ],
                              "text": "S"
                            }
                          ],
                          "text": "["
                        }
                      ],
                      "text": " "
                    }""";
            return ComponentService.getInstance().fromJson(json);
        }

        @ComponentProvider(text = "regression2")
        public Component component2(UUID playerUUID) {
            @Language("JSON")
            String json2 = """
                    {"color":"dark_aqua","text":"EA"}""";
            return ComponentService.getInstance().fromJson(json2);
        }
    }
}