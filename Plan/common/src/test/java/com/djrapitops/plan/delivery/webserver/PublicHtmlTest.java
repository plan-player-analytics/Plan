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
import com.djrapitops.plan.delivery.DeliveryUtilities;
import com.djrapitops.plan.settings.config.PlanConfig;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import utilities.HTTPConnector;

import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class PublicHtmlTest {

    private static final HTTPConnector CONNECTOR = new HTTPConnector();

    @BeforeAll
    static void setupSystem(PlanSystem system) {
        system.enable();
    }

    @AfterAll
    static void teardownSystem(PlanSystem system) {
        system.disable();
    }

    @DisplayName("Public Html supports file types")
    @ParameterizedTest(name = "Supports file type {0}")
    @CsvSource({
            "avif", "bin", "bmp",
            "css", "csv", "eot",
            "gif", "html", "htm",
            "ico", "ics", "js",
            "jpeg", "jpg", "json",
            "jsonld", "mjs", "otf",
            "pdf", "php", "png",
            "rtf", "svg", "tif",
            "tiff", "ttf", "txt",
            "woff", "woff2", "xml",
    })
    void customFileIsResponded(String extension, PlanConfig config, DeliveryUtilities deliveryUtilities) throws Exception {
        String fileName = "test." + extension;
        Path publicHtmlDirectory = config.getResourceSettings().getPublicHtmlDirectory();
        Path file = publicHtmlDirectory.resolve(fileName);
        Files.createDirectories(publicHtmlDirectory);
        Files.write(file, new byte[]{1, 1, 1, 1});
        String address = deliveryUtilities.getAddresses().getAccessAddress()
                .orElse(deliveryUtilities.getAddresses().getFallbackLocalhostAddress());

        assertEquals(200, access(address + "/" + fileName));
        assertEquals(404, access(address + "/does-not-exist-" + fileName));
    }

    @Test
    @DisplayName("Public Html doesn't support some file types")
    void customFileTypeNotSupported(PlanConfig config, DeliveryUtilities deliveryUtilities) throws Exception {
        String fileName = "test.mp4";
        Path publicHtmlDirectory = config.getResourceSettings().getPublicHtmlDirectory();
        Path file = publicHtmlDirectory.resolve(fileName);
        Files.createDirectories(publicHtmlDirectory);
        Files.write(file, new byte[]{1, 1, 1, 1});
        String address = deliveryUtilities.getAddresses().getAccessAddress()
                .orElse(deliveryUtilities.getAddresses().getFallbackLocalhostAddress());

        assertEquals(404, access(address + "/" + fileName));
    }

    private int access(String address) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = CONNECTOR.getConnection("GET", address);

            return connection.getResponseCode();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }
}
