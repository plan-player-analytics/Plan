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
package com.djrapitops.plan.gathering.geolocation;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.file.PlanFiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestPluginLogger;
import utilities.mocks.objects.TestRunnableFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests for Geolocation functionality.
 *
 * @author AuroraLS3
 * @author Fuzzlemann
 */
@ExtendWith(MockitoExtension.class)
class GeolocationTest {

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
        GeolocationTest.tempDir = tempDir;
        IP_STORE = GeolocationTest.tempDir.resolve("GeoLite2-Country.mmdb").toFile();

        TEST_DATA.put("156.53.159.86", "United States"); // Oregon, US
        TEST_DATA.put("208.67.222.222", "United States"); // California, US
        TEST_DATA.put("208.67.220.220", "United States"); // California, US
        TEST_DATA.put("205.210.42.205", "Canada");
        TEST_DATA.put("64.68.200.200", "Canada");
        TEST_DATA.put("0.0.0.0", "Not Found"); // Invalid IP
        TEST_DATA.put("127.0.0.1", "Local Machine");
    }

    @BeforeEach
    void setUpCache() {
        when(config.isTrue(DataGatheringSettings.GEOLOCATIONS)).thenReturn(true);
        lenient().when(config.isTrue(DataGatheringSettings.ACCEPT_GEOLITE2_EULA)).thenReturn(true);
        when(files.getFileFromPluginFolder("GeoLite2-Country.mmdb")).thenReturn(IP_STORE);
        when(files.getFileFromPluginFolder("GeoIP.dat")).thenReturn(tempDir.resolve("Non-file").toFile());

        assertTrue(config.isTrue(DataGatheringSettings.GEOLOCATIONS));

        GeoLite2Geolocator geoLite2Geolocator = new GeoLite2Geolocator(files, config);
        underTest = new GeolocationCache(new Locale(), config, geoLite2Geolocator, new TestPluginLogger(), TestRunnableFactory.forSameThread());
        underTest.enable();

        assertTrue(underTest.canGeolocate());
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
            String expected = entry.getValue();
            String result = underTest.getCountry(ip);

            assertEquals(expected, result, "Tested " + ip + ", expected: <" + expected + "> but was: <" + result + '>');
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
