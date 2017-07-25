package main.java.com.djrapitops.plan.data.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

/**
 * This class contains the geolocation cache.
 * <p>
 * It caches all IPs with their matching country.
 * <p>
 * This cache uses the Google Guava {@link Cache} and has a capacity of 10.000 entries.
 *
 * @author Fuzzlemann
 */
public class GeolocationCacheHandler {
    private static final Cache<String, String> geolocationCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build();

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     * <p>
     * This method uses the {@code geolocationCache}, every first access is getting cached and then retrieved later.
     *
     * @param ipAddress The IP Address from which the country is retrieved
     * @return The name of the country in full length.
     * <p>
     * An exception from that rule is when the country is unknown or the retrieval of the country failed in any way,
     * if that happens, the phrase for unknown country set in the config will be returned.
     * @see #getUncachedCountry(String)
     */
    public static String getCountry(String ipAddress) {
        Log.debug("Started country retrieval from IP Address " + ipAddress);

        Map<String, String> geolocationMap = geolocationCache.asMap();
        String country = geolocationMap.get(ipAddress);

        Log.debug("Got country from " + ipAddress + " out of cache: " + country + " (if null, country wasn't cached)");

        if (country != null) {
            return country;
        } else {
            country = getUncachedCountry(ipAddress);
            geolocationCache.put(ipAddress, country);

            Log.debug("Got uncached country from IP Address " + ipAddress + ": " + country);
            return country;
        }
    }

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     * <p>
     * This method uses the free service of freegeoip.net. The maximum amount of requests is 15.000 per hour.
     *
     * @param ipAddress The IP Address from which the country is retrieved
     * @return The name of the country in full length.
     * <p>
     * An exception from that rule is when the country is unknown or the retrieval of the country failed in any way,
     * if that happens, the phrase for unknown country set in the config will be returned.
     * @see <a href="http://freegeoip.net">http://freegeoip.net</a>
     * @see #getCountry(String)
     */
    private static String getUncachedCountry(String ipAddress) {
        Benchmark.start("getUncachedCountry");
        try {
            URL url = new URL("http://freegeoip.net/csv/" + ipAddress);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String resultLine = in.readLine();
            Log.debug(resultLine);
            in.close();

            String[] results = resultLine.split(",");
            String result = results[2];

            return result.isEmpty() ? "Not Known" : result;
        } catch (Exception exc) {
            return "Not Known";
        } finally {
            Benchmark.stop("getUncachedCountry");
        }

    }

}
