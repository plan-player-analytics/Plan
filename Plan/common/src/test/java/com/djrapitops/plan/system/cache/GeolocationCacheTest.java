package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DataGatheringSettings;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link GeolocationCache}.
 *
 * @author Fuzzlemann
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class GeolocationCacheTest {

    private static final Map<String, String> TEST_DATA = new HashMap<>();
    private static File IP_STORE;
    @Mock
    public PlanFiles files;
    @Mock
    public PlanConfig config;

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private GeolocationCache underTest;

    @BeforeClass
    public static void setUpClass() throws IOException {
        IP_STORE = temporaryFolder.newFile("GeoIP.dat");
        // TemporaryFolder creates the file, which prevents cache from downloading the GeoIP database from the internet.
        // This is why the file needs to be removed first.
        Files.delete(IP_STORE.toPath());

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

    @Before
    public void setUp() throws EnableException {
        when(config.isTrue(DataGatheringSettings.GEOLOCATIONS)).thenReturn(true);
        when(files.getFileFromPluginFolder("GeoIP.dat")).thenReturn(IP_STORE);

        assertTrue(config.isTrue(DataGatheringSettings.GEOLOCATIONS));

        underTest = new GeolocationCache(new Locale(), files, config, new TestPluginLogger());
        underTest.enable();
    }

    @Test
    public void countryIsFetched() {
        for (Map.Entry<String, String> entry : TEST_DATA.entrySet()) {
            String ip = entry.getKey();
            String expCountry = entry.getValue();

            String country = underTest.getCountry(ip);

            assertEquals(expCountry, country);
        }
    }

    @Test
    public void callsToCachedIPsReturnCachedEntries() {
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
