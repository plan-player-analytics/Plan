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
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.StoreWebGroupTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import utilities.RandomData;
import utilities.TestResources;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author AuroraLS3
 */
@ExtendWith({SeleniumExtension.class, FullSystemExtension.class})
class AccessControlVisibilityTest {
    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    private static final String PASSWORD = "testPass";

    @BeforeAll
    static void setUp(PlanSystem system, Path tempDir, PlanConfig config) throws Exception {
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
        system.enable();
    }

    @AfterEach
    void tearDownTest(WebDriver driver) {
        SeleniumExtension.newTab(driver);
        driver.manage().deleteAllCookies();
    }

    @AfterAll
    static void tearDown(PlanSystem system) {
        system.disable();
    }

    User registerUser(Database db, WebPermission... permissions) throws Exception {
        String groupName = RandomData.randomString(75);
        db.executeTransaction(
                new StoreWebGroupTransaction(groupName, Arrays.stream(permissions)
                        .map(WebPermission::getPermission)
                        .collect(Collectors.toList()))
        ).get();

        User user = new User(RandomData.randomString(45), "console", null, PassEncryptUtil.createHash(PASSWORD), groupName, Collections.emptyList());
        db.executeTransaction(new StoreWebUserTransaction(user)).get();

        return user;
    }

    void login(WebDriver driver, User user) {
        driver.findElement(By.id("inputUser")).sendKeys(user.getUsername());
        driver.findElement(By.id("inputPassword")).sendKeys(PASSWORD);
        driver.findElement(By.id("login-button")).click();
    }

    @Test
    void adminHasAccessToManagePage(Database database, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.MANAGE_GROUPS);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/manage";
        driver.get(address);
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        Thread.sleep(250); // Wait for React render
        login(driver, user);
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        Thread.sleep(250); // Wait for React render
        assertTrue(driver.findElement(By.id("slice_h_0")).isDisplayed(), "Could not see groups");
    }

    static Stream<Arguments> serverPageElementVisibleCases() {
        return Stream.of(
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH, "players-online-graph", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW, "players-online-graph", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW, "last-7-days", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW, "server-as-numbers", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW, "week-comparison", "overview")
        );
    }

    @ParameterizedTest(name = "Access to server page with visibility {0} can see element #{1} in section /server/uuid/{2}")
    @MethodSource("serverPageElementVisibleCases")
    void serverPageElementVisible(WebPermission permission, String element, String section, Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_SERVER, permission);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/server/" + serverUUID + "/" + section;
        driver.get(address);
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        Thread.sleep(250); // Wait for React render
        login(driver, user);
        SeleniumExtension.waitForPageLoadForSeconds(5, driver);
        Thread.sleep(250); // Wait for React render
        assertTrue(driver.findElement(By.id(element)).isDisplayed(), () -> "Did not see #" + element + " at " + address + " with permission '" + permission.getPermission() + "'");
    }
}
