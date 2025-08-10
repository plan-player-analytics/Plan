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
import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.ExtensionsDatabaseTest;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.StoreWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreWebGroupTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import utilities.HTTPConnector;
import utilities.RandomData;
import utilities.TestConstants;
import utilities.TestResources;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for limiting user access control based on permissions.
 */
class AccessControlTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    private static final String QUERY_VIEW_SIMPLE = "%7B%22afterDate%22%3A%2201%2F01%2F1970%22%2C%22afterTime%22%3A%2200%3A00%22%2C%22beforeDate%22%3A%2201%2F01%2F2024%22%2C%22beforeTime%22%3A%2200%3A00%22%2C%22servers%22%3A%5B%5D%7D";

    private static final HTTPConnector CONNECTOR = new HTTPConnector();

    private static PlanSystem system;
    private static String address;
    private static String cookieNoAccess;


    static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of("/", WebPermission.ACCESS, 302, 403),
                Arguments.of("/pageExtensionApi.js", WebPermission.ACCESS, 200, 200),
                Arguments.of("/server", WebPermission.ACCESS_SERVER, 302, 403),
                Arguments.of("/server/" + TestConstants.SERVER_UUID_STRING + "", WebPermission.ACCESS_SERVER, 200, 403),
                Arguments.of("/v1/serverOverview?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_OVERVIEW_NUMBERS, 200, 403),
                Arguments.of("/v1/onlineOverview?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_OVERVIEW, 200, 403),
                Arguments.of("/v1/sessionsOverview?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_SESSIONS, 200, 403),
                Arguments.of("/v1/playerVersus?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PLAYER_VERSUS, 200, 403),
                Arguments.of("/v1/playerbaseOverview?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PLAYERBASE_OVERVIEW, 200, 403),
                Arguments.of("/v1/performanceOverview?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PERFORMANCE_OVERVIEW, 200, 403),
                Arguments.of("/v1/graph?type=optimizedPerformance&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS, 200, 403),
                Arguments.of("/v1/graph?type=aggregatedPing&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PERFORMANCE_GRAPHS, 200, 403),
                Arguments.of("/v1/graph?type=worldPie&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_SESSIONS_WORLD_PIE, 200, 403),
                Arguments.of("/v1/graph?type=activity&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PLAYERBASE_GRAPHS, 200, 403),
                Arguments.of("/v1/graph?type=geolocation&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_GEOLOCATIONS_MAP, 200, 403),
                Arguments.of("/v1/graph?type=uniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_DAY_BY_DAY, 200, 403),
                Arguments.of("/v1/graph?type=hourlyUniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_HOUR_BY_HOUR, 200, 403),
                Arguments.of("/v1/graph?type=serverCalendar&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR, 200, 403),
                Arguments.of("/v1/graph?type=punchCard&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_PUNCHCARD, 200, 403),
                Arguments.of("/v1/graph?type=joinAddressByDay&server=" + TestConstants.SERVER_UUID_STRING + "&after=0&before=" + 123456L + "", WebPermission.PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_TIME, 200, 403),
                Arguments.of("/v1/players?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PLAYERS, 200, 403),
                Arguments.of("/v1/kills?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_PLAYER_VERSUS_KILL_LIST, 200, 403),
                Arguments.of("/v1/pingTable?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_GEOLOCATIONS_PING_PER_COUNTRY, 200, 403),
                Arguments.of("/v1/sessions?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_SESSIONS_LIST, 200, 403),
                Arguments.of("/v1/retention?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_RETENTION, 200, 403),
                Arguments.of("/v1/joinAddresses", WebPermission.PAGE_NETWORK_RETENTION, 200, 403),
                Arguments.of("/v1/joinAddresses?listOnly=true", WebPermission.PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_TIME, 200, 403),
                Arguments.of("/v1/joinAddresses?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_RETENTION, 200, 403),
                Arguments.of("/v1/joinAddresses?server=" + TestConstants.SERVER_UUID_STRING + "&listOnly=true", WebPermission.PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_TIME, 200, 403),
                Arguments.of("/network", WebPermission.ACCESS_NETWORK, 302, 403),
                Arguments.of("/v1/network/overview", WebPermission.PAGE_NETWORK_OVERVIEW_NUMBERS, 200, 403),
                Arguments.of("/v1/network/servers", WebPermission.PAGE_NETWORK_SERVER_LIST, 200, 403),
                Arguments.of("/v1/network/sessionsOverview", WebPermission.PAGE_NETWORK_SESSIONS_OVERVIEW, 200, 403),
                Arguments.of("/v1/network/playerbaseOverview", WebPermission.PAGE_NETWORK_PLAYERBASE_OVERVIEW, 200, 403),
                Arguments.of("/v1/sessions", WebPermission.PAGE_NETWORK_SESSIONS_LIST, 200, 403),
                Arguments.of("/v1/graph?type=playersOnline&server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH, 200, 403),
                Arguments.of("/v1/graph?type=uniqueAndNew", WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_DAY_BY_DAY, 200, 403),
                Arguments.of("/v1/graph?type=hourlyUniqueAndNew", WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_HOUR_BY_HOUR, 200, 403),
                Arguments.of("/v1/graph?type=serverCalendar", WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR, 200, 403),
                Arguments.of("/v1/graph?type=serverPie", WebPermission.PAGE_NETWORK_SESSIONS_SERVER_PIE, 200, 403),
                Arguments.of("/v1/graph?type=activity", WebPermission.PAGE_NETWORK_PLAYERBASE_GRAPHS, 200, 403),
                Arguments.of("/v1/graph?type=geolocation", WebPermission.PAGE_NETWORK_GEOLOCATIONS_MAP, 200, 403),
                Arguments.of("/v1/network/pingTable", WebPermission.PAGE_NETWORK_GEOLOCATIONS_PING_PER_COUNTRY, 200, 403),
                Arguments.of("/player/" + TestConstants.PLAYER_ONE_NAME + "", WebPermission.ACCESS_PLAYER, 200, 403),
                Arguments.of("/player/" + TestConstants.PLAYER_TWO_NAME + "", WebPermission.ACCESS_PLAYER, 404, 403),
                Arguments.of("/player/" + TestConstants.PLAYER_ONE_UUID_STRING + "", WebPermission.ACCESS_PLAYER, 200, 403),
                Arguments.of("/player/" + TestConstants.PLAYER_TWO_UUID_STRING + "", WebPermission.ACCESS_PLAYER, 404, 403),
                Arguments.of("/v1/player?player=" + TestConstants.PLAYER_ONE_NAME + "", WebPermission.ACCESS_PLAYER, 200, 403),
                Arguments.of("/v1/player?player=" + TestConstants.PLAYER_TWO_NAME + "", WebPermission.ACCESS_PLAYER, 400, 403),
                Arguments.of("/players", WebPermission.ACCESS_PLAYERS, 200, 403),
                Arguments.of("/v1/players", WebPermission.ACCESS_PLAYERS, 200, 403),
                Arguments.of("/query", WebPermission.ACCESS_QUERY, 200, 403),
                Arguments.of("/v1/filters", WebPermission.ACCESS_QUERY, 200, 403),
                Arguments.of("/v1/query", WebPermission.ACCESS_QUERY, 400, 403),
                Arguments.of("/v1/query?q=%5B%5D&view=%7B%22afterDate%22%3A%2224%2F10%2F2022%22%2C%22afterTime%22%3A%2218%3A21%22%2C%22beforeDate%22%3A%2223%2F11%2F2022%22%2C%22beforeTime%22%3A%2217%3A21%22%2C%22servers%22%3A%5B%0A%7B%22serverUUID%22%3A%22" + TestConstants.SERVER_UUID_STRING + "%22%2C%22serverName%22%3A%22" + TestConstants.SERVER_NAME + "%22%2C%22proxy%22%3Afalse%7D%5D%7D", WebPermission.ACCESS_QUERY, 200, 403),
                Arguments.of("/v1/query?q=%5B%7B%22kind%22%3A%22playedBetween%22%2C%22parameters%22%3A%7B%22afterDate%22%3A%2201%2F01%2F1970%22%2C%22afterTime%22%3A%2200%3A00%22%2C%22beforeDate%22%3A%2201%2F01%2F2024%22%2C%22beforeTime%22%3A%2200%3A00%22%7D%7D%5D&view=" + QUERY_VIEW_SIMPLE, WebPermission.PAGE_NETWORK_OVERVIEW_GRAPHS_CALENDAR, 200, 403),
                Arguments.of("/v1/query?q=%5B%7B%22kind%22%3A%22playedBetween%22%2C%22parameters%22%3A%7B%22afterDate%22%3A%2201%2F01%2F1970%22%2C%22afterTime%22%3A%2200%3A00%22%2C%22beforeDate%22%3A%2201%2F01%2F2024%22%2C%22beforeTime%22%3A%2200%3A00%22%7D%7D%5D&view=" + QUERY_VIEW_SIMPLE, WebPermission.PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR, 200, 403),
                Arguments.of("/v1/query?q=%5B%7B%22kind%22%3A%22geolocations%22%2C%22parameters%22%3A%7B%22selected%22%3A%22%5B%5C%22FIN%5C%22%5D%22%7D%7D%5D&view=" + QUERY_VIEW_SIMPLE, WebPermission.PAGE_NETWORK_GEOLOCATIONS_MAP, 200, 403),
                Arguments.of("/v1/query?q=%5B%7B%22kind%22%3A%22geolocations%22%2C%22parameters%22%3A%7B%22selected%22%3A%22%5B%5C%22FIN%5C%22%5D%22%7D%7D%5D&view=" + QUERY_VIEW_SIMPLE, WebPermission.PAGE_SERVER_GEOLOCATIONS_MAP, 200, 403),
                Arguments.of("/v1/errors", WebPermission.ACCESS_ERRORS, 200, 403),
                Arguments.of("/errors", WebPermission.ACCESS_ERRORS, 200, 403),
                Arguments.of("/v1/network/listServers", WebPermission.PAGE_NETWORK_PERFORMANCE, 200, 403),
                Arguments.of("/v1/network/serverOptions", WebPermission.PAGE_NETWORK_PERFORMANCE, 200, 403),
                Arguments.of("/v1/network/performanceOverview?servers=[" + TestConstants.SERVER_UUID_STRING + "]", WebPermission.PAGE_NETWORK_PERFORMANCE, 200, 403),
                Arguments.of("/v1/version", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/whoami", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/metadata", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/networkMetadata", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/serverIdentity?server=" + TestConstants.SERVER_UUID_STRING + "", WebPermission.ACCESS_SERVER, 200, 403),
                Arguments.of("/v1/locale", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/locale/EN", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/locale/NonexistingLanguage", WebPermission.ACCESS, 404, 404),
                Arguments.of("/docs/swagger.json", WebPermission.ACCESS_DOCS, 200, 403),
                Arguments.of("/docs", WebPermission.ACCESS_DOCS, 200, 403),
                Arguments.of("/pageExtensionApi.js", WebPermission.ACCESS, 200, 200),
                Arguments.of("/manage", WebPermission.MANAGE_GROUPS, 200, 403),
                Arguments.of("/v1/groupPermissions?group=admin", WebPermission.MANAGE_GROUPS, 200, 403),
                Arguments.of("/v1/webGroups", WebPermission.MANAGE_GROUPS, 200, 403),
                Arguments.of("/v1/deleteGroup?group=admin&moveTo=no_access", WebPermission.MANAGE_GROUPS, 405, 403),
                Arguments.of("/v1/saveGroupPermissions?group=admin", WebPermission.MANAGE_GROUPS, 405, 403),
                Arguments.of("/v1/preferences", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/storePreferences", WebPermission.ACCESS, 405, 405),
                Arguments.of("/v1/pluginHistory?server=" + TestConstants.SERVER_UUID_STRING, WebPermission.PAGE_NETWORK_PLUGIN_HISTORY, 200, 403),
                Arguments.of("/v1/pluginHistory?server=" + TestConstants.SERVER_UUID_STRING, WebPermission.PAGE_SERVER_PLUGIN_HISTORY, 200, 403),
                Arguments.of("/v1/gameAllowlistBounces?server=" + TestConstants.SERVER_UUID_STRING, WebPermission.PAGE_SERVER_ALLOWLIST_BOUNCE, 200, 403),
                Arguments.of("/v1/theme?theme=default", WebPermission.ACCESS, 200, 200),
                Arguments.of("/v1/saveTheme?theme=default", WebPermission.MANAGE_THEMES, 405, 403),
                Arguments.of("/v1/deleteTheme?theme=default", WebPermission.MANAGE_THEMES, 405, 403)
        );
    }

    @BeforeAll
    static void setUpClass(@TempDir Path tempDir) throws Exception {
        File file = tempDir.resolve("TestCert.p12").toFile();
        File testCert = TestResources.getTestResourceFile("TestCert.p12", ConfigUpdater.class);
        Files.copy(testCert.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String absolutePath = file.getAbsolutePath();

        PluginMockComponent component = new PluginMockComponent(tempDir);
        system = component.getPlanSystem();

        PlanConfig config = system.getConfigSystem().getConfig();

        config.set(WebserverSettings.CERTIFICATE_PATH, absolutePath);
        config.set(WebserverSettings.CERTIFICATE_KEYPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_STOREPASS, "test");
        config.set(WebserverSettings.CERTIFICATE_ALIAS, "test");

        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        system.enable();

        User userNoAccess = new User("test0", "console", null, PassEncryptUtil.createHash("testPass"), "no_access", Collections.emptyList());

        Database database = system.getDatabaseSystem().getDatabase();
        database.executeTransaction(new StoreWebUserTransaction(userNoAccess));

        database.executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, () -> 0L, TestConstants.PLAYER_ONE_NAME));
        database.executeTransaction(new StoreServerInformationTransaction(new Server(
                TestConstants.SERVER_UUID,
                TestConstants.SERVER_NAME,
                address,
                TestConstants.VERSION)));

        Caller caller = system.getApiServices().getExtensionService().register(new ExtensionsDatabaseTest.PlayerExtension())
                .orElseThrow(AssertionError::new);
        caller.updatePlayerData(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);

        assertTrue(system.getWebServerSystem().getWebServer().isUsingHTTPS());
        assertTrue(system.getWebServerSystem().getWebServer().isAuthRequired());

        address = "https://localhost:" + TEST_PORT_NUMBER;
        cookieNoAccess = login(address, userNoAccess.getUsername());
    }

    @AfterAll
    static void tearDownClass() {
        if (system != null) {
            system.disable();
        }
    }

    public static String login(String address, String username) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection loginConnection = null;
        String cookie;
        try {
            loginConnection = CONNECTOR.getConnection("POST", address + "/auth/login");
            loginConnection.setDoOutput(true);
            loginConnection.getOutputStream().write(("user=" + username + "&password=testPass").getBytes());
            try (InputStream in = loginConnection.getInputStream()) {
                String responseBody = new String(IOUtils.toByteArray(in));
                assertTrue(responseBody.contains("\"success\":true"), () -> "Not successful: " + responseBody);
                cookie = loginConnection.getHeaderField("Set-Cookie").split(";")[0];
                System.out.println("Got cookie: " + cookie);
            }
        } finally {
            if (loginConnection != null) loginConnection.disconnect();
        }
        return cookie;
    }

    @DisplayName("Access control test")
    @ParameterizedTest(name = "{0}: Permission {1}, expecting {2} with & {3} without permission")
    @MethodSource("testCases")
    void accessControlTest(String resource, WebPermission permission, int expectedWithPermission, int expectedWithout) throws Exception {
        String cookie = login(address, createUserWithPermissions(resource, permission).getUsername());
        int responseCodeWithPermission = access(resource, cookie);
        int responseCodeWithout = access(resource, cookieNoAccess);

        assertAll(
                () -> assertEquals(expectedWithPermission, responseCodeWithPermission,
                        () -> "Permission '" + permission.getPermission() + "', Wrong response code for " + resource + ", expected " + expectedWithPermission + " but was " + responseCodeWithPermission),
                () -> assertEquals(expectedWithout, responseCodeWithout,
                        () -> "No Permissions, Wrong response code for " + resource + ", expected " + expectedWithout + " but was " + responseCodeWithout)
        );
    }

    @DisplayName("Access test player/uuid/raw")
    @Test
    void playerRawAccess() throws Exception {
        String resource = "/player/" + TestConstants.PLAYER_ONE_UUID + "/raw";
        int expectedWithPermission = 200;
        int expectedWithout = 403;
        String cookie = login(address, createUserWithPermissions(resource, WebPermission.ACCESS_PLAYER, WebPermission.ACCESS_RAW_PLAYER_DATA).getUsername());
        String cookieJustPage = login(address, createUserWithPermissions(resource, WebPermission.ACCESS_PLAYER).getUsername());
        int responseCodeWithPermission = access(resource, cookie);
        int responseCodeJustPage = access(resource, cookieJustPage);
        int responseCodeWithout = access(resource, cookieNoAccess);

        assertAll(
                () -> assertEquals(expectedWithPermission, responseCodeWithPermission,
                        () -> "Permission 'access.player', 'access.raw.player.data', Wrong response code for " + resource + ", expected " + expectedWithPermission + " but was " + responseCodeWithPermission),
                () -> assertEquals(expectedWithout, responseCodeJustPage,
                        () -> "Just page visibility permissions, Wrong response code for " + resource + ", expected " + expectedWithout + " but was " + responseCodeJustPage),
                () -> assertEquals(expectedWithout, responseCodeWithout,
                        () -> "No Permissions, Wrong response code for " + resource + ", expected " + expectedWithout + " but was " + responseCodeWithout)
        );
    }

    private User createUserWithPermissions(String resource, WebPermission... permissions) throws ExecutionException, InterruptedException {
        Database db = system.getDatabaseSystem().getDatabase();

        String groupName = StringUtils.truncate(resource, 75);
        db.executeTransaction(
                new StoreWebGroupTransaction(groupName, Arrays.stream(permissions).map(WebPermission::getPermission).collect(Collectors.toList()))
        ).get();

        User user = new User(RandomData.randomString(45), "console", null, PassEncryptUtil.createHash("testPass"), groupName, Collections.emptyList());
        db.executeTransaction(new StoreWebUserTransaction(user)).get();

        return user;
    }

    private int access(String resource, String cookie) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        try {
            connection = CONNECTOR.getConnection("GET", address + resource);
            connection.setRequestProperty("Cookie", cookie);

            return connection.getResponseCode();

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

}
