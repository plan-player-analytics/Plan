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
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveWebGroupsTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreServerPlayerTransaction;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import extension.FullSystemExtension;
import extension.SeleniumExtension;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestResources;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.djrapitops.plan.delivery.export.ExportTestUtilities.assertNoLogs;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author AuroraLS3
 */
@ExtendWith({SeleniumExtension.class, FullSystemExtension.class})
class AccessControlVisibilityTest {
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

    static Stream<Arguments> serverPageElementVisibleCases() {
        return Stream.of(
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH, "players-online-graph", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW_NUMBERS, "last-7-days", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW_NUMBERS, "server-as-numbers", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_OVERVIEW_NUMBERS, "week-comparison", "overview"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS, "online-activity-graphs", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_DAY_BY_DAY, "day-by-day-nav", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_HOUR_BY_HOUR, "hour-by-hour-nav", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR, "server-calendar-nav", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_PUNCHCARD, "punchcard-nav", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_OVERVIEW, "online-activity-numbers", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_OVERVIEW, "online-activity-insights", "online-activity"),
                Arguments.arguments(WebPermission.PAGE_SERVER_SESSIONS_OVERVIEW, "session-insights", "sessions"),
                Arguments.arguments(WebPermission.PAGE_SERVER_SESSIONS_WORLD_PIE, "world-pie", "sessions"),
                Arguments.arguments(WebPermission.PAGE_SERVER_SESSIONS_LIST, "session-list", "sessions"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYER_VERSUS_OVERVIEW, "pvp-pve-as-numbers", "pvppve"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYER_VERSUS_OVERVIEW, "pvp-pve-insights", "pvppve"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYER_VERSUS_KILL_LIST, "pvp-kills-table", "pvppve"),
                Arguments.arguments(WebPermission.PAGE_SERVER_ALLOWLIST_BOUNCE, "allowlist-bounce-table", "allowlist"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYERBASE_OVERVIEW, "playerbase-trends", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYERBASE_OVERVIEW, "playerbase-insights", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYERBASE_GRAPHS, "playerbase-graph", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYERBASE_GRAPHS, "playerbase-current", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_TIME, "server-join-addresses", "join-addresses"),
                Arguments.arguments(WebPermission.PAGE_SERVER_RETENTION, "retention-graph", "retention"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLAYERS, "players-table", "players"),
                Arguments.arguments(WebPermission.PAGE_SERVER_GEOLOCATIONS_MAP, "geolocations", "geolocations"),
                Arguments.arguments(WebPermission.PAGE_SERVER_GEOLOCATIONS_PING_PER_COUNTRY, "ping-per-country", "geolocations"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS, "performance-graphs", "performance"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PERFORMANCE_OVERVIEW, "performance-as-numbers", "performance"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PERFORMANCE_OVERVIEW, "performance-insights", "performance"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLUGIN_HISTORY, "server-plugin-history", "plugin-history"),
                Arguments.arguments(WebPermission.PAGE_SERVER_PLUGINS, "server-plugin-data", "plugins-overview")
        );
    }

    static Stream<Arguments> networkPageElementVisibleCases() {
        return Stream.of(
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_ONLINE, "online-activity-nav", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_DAY_BY_DAY, "day-by-day-nav", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_HOUR_BY_HOUR, "hour-by-hour-nav", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR, "network-calendar-nav", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW, "recent-players", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW, "network-as-numbers", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_OVERVIEW, "week-comparison", "overview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_SERVER_LIST, "row-network-servers-0", "serversOverview"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_SESSIONS_OVERVIEW, "session-insights", "sessions"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_SESSIONS_SERVER_PIE, "server-pie", "sessions"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_SESSIONS_LIST, "session-list", "sessions"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLAYERBASE_OVERVIEW, "playerbase-trends", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLAYERBASE_OVERVIEW, "playerbase-insights", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLAYERBASE_GRAPHS, "playerbase-graph", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLAYERBASE_GRAPHS, "playerbase-current", "playerbase"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_TIME, "network-join-addresses", "join-addresses"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_RETENTION, "retention-graph", "retention"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLAYERS, "players-table", "players"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_GEOLOCATIONS_MAP, "geolocations", "geolocations"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_GEOLOCATIONS_PING_PER_COUNTRY, "ping-per-country", "geolocations"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PERFORMANCE, "row-network-performance-0", "performance"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLUGIN_HISTORY, "network-plugin-history", "plugin-history"),
                Arguments.arguments(WebPermission.PAGE_NETWORK_PLUGINS, "server-plugin-data", "plugins-overview")
        );
    }

    static Stream<Arguments> playerPageVisibleCases() {
        return Stream.of(
                Arguments.arguments(WebPermission.PAGE_PLAYER_OVERVIEW, "player-overview", "overview"),
                Arguments.arguments(WebPermission.PAGE_PLAYER_SESSIONS, "player-sessions", "sessions"),
                Arguments.arguments(WebPermission.PAGE_PLAYER_VERSUS, "player-pvp-pve", "pvppve"),
                Arguments.arguments(WebPermission.PAGE_PLAYER_SERVERS, "player-servers", "servers"),
                Arguments.arguments(WebPermission.PAGE_PLAYER_PLUGINS, "player-plugin-data", "plugins/Server%201")
        );
    }

    static Stream<Arguments> pageLevelVisibleCases() {
        return Stream.of(
                Arguments.arguments(WebPermission.MANAGE_GROUPS, "slice_h_0", "manage"),
                Arguments.arguments(WebPermission.ACCESS_QUERY, "query-button", "query"),
                Arguments.arguments(WebPermission.ACCESS_PLAYERS, "players-table", "players"),
                Arguments.arguments(WebPermission.ACCESS_ERRORS, "content", "errors"),
                Arguments.arguments(WebPermission.ACCESS_THEME_EDITOR, "theme-editor", "theme-editor/default"),
                Arguments.arguments(WebPermission.ACCESS_THEME_EDITOR, "add-theme", "theme-editor/new"),
                Arguments.arguments(WebPermission.ACCESS_THEME_EDITOR, "delete-theme", "theme-editor/delete")
//                Arguments.arguments(WebPermission.ACCESS_DOCS, "swagger-ui", "docs")
        );
    }

    private static void storePlayer(Database database, ServerUUID serverUUID) throws ExecutionException, InterruptedException {
        storePlayer(database, serverUUID, TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
    }

    private static void storePlayer(Database database, ServerUUID serverUUID, UUID playerUUID, String playerName) throws ExecutionException, InterruptedException {
        database.executeTransaction(new StoreServerPlayerTransaction(playerUUID, System.currentTimeMillis(), playerName, serverUUID, TestConstants.GET_PLAYER_HOSTNAME.get()))
                .get();
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
                        .collect(Collectors.toList()))
        ).get();

        User user = new User(RandomData.randomString(45), "console", null, PassEncryptUtil.createHash(PASSWORD), groupName, Collections.emptyList());
        db.executeTransaction(new StoreWebUserTransaction(user)).get();

        return user;
    }

    @DisplayName("Whole page is visible with permission")
    @ParameterizedTest(name = "Access with visibility {0} can see element #{1} in /{2}")
    @MethodSource("pageLevelVisibleCases")
    void pageVisible(WebPermission permission, String element, String page, Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        // TODO Remove after fixing manage/groups making bazillion calls to /v1/permissions
        database.executeTransaction(new RemoveWebGroupsTransaction()).get();
        User user = registerUser(database, permission);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/" + page;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.id(element), driver);
        assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address + " with permission '" + permission.getPermission() + "'");
        assertNoLogs(driver, address);
    }

    @DisplayName("Whole page is not visible with permission")
    @ParameterizedTest(name = "Access with no visibility needs {0} can't see element #{1} in /{2}")
    @MethodSource("pageLevelVisibleCases")
    void pageNotVisible(WebPermission permission, String element, String page, Database database, ChromeDriver driver) throws Exception {
        User user = registerUser(database);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/" + page;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.id("wrapper"), driver);
        By id = By.id(element);
        assertThrows(NoSuchElementException.class, () -> driver.findElement(id), () -> "Saw element #" + element + " at " + address + " without permission to");
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

    @DisplayName("Server element is visible with permission")
    @ParameterizedTest(name = "Access to server page with visibility {0} can see element #{1} in section /server/uuid/{2}")
    @MethodSource("serverPageElementVisibleCases")
    void serverPageElementVisible(WebPermission permission, String element, String section, Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_SERVER, permission);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/server/" + serverUUID + "/" + section;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.id(element), driver);
        assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address + " with permission '" + permission.getPermission() + "'");
        assertNoLogs(driver, address);
    }

    @DisplayName("Server element is not visible without permission")
    @ParameterizedTest(name = "Access to server page with no visibility needs {0} can't see element #{1} in section /server/uuid/{2}")
    @MethodSource("serverPageElementVisibleCases")
    void serverPageElementNotVisible(WebPermission permission, String element, String section, Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_SERVER);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/server/" + serverUUID + "/" + section;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.className("login-username"), driver);
        By id = By.id(element);
        assertThrows(NoSuchElementException.class, () -> driver.findElement(id), () -> "Saw element #" + element + " at " + address + " without permission to");
        assertNoLogs(driver, address);
    }

    private void registerProxy(Database database) throws ExecutionException, InterruptedException {
        database.executeTransaction(new StoreServerInformationTransaction(
                new Server(null, TestConstants.SERVER_TWO_UUID, "Proxy", "https://localhost", true, TestConstants.VERSION)
        )).get();
        Awaitility.await("Proxy was not registered")
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> !database.query(ServerQueries.fetchProxyServers()).isEmpty());
    }

    @DisplayName("Network element is visible with permission")
    @ParameterizedTest(name = "Access to network page with visibility {0} can see element #{1} in section /network/{2}")
    @MethodSource("networkPageElementVisibleCases")
    void networkPageElementVisible(WebPermission permission, String element, String section, Database database, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_NETWORK, permission);
        registerProxy(database);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/network/" + section;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.id(element), driver);
        assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address + " with permission '" + permission.getPermission() + "'");
        assertNoLogs(driver, address);
    }

    @DisplayName("Network element is not visible without permission")
    @ParameterizedTest(name = "Access to network page with no visibility needs {0} can't see element #{1} in section /network/{2}")
    @MethodSource("networkPageElementVisibleCases")
    void networkPageElementNotVisible(WebPermission permission, String element, String section, Database database, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_NETWORK);
        registerProxy(database);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/network/" + section;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.className("login-username"), driver);
        By id = By.id(element);
        assertThrows(NoSuchElementException.class, () -> driver.findElement(id), () -> "Saw element #" + element + " at " + address + " without permission to");
        assertNoLogs(driver, address);
    }

    @DisplayName("Player element is visible with permission")
    @ParameterizedTest(name = "Access to player page with visibility {0} can see element #{1} in section /player/uuid/{2}")
    @MethodSource("playerPageVisibleCases")
    void playerPageElementVisible(WebPermission permission, String element, String section, Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_PLAYER, permission);
        storePlayer(database, serverUUID);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID + "/" + section;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.id(element), driver);
        assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at " + address + " with permission '" + permission.getPermission() + "'");
        assertNoLogs(driver, address);
    }

    @DisplayName("Player element is not visible without permission")
    @ParameterizedTest(name = "Access to player page with no visibility needs {0} can't see element #{1} in section /player/uuid/{2}")
    @MethodSource("playerPageVisibleCases")
    void playerPageElementNotVisible(WebPermission permission, String element, String section, Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        User user = registerUser(database, WebPermission.ACCESS_PLAYER);
        storePlayer(database, serverUUID);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID + "/" + section;
        driver.get(address);
        login(driver, user);

        SeleniumExtension.waitForElementToBeVisible(By.className("login-username"), driver);
        By id = By.id(element);
        assertThrows(NoSuchElementException.class, () -> driver.findElement(id), () -> "Saw element #" + element + " at " + address + " without permission to");
        assertNoLogs(driver, address);
    }

    @Test
    @DisplayName("ACCESS_PLAYER_SELF can see own player page")
    void playerSelfVisibilityTests(Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        String element = "player-overview";

        User user = registerUser(database, WebPermission.ACCESS_PLAYER_SELF, WebPermission.PAGE_PLAYER);
        // Link a user to the player
        User playerUser = new User("player_user", TestConstants.PLAYER_ONE_NAME, TestConstants.PLAYER_ONE_UUID, PassEncryptUtil.createHash(PASSWORD), user.getPermissionGroup(), user.getPermissions());
        database.executeTransaction(new StoreWebUserTransaction(playerUser)).get();

        storePlayer(database, serverUUID, TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
        storePlayer(database, serverUUID, TestConstants.PLAYER_TWO_UUID, TestConstants.PLAYER_TWO_NAME);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_ONE_UUID;
        driver.get(address);
        login(driver, playerUser);

        SeleniumExtension.waitForElementToBeVisible(By.id(element), driver);
        assertDoesNotThrow(() -> driver.findElement(By.id(element)), () -> "Did not see #" + element + " at /player/" + TestConstants.PLAYER_ONE_UUID + " with permission '" + WebPermission.ACCESS_PLAYER_SELF.getPermission() + "'");
        assertNoLogs(driver, address);
    }


    @Test
    @DisplayName("ACCESS_PLAYER_SELF can not see other player's page")
    void playerSelfNonVisibilityTests(Database database, ServerUUID serverUUID, ChromeDriver driver) throws Exception {
        String element = "player-overview";

        User user = registerUser(database, WebPermission.ACCESS_PLAYER_SELF, WebPermission.PAGE_PLAYER);
        // Link a user to the player
        User playerUser = new User("player_user", TestConstants.PLAYER_ONE_NAME, TestConstants.PLAYER_ONE_UUID, PassEncryptUtil.createHash(PASSWORD), user.getPermissionGroup(), user.getPermissions());
        database.executeTransaction(new StoreWebUserTransaction(playerUser));

        storePlayer(database, serverUUID, TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);
        storePlayer(database, serverUUID, TestConstants.PLAYER_TWO_UUID, TestConstants.PLAYER_TWO_NAME);

        String address = "https://localhost:" + TEST_PORT_NUMBER + "/player/" + TestConstants.PLAYER_TWO_UUID;
        driver.get(address);
        login(driver, playerUser);

        SeleniumExtension.waitForElementToBeVisible(By.className("login-username"), driver);
        By id = By.id(element);
        assertThrows(NoSuchElementException.class, () -> driver.findElement(id), () -> "Saw element #" + element + " at /player/" + TestConstants.PLAYER_TWO_UUID + " without permission to");
    }
}
