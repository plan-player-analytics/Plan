package com.djrapitops.plan.data.cache;

import com.djrapitops.plan.system.cache.CacheSystem;
import com.djrapitops.plan.system.cache.GeolocationCache;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.TestInit;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.*;

/**
 * @author Fuzzlemann
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class GeolocationCacheTest {

    private final Map<String, String> ipsToCountries = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        TestInit.init();
        CacheSystem.getInstance().getGeolocationCache().clearCache();

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

            String country = GeolocationCache.getCountry(ip);

            assertEquals(country, expCountry);
        }
    }

    @Test
    public void testCaching() {
        for (Map.Entry<String, String> entry : ipsToCountries.entrySet()) {
            String ip = entry.getKey();
            String expIp = entry.getValue();

            assertFalse(GeolocationCache.isCached(ip));
            String countrySecondCall = GeolocationCache.getCountry(ip);
            assertTrue(GeolocationCache.isCached(ip));

            String countryThirdCall = GeolocationCache.getCountry(ip);

            assertEquals(countrySecondCall, countryThirdCall);
            assertEquals(countryThirdCall, expIp);
        }
    }
}
