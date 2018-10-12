package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.SystemMockUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.doReturn;

/**
 * @author Fuzzlemann
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class GeolocationCacheTest {

    private final Map<String, String> ipsToCountries = new HashMap<>();

    @Mock
    private PlanFiles files;
    @Mock
    private PlanConfig config;
    private GeolocationCache geolocationCache;

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem()
                .enableCacheSystem();
    }

    @Before
    public void setUp() throws IOException {
        doReturn(temporaryFolder.newFile("GeoIP.dat")).when(files.getFileFromPluginFolder("GeoIP.dat"));
        geolocationCache = new GeolocationCache(new Locale(), files, config, new TestPluginLogger());

        ipsToCountries.put("8.8.8.8", "United States");
        ipsToCountries.put("8.8.4.4", "United States");
        ipsToCountries.put("4.4.2.2", "United States");
        ipsToCountries.put("208.67.222.222", "United States");
        ipsToCountries.put("208.67.220.220", "United States");
        ipsToCountries.put("205.210.42.205", "Canada");
        ipsToCountries.put("64.68.200.200", "Canada");
        ipsToCountries.put("0.0.0.0", "Not Known");
        ipsToCountries.put("127.0.0.1", "Local Machine");
    }

    @Test
    public void testCountryGetting() {
        for (Map.Entry<String, String> entry : ipsToCountries.entrySet()) {
            String ip = entry.getKey();
            String expCountry = entry.getValue();

            String country = geolocationCache.getCountry(ip);

            assertEquals(country, expCountry);
        }
    }

    @Test
    public void testCaching() {
        for (Map.Entry<String, String> entry : ipsToCountries.entrySet()) {
            String ip = entry.getKey();
            String expIp = entry.getValue();

            assertFalse(geolocationCache.isCached(ip));
            String countrySecondCall = geolocationCache.getCountry(ip);
            assertTrue(geolocationCache.isCached(ip));

            String countryThirdCall = geolocationCache.getCountry(ip);

            assertEquals(countrySecondCall, countryThirdCall);
            assertEquals(countryThirdCall, expIp);
        }
    }
}
