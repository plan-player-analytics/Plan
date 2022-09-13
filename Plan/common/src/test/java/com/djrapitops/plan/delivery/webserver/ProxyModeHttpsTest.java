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
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.RandomData;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyModeHttpsTest {
    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);

    private static PlanSystem system;

    @BeforeAll
    static void setUpClass(@TempDir Path tempDir) throws Exception {
        PluginMockComponent component = new PluginMockComponent(tempDir);
        system = component.getPlanSystem();

        PlanConfig config = system.getConfigSystem().getConfig();

        config.set(WebserverSettings.CERTIFICATE_PATH, "proxy");

        config.set(WebserverSettings.PORT, TEST_PORT_NUMBER);

        system.enable();
    }

    @AfterAll
    static void tearDownClass() {
        if (system != null) {
            system.disable();
        }
    }

    @Test
    @DisplayName("Webserver with 'proxy' keystore path assumes proxy server is handling https")
    void proxyModeAddressIsHttps() {
        assertEquals("https", system.getWebServerSystem().getWebServer().getProtocol());
    }
}
