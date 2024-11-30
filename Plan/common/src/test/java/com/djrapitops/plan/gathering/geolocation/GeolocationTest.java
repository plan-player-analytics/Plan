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
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import extension.FullSystemExtension;
import net.playeranalytics.plugin.server.PluginLogger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestErrorLogger;
import utilities.TestPluginLogger;
import utilities.TestResources;
import utilities.mocks.TestProcessing;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

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
        config.set(DataGatheringSettings.GEOLOCATION_DOWNLOAD_URL, "https://geodb.playeranalytics.net/GeoLite2-Country.mmdb");

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

    // Test utility for reading https://cable.ayra.ch/ip/data/countries.json for getting first IP of each country
    // Have to manually remove 3 first ones and the IPv6 addresses at the end.
    public static void main(String[] args) throws URISyntaxException, IOException {
        File testResourceFile = TestResources.getTestResourceFile("countries.json", GeolocationTest.class);
        String read = Files.readString(testResourceFile.toPath());
        Map<String, Map<String, List<String>>> contents = new Gson().fromJson(new StringReader(read), new TypeToken<>() {}.getType());
        List<String> singleIpPerCountry = contents.values().stream()
                .map(Map::values)
                .map(set -> set.stream().findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(list -> list.get(0))
                .map(string -> string.split("/")[0])
                .toList();
        Path write = new File("src/test/resources/countries-reduced.txt").toPath();
        Files.write(write, singleIpPerCountry, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    @TestFactory
    @DisplayName("Country has geocode")
    Collection<DynamicTest> everyCountryHasCodeInGeocodesJson(Graphs graphs) throws URISyntaxException, IOException {
        Map<String, String> geocodes = graphs.special().getGeocodes();
        File testResourceFile = TestResources.getTestResourceFile("countries-reduced.txt", GeolocationTest.class);
        try (Stream<String> lines = Files.lines(testResourceFile.toPath())) {
            return lines
                    .map(underTest::getCountry)
                    .distinct()
                    .map(country -> DynamicTest.dynamicTest(country, () -> {
                        assertTrue(geocodes.containsKey(country.toLowerCase()),
                                () -> "Country '" + country + "' had no geocode associated with it.");
                    })).toList();
        }
    }
}
