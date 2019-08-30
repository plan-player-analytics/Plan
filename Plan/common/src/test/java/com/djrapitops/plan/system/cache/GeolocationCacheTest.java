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
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.paths.DataGatheringSettings;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GeolocationCache}.
 *
 * @author Fuzzlemann
 */
@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
class GeolocationCacheTest {

    private static final Map<String, String> TEST_DATA = new HashMap<>();
    private static File IP_STORE;
    private static Path tempDir;

    @Mock
    public PlanFiles files;
    @Mock
    public PlanConfig config;

    private GeolocationCache underTest;

    @BeforeAll
    static void setUpTestData(@TempDir Path tempDir) {
        GeolocationCacheTest.tempDir = tempDir;
        IP_STORE = GeolocationCacheTest.tempDir.resolve("GeoIP.dat").toFile();

        TEST_DATA.put("8.8.8.8", "United States");
        TEST_DATA.put("8.8.4.4", "United States");
        TEST_DATA.put("4.4.2.2", "United States");
        TEST_DATA.put("208.67.222.222", "United States");
        TEST_DATA.put("208.67.220.220", "United States");
        TEST_DATA.put("205.210.42.205", "Canada");
        TEST_DATA.put("64.68.200.200", "Canada");
        TEST_DATA.put("0.0.0.0", "Not Known");
        TEST_DATA.put("127.0.0.1", "Local Machine");
    }

    @BeforeEach
    void setUpCache() throws EnableException {
        when(config.isTrue(DataGatheringSettings.GEOLOCATIONS)).thenReturn(true);
        when(files.getFileFromPluginFolder("GeoIP.dat")).thenReturn(IP_STORE);

        assertTrue(config.isTrue(DataGatheringSettings.GEOLOCATIONS));

        underTest = new GeolocationCache(new Locale(), files, config, new TestPluginLogger());
        underTest.enable();
    }

    @AfterEach
    void tearDownCache() throws IOException {
        underTest.disable();
        Files.deleteIfExists(IP_STORE.toPath());
    }

    @Test
    void countryIsFetched() {
        for (Map.Entry<String, String> entry : TEST_DATA.entrySet()) {
            String ip = entry.getKey();
            String expCountry = entry.getValue();

            String country = underTest.getCountry(ip);

            assertEquals(expCountry, country);
        }
    }

    @Test
    void callsToCachedIPsReturnCachedEntries() {
        for (Map.Entry<String, String> entry : TEST_DATA.entrySet()) {
            String ip = entry.getKey();
            String expIp = entry.getValue();

            assertFalse(underTest.isCached(ip));
            String countrySecondCall = underTest.getCountry(ip);
            assertTrue(underTest.isCached(ip));

            String countryThirdCall = underTest.getCountry(ip);

            assertSame(countrySecondCall, countryThirdCall);
            assertEquals(expIp, countryThirdCall);
        }
    }
}
