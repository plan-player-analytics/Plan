package main.java.com.djrapitops.plan.data.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class contains the geolocation cache.
 * <p>
 * It caches all IPs with their matching country.
 * <p>
 * This cache uses the Google Guava {@link Cache}.
 *
 * @author Fuzzlemann
 * @since 3.5.5
 */
public class GeolocationCacheHandler {

    private static final Cache<String, String> geolocationCache = CacheBuilder.newBuilder()
            .build();

    /**
     * Constructor used to hide the public constructor
     */
    private GeolocationCacheHandler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     * <p>
     * This method uses the {@code geolocationCache}, every first access is getting cached and then retrieved later.
     *
     * @param ipAddress The IP Address from which the country is retrieved
     * @return The name of the country in full length.
     * <p>
     * An exception from that rule is when the country is unknown or the retrieval of the country failed in any way,
     * if that happens, "Not Known" will be returned.
     * @see #getUncachedCountry(String)
     */
    public static String getCountry(String ipAddress) {
        Log.debug("Started country retrieval from IP Address " + ipAddress);

        String country = getCachedCountry(ipAddress);

        if (country != null) {
            Log.debug("Got cached country from IP Address " + ipAddress + ": " + country);
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
     * if that happens, "Not Known" will be returned.
     * @see <a href="http://freegeoip.net">http://freegeoip.net</a>
     * @see #getCountry(String)
     */
    public static String getUncachedCountry(String ipAddress) {
        Benchmark.start("getUncachedCountry");

        URL url;

        String urlString = "http://freegeoip.net/csv/" + ipAddress;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.error("The URL \"" + urlString + "\" couldn't be converted to URL: " + e.getCause()); //Shouldn't ever happen
            return "Not Known";
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String resultLine = in.readLine();
            Log.debug("Result for country request for " + ipAddress + ": " + resultLine);

            String[] results = resultLine.split(",");
            String result = results[2];

            return result.isEmpty() ? "Not Known" : result;
        } catch (Exception exc) {
            return "Not Known";
        } finally {
            Benchmark.stop("getUncachedCountry");
        }
    }

    /**
     * Returns the cached country
     *
     * @param ipAddress The IP Address which is retrieved out of the cache
     * @return The cached country, {@code null} if the country is not cached
     */
    public static String getCachedCountry(String ipAddress) {
        return geolocationCache.getIfPresent(ipAddress);
    }

    /**
     * Checks if the IP Address is cached
     *
     * @param ipAddress The IP Address which is checked
     * @return true if the IP Address is cached
     */
    public static boolean isCached(String ipAddress) {
        return geolocationCache.asMap().containsKey(ipAddress);
    }
}
