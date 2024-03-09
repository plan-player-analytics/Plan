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

import com.djrapitops.plan.delivery.webserver.http.WebServer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import utilities.HTTPConnector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;

interface HttpsServerTest {

    HTTPConnector connector = new HTTPConnector();

    WebServer getWebServer();

    int testPortNumber();

    @Test
    default void webServerIsRunningHTTPS() {
        assertTrue(getWebServer().isUsingHTTPS(), "WebServer is not using https");
    }

    /**
     * Test case against "Perm level 0 required, got 0".
     */
    @Test
    default void userCanLogIn() throws Exception {
        webServerIsRunningHTTPS();

        String address = "https://localhost:" + testPortNumber();

        String cookie = login(address);
        testAccess(address + "/server/Server%201", cookie);
    }

    default void testAccess(String address, String cookie) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        try {
            connection = connector.getConnection("GET", address);
            connection.setRequestProperty("Cookie", cookie);

            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200 -> {}
                case 302 -> throw new IllegalStateException("Redirection to " + connection.getHeaderField("Location"));
                case 400 -> throw new IllegalStateException("Bad Request: " + address);
                case 403 -> throw new IllegalStateException(address + " returned 403");
                case 404 -> throw new IllegalStateException(address + " returned a 404.");
                case 500 -> throw new IllegalStateException(); // Not supported
                default -> throw new IllegalStateException(address + "| Wrong response code " + responseCode);
            }
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    default String login(String address) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        String cookie;
        try {
            connection = connector.getConnection("POST", address + "/auth/login");
            connection.setDoOutput(true);
            connection.getOutputStream().write("user=test&password=testPass".getBytes());
            try (InputStream in = connection.getInputStream()) {
                String responseBody = new String(IOUtils.toByteArray(in));
                assertTrue(responseBody.contains("\"success\":true"), () -> "Not successful: " + responseBody);
                cookie = connection.getHeaderField("Set-Cookie").split(";")[0];
                System.out.println("Got cookie: " + cookie);
            }
        } finally {
            assertNotNull(connection);
            connection.disconnect();
        }
        return cookie;
    }

    default void logout(String address, String cookie) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        try {
            connection = connector.getConnection("POST", address + "/auth/logout");
            connection.setRequestProperty("Cookie", cookie);
            int responseCode = connection.getResponseCode();
            assertEquals(302, responseCode, () -> "Logout not redirecting, got response code " + responseCode);
        } finally {
            assertNotNull(connection);
            connection.disconnect();
        }
    }
}