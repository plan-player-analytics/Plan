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
import com.djrapitops.plan.extension.ExtensionService;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestExtensions;
import utilities.TestResources;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.assertNoLogs;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
    static void setUp(PlanSystem system, @TempDir Path tempDir, PlanConfig config) throws Exception {
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
    }

    @AfterAll
    static void tearDown(PlanSystem system) {
        system.disable();
    }

    private static void storePlayer(Database database, ServerUUID serverUUID) throws ExecutionException, InterruptedException {
        storePlayer(database, serverUUID, TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
    }

    private static void storePlayer(Database database, ServerUUID serverUUID, UUID playerUUID, String playerName) throws ExecutionException, InterruptedException {
        database.executeTransaction(new StoreServerPlayerTransaction(playerUUID, System.currentTimeMillis(), playerName, serverUUID, TestConstants.GET_PLAYER_HOSTNAME.get()))
                .get();
    }

    static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(new TestExtensions.PlayerGraphExtension()
                        .point(() -> Optional.empty())
                        .pointHistory(() -> List.of())
                        .seriesMetadata(() -> List.of())
                )
        );
    }

    @BeforeEach
    void setUp(Database database, ServerUUID serverUUID) {
        RandomData.dateOrderedTPS(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12)).forEach(tps -> database.executeTransaction(new TPSStoreTransaction(serverUUID, tps)).join());
        RandomData.dateOrderedTPS(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(8)).forEach(tps -> database.executeTransaction(new TPSStoreTransaction(serverUUID, tps)).join());
        RandomData.dateOrderedTPS(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(15)).forEach(tps -> database.executeTransaction(new TPSStoreTransaction(serverUUID, tps)).join());
        RandomData.dateOrderedTPS(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(22)).forEach(tps -> database.executeTransaction(new TPSStoreTransaction(serverUUID, tps)).join());
        RandomData.dateOrderedTPS(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(29)).forEach(tps -> database.executeTransaction(new TPSStoreTransaction(serverUUID, tps)).join());
    }

    @AfterEach
    void tearDownTest(WebDriver driver) {
        String address = "https://localhost:" + TEST_PORT_NUMBER + "/auth/logout";
        driver.get(address);
        SeleniumExtension.newTab(driver);
        driver.manage().deleteAllCookies();
    }

    User registerUser(Database db, WebPermission... permissions) throws Exception {
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

    @ParameterizedTest
    @MethodSource("testCases")
    void graphVisible(TestExtensions.PlayerGraphExtension graphExtension, Database database, ExtensionService extensionService, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS, WebPermission.PAGE, WebPermission.DATA);

        extensionService.register(graphExtension);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + "/plugins/" + TestConstants.SERVER_NAME;
        driver.get(address);
        login(driver, user);

        String element = "plan_extension_graph_playergraphextension_graph";
        SeleniumExtension.waitForElementToBeVisible(By.id(element), driver);
        assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address);
        assertNoLogs(driver, address);
    }

    void login(ChromeDriver driver, User user) {
//        String cookie = AccessControlTest.login("https://localhost:" + TEST_PORT_NUMBER, user.getUsername());
//        driver.manage().addCookie(new Cookie("auth", cookie.split("=")[1]));
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        SeleniumExtension.waitForElementToBeVisible(By.id("inputUser"), driver);

        driver.findElement(By.id("inputUser")).sendKeys(user.getUsername());
        driver.findElement(By.id("inputPassword")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
    }
}
