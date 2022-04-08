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
import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.changes.ConfigUpdater;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.queries.ExtensionsDatabaseTest;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RegisterWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for limiting user access control based on permissions.
 */
class AccessControlTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private static final HTTPConnector CONNECTOR = new HTTPConnector();

    private static PlanSystem system;
    private static String address;
    private static String cookieLevel0;
    private static String cookieLevel1;
    private static String cookieLevel2;
    private static ServerUUID serverUUID;
    private static String cookieLevel100;

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

        User userLevel0 = new User("test0", "console", null, PassEncryptUtil.createHash("testPass"), 0, Collections.emptyList());
        User userLevel1 = new User("test1", "console", null, PassEncryptUtil.createHash("testPass"), 1, Collections.emptyList());
        User userLevel2 = new User("test2", TestConstants.PLAYER_ONE_NAME, TestConstants.PLAYER_ONE_UUID, PassEncryptUtil.createHash("testPass"), 2, Collections.emptyList());
        User userLevel100 = new User("test100", "console", null, PassEncryptUtil.createHash("testPass"), 100, Collections.emptyList());
        system.getDatabaseSystem().getDatabase().executeTransaction(new RegisterWebUserTransaction(userLevel0));
        system.getDatabaseSystem().getDatabase().executeTransaction(new RegisterWebUserTransaction(userLevel1));
        system.getDatabaseSystem().getDatabase().executeTransaction(new RegisterWebUserTransaction(userLevel2));
        system.getDatabaseSystem().getDatabase().executeTransaction(new RegisterWebUserTransaction(userLevel100));

        system.getDatabaseSystem().getDatabase().executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, () -> 0L, TestConstants.PLAYER_ONE_NAME));
        system.getDatabaseSystem().getDatabase().executeTransaction(new StoreServerInformationTransaction(new Server(
                TestConstants.SERVER_UUID,
                TestConstants.SERVER_NAME,
                address,
                TestConstants.VERSION)));

        Caller caller = system.getExtensionService().register(new ExtensionsDatabaseTest.PlayerExtension())
                .orElseThrow(AssertionError::new);
        caller.updatePlayerData(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);

        address = "https://localhost:" + TEST_PORT_NUMBER;
        cookieLevel0 = login(address, userLevel0.getUsername());
        cookieLevel1 = login(address, userLevel1.getUsername());
        cookieLevel2 = login(address, userLevel2.getUsername());
        cookieLevel100 = login(address, userLevel100.getUsername());
    }

    @AfterAll
    static void tearDownClass() {
        if (system != null) {
            system.disable();
        }
    }

    static String login(String address, String username) throws IOException, KeyManagementException, NoSuchAlgorithmException {
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

    @DisplayName("Access control test, level 0:")
    @ParameterizedTest(name = "{0}: expecting {1}")
    @CsvSource({
            "/,302",
            "/server,302",
            "/server/" + TestConstants.SERVER_UUID_STRING + ",200",
            "/css/style.css,200",
            "/js/color-selector.js,200",
            "/v1/serverOverview?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/onlineOverview?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/sessionsOverview?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/playerVersus?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/playerbaseOverview?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/performanceOverview?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=optimizedPerformance&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=aggregatedPing&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=worldPie&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=activity&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=joinAddressPie&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=geolocation&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=uniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=hourlyUniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=serverCalendar&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=punchCard&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/players?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/kills?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/pingTable?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/sessions?server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/network,302",
            "/v1/network/overview,200",
            "/v1/network/servers,200",
            "/v1/network/sessionsOverview,200",
            "/v1/network/playerbaseOverview,200",
            "/v1/sessions,200",
            "/v1/graph?type=playersOnline&server=" + TestConstants.SERVER_UUID_STRING + ",200",
            "/v1/graph?type=uniqueAndNew,200",
            "/v1/graph?type=hourlyUniqueAndNew,200",
            "/v1/graph?type=serverPie,200",
            "/v1/graph?type=joinAddressPie,200",
            "/v1/graph?type=activity,200",
            "/v1/graph?type=geolocation,200",
            "/v1/network/pingTable,200",
            "/player/" + TestConstants.PLAYER_ONE_NAME + ",200",
            "/player/" + TestConstants.PLAYER_TWO_NAME + ",404",
            "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + ",200",
            "/player/" + TestConstants.PLAYER_TWO_UUID_STRING + ",404",
            "/v1/player?player=" + TestConstants.PLAYER_ONE_NAME + ",200",
            "/v1/player?player=" + TestConstants.PLAYER_TWO_NAME + ",400",
            "/players,200",
            "/v1/players,200",
            "/query,200",
            "/v1/filters,200",
            "/v1/query,400",
            "/v1/errors,200",
            "/errors,200",
            "/v1/network/listServers,200",
            "/v1/network/serverOptions,200",
            "/v1/network/performanceOverview?servers=[" + TestConstants.SERVER_UUID_STRING + "],200",
            "/v1/version,200",
            "/v1/whoami,200",
            "/v1/metadata,200",
            "/v1/locale,200",
            "/v1/locale/EN,200",
    })
    void levelZeroCanAccess(String resource, String expectedResponseCode) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        int responseCode = access(resource, cookieLevel0);
        assertEquals(Integer.parseInt(expectedResponseCode), responseCode, () -> "User level 0, Wrong response code for " + resource + ", expected " + expectedResponseCode + " but was " + responseCode);
    }

    @DisplayName("Access control test, level 1:")
    @ParameterizedTest(name = "{0}: expecting {1}")
    @CsvSource({
            "/,302",
            "/server,403",
            "/server/" + TestConstants.SERVER_UUID_STRING + ",403",
            "/css/style.css,200",
            "/js/color-selector.js,200",
            "/v1/serverOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/onlineOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/sessionsOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/playerVersus?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/playerbaseOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/performanceOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=optimizedPerformance&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=aggregatedPing&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=worldPie&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=activity&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=joinAddressPie&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=geolocation&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=uniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=hourlyUniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=serverCalendar&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=punchCard&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/players?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/kills?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/pingTable?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/sessions?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/network,403",
            "/v1/network/overview,403",
            "/v1/network/servers,403",
            "/v1/network/sessionsOverview,403",
            "/v1/network/playerbaseOverview,403",
            "/v1/sessions,403",
            "/v1/graph?type=playersOnline&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=uniqueAndNew,403",
            "/v1/graph?type=hourlyUniqueAndNew,403",
            "/v1/graph?type=serverPie,403",
            "/v1/graph?type=joinAddressPie,403",
            "/v1/graph?type=activity,403",
            "/v1/graph?type=geolocation,403",
            "/v1/network/pingTable,403",
            "/player/" + TestConstants.PLAYER_ONE_NAME + ",200",
            "/player/" + TestConstants.PLAYER_TWO_NAME + ",404",
            "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + ",200",
            "/player/" + TestConstants.PLAYER_TWO_UUID_STRING + ",404",
            "/v1/player?player=" + TestConstants.PLAYER_ONE_NAME + ",200",
            "/v1/player?player=" + TestConstants.PLAYER_TWO_NAME + ",400",
            "/players,200",
            "/v1/players,200",
            "/query,200",
            "/v1/filters,200",
            "/v1/query,400",
            "/v1/errors,403",
            "/errors,403",
            "/v1/network/listServers,403",
            "/v1/network/serverOptions,403",
            "/v1/network/performanceOverview?servers=[" + TestConstants.SERVER_UUID_STRING + "],403",
            "/v1/version,200",
            "/v1/whoami,200",
            "/v1/metadata,200",
            "/v1/locale,200",
            "/v1/locale/EN,200",
    })
    void levelOneCanAccess(String resource, String expectedResponseCode) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        int responseCode = access(resource, cookieLevel1);
        assertEquals(Integer.parseInt(expectedResponseCode), responseCode, () -> "User level 1, Wrong response code for " + resource + ", expected " + expectedResponseCode + " but was " + responseCode);
    }

    @DisplayName("Access control test, level 2:")
    @ParameterizedTest(name = "{0}: expecting {1}")
    @CsvSource({
            "/,302",
            "/server,403",
            "/server/" + TestConstants.SERVER_UUID_STRING + ",403",
            "/css/style.css,200",
            "/js/color-selector.js,200",
            "/v1/serverOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/onlineOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/sessionsOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/playerVersus?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/playerbaseOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/performanceOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=optimizedPerformance&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=aggregatedPing&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=worldPie&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=activity&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=joinAddressPie&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=geolocation&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=uniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=hourlyUniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=serverCalendar&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=punchCard&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/players?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/kills?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/pingTable?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/sessions?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/network,403",
            "/v1/network/overview,403",
            "/v1/network/servers,403",
            "/v1/network/sessionsOverview,403",
            "/v1/network/playerbaseOverview,403",
            "/v1/sessions,403",
            "/v1/graph?type=playersOnline&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=uniqueAndNew,403",
            "/v1/graph?type=hourlyUniqueAndNew,403",
            "/v1/graph?type=serverPie,403",
            "/v1/graph?type=joinAddressPie,403",
            "/v1/graph?type=activity,403",
            "/v1/graph?type=geolocation,403",
            "/v1/network/pingTable,403",
            "/player/" + TestConstants.PLAYER_ONE_NAME + ",200",
            "/player/" + TestConstants.PLAYER_TWO_NAME + ",403",
            "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + ",200",
            "/player/" + TestConstants.PLAYER_TWO_UUID_STRING + ",403",
            "/v1/player?player=" + TestConstants.PLAYER_ONE_NAME + ",200",
            "/v1/player?player=" + TestConstants.PLAYER_TWO_NAME + ",403",
            "/players,403",
            "/v1/players,403",
            "/query,403",
            "/v1/filters,403",
            "/v1/query,403",
            "/v1/errors,403",
            "/errors,403",
            "/v1/network/listServers,403",
            "/v1/network/serverOptions,403",
            "/v1/network/performanceOverview?servers=[" + TestConstants.SERVER_UUID_STRING + "],403",
            "/v1/version,200",
            "/v1/whoami,200",
            "/v1/metadata,200",
            "/v1/locale,200",
            "/v1/locale/EN,200",
    })
    void levelTwoCanAccess(String resource, String expectedResponseCode) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        int responseCode = access(resource, cookieLevel2);
        assertEquals(Integer.parseInt(expectedResponseCode), responseCode, () -> "User level 2, Wrong response code for " + resource + ", expected " + expectedResponseCode + " but was " + responseCode);
    }

    @DisplayName("Access control test, level 100:")
    @ParameterizedTest(name = "{0}: expecting {1}")
    @CsvSource({
            "/,403",
            "/server,403",
            "/server/" + TestConstants.SERVER_UUID_STRING + ",403",
            "/css/style.css,200",
            "/js/color-selector.js,200",
            "/v1/serverOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/onlineOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/sessionsOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/playerVersus?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/playerbaseOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/performanceOverview?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=optimizedPerformance&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=aggregatedPing&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=worldPie&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=activity&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=joinAddressPie&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=geolocation&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=uniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=hourlyUniqueAndNew&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=serverCalendar&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=punchCard&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/players?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/kills?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/pingTable?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/sessions?server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/network,403",
            "/v1/network/overview,403",
            "/v1/network/servers,403",
            "/v1/network/sessionsOverview,403",
            "/v1/network/playerbaseOverview,403",
            "/v1/sessions,403",
            "/v1/graph?type=playersOnline&server=" + TestConstants.SERVER_UUID_STRING + ",403",
            "/v1/graph?type=uniqueAndNew,403",
            "/v1/graph?type=hourlyUniqueAndNew,403",
            "/v1/graph?type=serverPie,403",
            "/v1/graph?type=joinAddressPie,403",
            "/v1/graph?type=activity,403",
            "/v1/graph?type=geolocation,403",
            "/v1/network/pingTable,403",
            "/player/" + TestConstants.PLAYER_ONE_NAME + ",403",
            "/player/" + TestConstants.PLAYER_TWO_NAME + ",403",
            "/player/" + TestConstants.PLAYER_ONE_UUID_STRING + ",403",
            "/player/" + TestConstants.PLAYER_TWO_UUID_STRING + ",403",
            "/v1/player?player=" + TestConstants.PLAYER_ONE_NAME + ",403",
            "/v1/player?player=" + TestConstants.PLAYER_TWO_NAME + ",403",
            "/players,403",
            "/v1/players,403",
            "/query,403",
            "/v1/filters,403",
            "/v1/query,403",
            "/v1/network/listServers,403",
            "/v1/network/serverOptions,403",
            "/v1/network/performanceOverview?servers=[" + TestConstants.SERVER_UUID_STRING + "],403",
            "/v1/version,200",
            "/v1/whoami,200",
            "/v1/metadata,200",
            "/v1/locale,200",
            "/v1/locale/EN,200",
    })
    void levelHundredCanNotAccess(String resource, String expectedResponseCode) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        int responseCode = access(resource, cookieLevel100);
        assertEquals(Integer.parseInt(expectedResponseCode), responseCode, () -> "User level 100, Wrong response code for " + resource + ", expected " + expectedResponseCode + " but was " + responseCode);
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
