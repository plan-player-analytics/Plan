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
import com.djrapitops.plan.extension.Caller;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.ExtensionsDatabaseTest;
import com.djrapitops.plan.storage.database.transactions.StoreServerInformationTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utilities.HTTPConnector;
import utilities.RandomData;
import utilities.TestConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test against endpoints that should not be visible without authentication.
 *
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class HttpAccessControlTest {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    private static final String ADDRESS = "http://localhost:" + TEST_PORT_NUMBER;

    private static final HTTPConnector CONNECTOR = new HTTPConnector();

    @BeforeAll
    static void setUp(PlanSystem system) {
        system.getConfigSystem().getConfig().set(WebserverSettings.PORT, TEST_PORT_NUMBER);
        system.enable();

        Database database = system.getDatabaseSystem().getDatabase();

        database.executeTransaction(new PlayerRegisterTransaction(TestConstants.PLAYER_ONE_UUID, () -> 0L, TestConstants.PLAYER_ONE_NAME));
        database.executeTransaction(new StoreServerInformationTransaction(new Server(
                TestConstants.SERVER_UUID,
                TestConstants.SERVER_NAME,
                ADDRESS,
                TestConstants.VERSION)));

        Caller caller = system.getApiServices().getExtensionService().register(new ExtensionsDatabaseTest.PlayerExtension())
                .orElseThrow(AssertionError::new);
        caller.updatePlayerData(TestConstants.PLAYER_ONE_UUID, TestConstants.PLAYER_ONE_NAME);

        assertFalse(system.getWebServerSystem().getWebServer().isAuthRequired());
    }

    @AfterAll
    static void afterAll(PlanSystem system) {
        system.disable();
    }

    @DisplayName("Endpoints disabled without authentication")
    @ParameterizedTest(name = "Endpoint {0} is disabled without authentication")
    @CsvSource({
            "/v1/webGroups",
            "/v1/groupPermissions?group=admin",
            "/v1/permissions",
            "/v1/saveGroupPermissions",
            "/v1/deleteGroup",
            "/v1/storePreferences",
            "/v1/saveTheme",
            "/v1/pluginHistory?server=" + TestConstants.SERVER_UUID_STRING,
            "/manage",
            "/auth/register",
            "/auth/login",
            "/auth/logout",
            "/login",
            "/register"
    })
    void endpointNotEnabled(String endpoint) throws Exception {
        int code = access(endpoint);
        assertEquals(404, code);
    }


    private int access(String resource) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        try {
            connection = CONNECTOR.getConnection("GET", ADDRESS + resource);

            return connection.getResponseCode();

        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
