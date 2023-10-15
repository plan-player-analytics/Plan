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

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.file.PlanFiles;
import extension.FullSystemExtension;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestErrorLogger;
import utilities.TestPluginLogger;
import utilities.mocks.TestProcessing;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Geolocation functionality.
 *
 * @author AuroraLS3
 * @author Fuzzlemann
 */
@ExtendWith({MockitoExtension.class, FullSystemExtension.class})
class GeolocationTest {

    private static final Map<String, String> TEST_DATA = new HashMap<>();

    private GeolocationCache underTest;

    @BeforeAll
    static void setUpTestData() {
        TEST_DATA.put("156.53.159.86", "United States"); // Oregon, US
        TEST_DATA.put("208.67.222.222", "United States"); // California, US
        TEST_DATA.put("208.67.220.220", "United States"); // California, US
        TEST_DATA.put("205.210.42.205", "Canada");
        TEST_DATA.put("64.68.200.200", "Canada");
        TEST_DATA.put("0.0.0.0", "Not Found"); // Invalid IP
        TEST_DATA.put("127.0.0.1", "Local Machine");
    }

    @BeforeEach
    void setUpCache(PlanFiles files, ConfigSystem configSystem, PlanConfig config) {
        config.set(DataGatheringSettings.GEOLOCATIONS, true);
        config.set(DataGatheringSettings.ACCEPT_GEOLITE2_EULA, true);

        GeoLite2Geolocator geoLite2Geolocator = new GeoLite2Geolocator(files, config);
        PluginLogger logger = new TestPluginLogger();
        Processing processing = new TestProcessing(Locale::new, logger, new TestErrorLogger());

        underTest = new GeolocationCache(new Locale(), config, geoLite2Geolocator, logger, processing);
        files.enable();
        configSystem.enable();
        underTest.enable();

        assertTrue(underTest.canGeolocate());
    }

    @AfterEach
    void tearDownCache(PlanSystem system, PlanFiles files) throws IOException {
        Files.deleteIfExists(files.getFileFromPluginFolder("GeoLite2-Country.mmdb").toPath());
        underTest.disable();
        system.disable();
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
