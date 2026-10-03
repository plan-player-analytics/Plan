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
package com.djrapitops.plan.extension.graph;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.implementation.providers.gathering.ExtensionMetadataStorage;
import com.djrapitops.plan.extension.implementation.providers.gathering.ServerGraphSampler;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.init.RemoveOldExtensionsTransaction;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import utilities.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.assertNoLogs;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Test for visibility of extension graphs.
 *
 * @author AuroraLS3
 */
@ExtendWith({SeleniumExtension.class, FullSystemExtension.class})
class ExtensionGraphTest {
    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    private static final String PASSWORD = "testPass";

    @BeforeAll
    static void setUp(PlanSystem system, @TempDir Path tempDir, PlanConfig config, ChromeDriver driver) throws Exception {
        File certFile = tempDir.resolve("TestCert.p12").toFile();
        File testCert = TestResources.getTestResourceFile("TestCert.p12", ConfigUpdater.class);
        Files.copy(testCert.toPath(), certFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        // Avoid accidentally DDoS:ing head image service during tests.
        config.set(DisplaySettings.PLAYER_HEAD_IMG_URL, "data:image/png;base64,AA==");
        config.set(WebserverSettings.CERTIFICATE_PATH, certFile.getAbsolutePath());
        config.set(WebserverSettings.CERTIFICATE_KEYPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_STOREPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_ALIAS, "test");
        config.set(DataGatheringSettings.ACCEPT_GEOLITE2_EULA, true);
        config.set(DataGatheringSettings.GEOLOCATIONS, true);
        system.enable();

        User user = registerUser(system.getDatabaseSystem().getDatabase(), WebPermission.ACCESS, WebPermission.PAGE, WebPermission.DATA);
        driver.get("https://localhost:" + TEST_PORT_NUMBER + "/");
        login(driver, user);
    }

    @AfterAll
    static void tearDown(PlanSystem system, ChromeDriver driver) {
        String address = "https://localhost:" + TEST_PORT_NUMBER + "/auth/logout";
        driver.get(address);
        driver.manage().deleteAllCookies();
        system.disable();
    }

    private static void storePlayer(Database database, ServerUUID serverUUID) {
        storePlayer(database, serverUUID, TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
    }

    private static void storePlayer(Database database, ServerUUID serverUUID, UUID playerUUID, String playerName) {
        database.executeTransaction(new StoreServerPlayerTransaction(playerUUID, System.currentTimeMillis(), playerName, serverUUID, TestConstants.GET_PLAYER_HOSTNAME.get()))
                .join();
    }

    static Stream<Arguments> playerTestCases() {
        return Stream.of(
                Arguments.of("1. Green path: n values and n metadata", new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0)))
                        .pointHistory(() -> List.of(
                                new DataPoint(System.currentTimeMillis() - 1000, 5.0, 15.0),
                                new DataPoint(System.currentTimeMillis(), 10.0, 20.0)
                        ))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series A", "ms", GraphFormatType.MILLISECONDS, "#ff0000"),
                                new SeriesMetadata("Series B", "MB", GraphFormatType.BYTES, "#00ff00")
                        ))
                ),
                Arguments.of("2. Empty defaults (omit builder methods)", new TestExtensions.PlayerGraphExtension()),
                Arguments.of("3. Nulls returned: point returns null", new TestExtensions.PlayerGraphExtension()
                        .point(() -> null)
                ),
                Arguments.of("4. Nulls returned: pointHistory returns null", new TestExtensions.PlayerGraphExtension()
                        .pointHistory(() -> null)
                ),
                Arguments.of("5. Nulls returned: seriesMetadata returns null", new TestExtensions.PlayerGraphExtension()
                        .seriesMetadata(() -> null)
                ),
                Arguments.of("6. Nulls returned: all return null", new TestExtensions.PlayerGraphExtension()
                        .point(() -> null)
                        .pointHistory(() -> null)
                        .seriesMetadata(() -> null)
                ),
                Arguments.of("7. Nulls returned: DataPoint contains null values and series metadata contains null fields", new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), Arrays.asList(null, 42.0))))
                        .pointHistory(() -> Collections.singletonList(null))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata(null, null, null, null),
                                new SeriesMetadata("Series 2", null, GraphFormatType.NONE, null)
                        ))
                ),
                Arguments.of("8. Length mismatch: metadata > dataPoint values", new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0)))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis(), 10.0)))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series 1", "ms", GraphFormatType.MILLISECONDS, null),
                                new SeriesMetadata("Series 2", "ms", GraphFormatType.MILLISECONDS, null),
                                new SeriesMetadata("Series 3", "ms", GraphFormatType.MILLISECONDS, null)
                        ))
                ),
                Arguments.of("9. Length mismatch: metadata < dataPoint values", new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0, 30.0)))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0, 30.0)))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series 1", "ms", GraphFormatType.MILLISECONDS, null)
                        ))
                ),
                Arguments.of("10. Length mismatch: empty metadata, non-empty data points", new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0)))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0)))
                        .seriesMetadata(() -> List.of())
                ),
                Arguments.of("11. Length mismatch: non-empty metadata, empty data points", new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis())))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis())))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series 1", "ms", GraphFormatType.MILLISECONDS, null)
                        ))
                ),
                Arguments.of("12. Exceptions: point throws RuntimeException", new TestExtensions.PlayerGraphExtension()
                        .point(() -> {
                            throw new RuntimeException("Error in getPoint");
                        })
                ),
                Arguments.of("13. Exceptions: pointHistory throws RuntimeException", new TestExtensions.PlayerGraphExtension()
                        .pointHistory(() -> {
                            throw new RuntimeException("Error in getPointHistory");
                        })
                ),
                Arguments.of("15. point NotReadyException", new TestExtensions.PlayerGraphExtension()
                        .point(() -> {
                            throw new NotReadyException();
                        })
                ),
                Arguments.of("16. pointHistory NotReadyException", new TestExtensions.PlayerGraphExtension()
                        .pointHistory(() -> {
                            throw new NotReadyException();
                        })
                ),
                Arguments.of("17. seriesMetadata NotReadyException", new TestExtensions.PlayerGraphExtension()
                        .seriesMetadata(() -> {
                            throw new NotReadyException();
                        })
                )
        );
    }

    static Stream<Arguments> serverTestCases() {
        return Stream.of(
                Arguments.of("1. Green path: n values and n metadata", new TestExtensions.ServerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0)))
                        .pointHistory(() -> List.of(
                                new DataPoint(System.currentTimeMillis() - 1000, 5.0, 15.0),
                                new DataPoint(System.currentTimeMillis(), 10.0, 20.0)
                        ))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series A", "ms", GraphFormatType.MILLISECONDS, "#ff0000"),
                                new SeriesMetadata("Series B", "MB", GraphFormatType.BYTES, "#00ff00")
                        ))
                ),
                Arguments.of("2. Empty defaults (omit builder methods)", new TestExtensions.ServerGraphExtension()),
                Arguments.of("3. Nulls returned: point returns null", new TestExtensions.ServerGraphExtension()
                        .point(() -> null)
                ),
                Arguments.of("4. Nulls returned: pointHistory returns null", new TestExtensions.ServerGraphExtension()
                        .pointHistory(() -> null)
                ),
                Arguments.of("5. Nulls returned: seriesMetadata returns null", new TestExtensions.ServerGraphExtension()
                        .seriesMetadata(() -> null)
                ),
                Arguments.of("6. Nulls returned: all return null", new TestExtensions.ServerGraphExtension()
                        .point(() -> null)
                        .pointHistory(() -> null)
                        .seriesMetadata(() -> null)
                ),
                Arguments.of("7. Nulls returned: DataPoint contains null values and series metadata contains null fields", new TestExtensions.ServerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), Arrays.asList(null, 42.0))))
                        .pointHistory(() -> Collections.singletonList(null))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata(null, null, null, null),
                                new SeriesMetadata("Series 2", null, GraphFormatType.NONE, null)
                        ))
                ),
                Arguments.of("8. Length mismatch: metadata > dataPoint values", new TestExtensions.ServerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0)))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis(), 10.0)))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series 1", "ms", GraphFormatType.MILLISECONDS, null),
                                new SeriesMetadata("Series 2", "ms", GraphFormatType.MILLISECONDS, null),
                                new SeriesMetadata("Series 3", "ms", GraphFormatType.MILLISECONDS, null)
                        ))
                ),
                Arguments.of("9. Length mismatch: metadata < dataPoint values", new TestExtensions.ServerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0, 30.0)))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0, 30.0)))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series 1", "ms", GraphFormatType.MILLISECONDS, null)
                        ))
                ),
                Arguments.of("10. Length mismatch: empty metadata, non-empty data points", new TestExtensions.ServerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0)))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis(), 10.0, 20.0)))
                        .seriesMetadata(() -> List.of())
                ),
                Arguments.of("11. Length mismatch: non-empty metadata, empty data points", new TestExtensions.ServerGraphExtension()
                        .point(() -> Optional.of(new DataPoint(System.currentTimeMillis())))
                        .pointHistory(() -> List.of(new DataPoint(System.currentTimeMillis())))
                        .seriesMetadata(() -> List.of(
                                new SeriesMetadata("Series 1", "ms", GraphFormatType.MILLISECONDS, null)
                        ))
                ),
                Arguments.of("12. Exceptions: point throws RuntimeException", new TestExtensions.ServerGraphExtension()
                        .point(() -> {
                            throw new RuntimeException("Error in getPoint");
                        })
                ),
                Arguments.of("13. Exceptions: pointHistory throws RuntimeException", new TestExtensions.ServerGraphExtension()
                        .pointHistory(() -> {
                            throw new RuntimeException("Error in getPointHistory");
                        })
                ),
                Arguments.of("15. point NotReadyException", new TestExtensions.ServerGraphExtension()
                        .point(() -> {
                            throw new NotReadyException();
                        })
                ),
                Arguments.of("16. pointHistory NotReadyException", new TestExtensions.ServerGraphExtension()
                        .pointHistory(() -> {
                            throw new NotReadyException();
                        })
                ),
                Arguments.of("17. seriesMetadata NotReadyException", new TestExtensions.ServerGraphExtension()
                        .seriesMetadata(() -> {
                            throw new NotReadyException();
                        })
                )
        );
    }

    static User registerUser(Database db, WebPermission... permissions) throws Exception {
        String groupName = RandomData.randomString(75);
        db.executeTransaction(
                new StoreWebGroupTransaction(groupName, Arrays.stream(permissions)
                        .map(WebPermission::getPermission)
                        .toList())
        ).get();

        User user = new User(RandomData.randomString(45), "console", null, PassEncryptUtil.createHash(PASSWORD), groupName, Collections.emptyList());
        db.executeTransaction(new StoreWebUserTransaction(user)).get();

        return user;
    }

    static void login(ChromeDriver driver, User user) {
//        String cookie = AccessControlTest.login("https://localhost:" + TEST_PORT_NUMBER, user.getUsername());
//        driver.manage().addCookie(new Cookie("auth", cookie.split("=")[1]));
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        SeleniumExtension.waitForElementToBeVisible(By.id("inputUser"), driver);

        driver.findElement(By.id("inputUser")).sendKeys(user.getUsername());
        driver.findElement(By.id("inputPassword")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
    }

    @BeforeEach
    void setUp(Database database, ServerUUID serverUUID, ChromeDriver driver) {
        storePlayer(database, serverUUID);
    }

    @AfterEach
    void tearDownTest(WebDriver driver, Database database, PlanConfig config, ServerUUID serverUUID) {
        SeleniumExtension.newTab(driver);
        removeExtensionData(database, config, serverUUID);
    }

    private void removeExtensionData(Database database, PlanConfig config, ServerUUID serverUUID) {
        config.getExtensionSettings().setEnabled("PlayerGraphExtension", false);
        config.getExtensionSettings().setEnabled("ServerGraphExtension", false);
        database.executeTransaction(new RemoveOldExtensionsTransaction(mock(ExtensionMetadataStorage.class), config.getExtensionSettings(), -TimeUnit.MINUTES.toMillis(60), serverUUID))
                .join();
        assertEquals(Map.of(), database.query(new ExtensionPlayerDataQuery(TestConstants.PLAYER_ONE_UUID)));
        assertEquals(List.of(), database.query(new ExtensionServerDataQuery(serverUUID)));
    }

    @DisplayName("Player extension graph functionality")
    @ParameterizedTest(name = "{0}")
    @MethodSource("playerTestCases")
    void playerGraphVisible(String testCase, TestExtensions.PlayerGraphExtension graphExtension, PlanConfig config, Database database, ServerUUID serverUUID, ExtensionSvc extensionService, ChromeDriver driver) {
        try {
            TestErrorLogger.throwErrors(!testCase.contains("throws"));
            config.getExtensionSettings().setEnabled("PlayerGraphExtension", true);
            assertDoesNotThrow(() -> extensionService.register(graphExtension));
            // Player join registers the sampler
            assertDoesNotThrow(() -> extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.PLAYER_JOIN));
            // Player leave calls the sampler
            assertDoesNotThrow(() -> extensionService.updatePlayerValues(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME, CallEvents.PLAYER_LEAVE));
            TestErrorLogger.throwErrors(true);

            String serverName = database.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)).map(Server::getIdentifiableName).orElse(serverUUID.toString())
                    .replace(" ", "%20");
            String address = "https://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + "/plugins/" + serverName;
            driver.get(address);

            String element = "plan_extension_graph_playergraphextension_playergraph";
            SeleniumExtension.waitForElementToBeVisible(By.id("player-plugin-data"), driver);
            assertAll(
                    () -> assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address),
                    () -> assertNoLogs(driver, address)
            );
        } finally {
            extensionService.unregister(graphExtension);
        }
    }

    @DisplayName("Server extension graph functionality")
    @ParameterizedTest(name = "{0}")
    @MethodSource("serverTestCases")
    void serverGraphVisible(String testCase, TestExtensions.ServerGraphExtension graphExtension, PlanConfig config, ServerUUID serverUUID, ExtensionSvc extensionService, ChromeDriver driver) {
        try {
            TestErrorLogger.throwErrors(!testCase.contains("throws"));
            config.getExtensionSettings().setEnabled("ServerGraphExtension", true);
            assertDoesNotThrow(() -> extensionService.register(graphExtension));
            // Stores point history
            assertDoesNotThrow(() -> extensionService.updateServerValues(CallEvents.SERVER_EXTENSION_REGISTER));
            // calls the sampler
            assertDoesNotThrow(() -> extensionService.getGraphSamplers().getActiveServerGraphSamplers()
                    .values().stream()
                    .flatMap(Collection::stream)
                    .forEach(ServerGraphSampler::run)
            );
            TestErrorLogger.throwErrors(true);

            String address = "https://localhost:" + TEST_PORT_NUMBER + "/server/" + serverUUID + "/plugins-overview";
            driver.get(address);

            String element = "plan_extension_graph_servergraphextension_servergraph";
            SeleniumExtension.waitForElementToBeVisible(By.id("server-plugin-data"), driver);
            assertAll(
                    () -> assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address),
                    () -> assertNoLogs(driver, address)
            );
        } finally {
            extensionService.unregister(graphExtension);
        }
    }
}
