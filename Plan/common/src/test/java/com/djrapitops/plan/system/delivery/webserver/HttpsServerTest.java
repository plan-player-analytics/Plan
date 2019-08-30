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
package com.djrapitops.plan.system.delivery.webserver;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.utilities.Base64Util;
import org.junit.jupiter.api.Test;
import utilities.HTTPConnector;

import java.net.HttpURLConnection;
import java.net.URL;

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
        URL url = new URL(address);
        HttpURLConnection connection = connector.getConnection("GET", address);

        String user = Base64Util.encode("test:testPass");
        connection.setRequestProperty("Authorization", "Basic " + user);

        int responseCode = connection.getResponseCode();

        switch (responseCode) {
            case 200:
            case 302:
                return;
            case 400:
                throw new BadRequestException("Bad Request: " + url.toString());
            case 403:
                throw new ForbiddenException(url.toString() + " returned 403");
            case 404:
                throw new NotFoundException(url.toString() + " returned a 404, ensure that your server is connected to an up to date Plan server.");
            case 500:
                throw new InternalErrorException();
            default:
                throw new WebException(url.toString() + "| Wrong response code " + responseCode);
        }
    }
}