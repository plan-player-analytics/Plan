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
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import utilities.HTTPConnector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        testAccess(address, cookie);
    }

    default void testAccess(String address, String cookie) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection connection = null;
        try {
            connection = connector.getConnection("GET", address);
            connection.setRequestProperty("Cookie", cookie);

            int responseCode = connection.getResponseCode();

            switch (responseCode) {
                case 200, 302 -> {}
                case 400 -> throw new IllegalStateException("Bad Request: " + address);
                case 403 -> throw new IllegalStateException(address + " returned 403");
                case 404 -> throw new IllegalStateException(address + " returned a 404.");
                case 500 -> throw new IllegalStateException(); // Not supported
                default -> throw new IllegalStateException(address + "| Wrong response code " + responseCode);
            }
        } finally {
            connection.disconnect();
        }
    }

    default String login(String address) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        HttpURLConnection loginConnection = null;
        String cookie = "";
        try {
            loginConnection = connector.getConnection("POST", address + "/auth/login");
            loginConnection.setDoOutput(true);
            loginConnection.getOutputStream().write("user=test&password=testPass".getBytes());
            try (InputStream in = loginConnection.getInputStream()) {
                String responseBody = new String(IOUtils.toByteArray(in));
                assertTrue(responseBody.contains("\"success\":true"), () -> "Not successful: " + responseBody);
                cookie = loginConnection.getHeaderField("Set-Cookie").split(";")[0];
                System.out.println("Got cookie: " + cookie);
            }
        } finally {
            loginConnection.disconnect();
        }
        return cookie;
    }
}