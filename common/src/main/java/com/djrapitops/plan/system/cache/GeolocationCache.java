package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This class contains the geolocation cache.
 * <p>
 * It caches all IPs with their matching country.
 *
 * @author Fuzzlemann
 * @since 3.5.5
 */
public class GeolocationCache implements SubSystem {

    private final Map<String, String> geolocationCache;
    private File geolocationDB;

    public GeolocationCache() {
        geolocationCache = new HashMap<>();
    }

    private static GeolocationCache getInstance() {
        GeolocationCache geolocationCache = CacheSystem.getInstance().getGeolocationCache();
        Verify.nullCheck(geolocationCache, () -> new IllegalStateException("GeolocationCache was not initialized."));
        return geolocationCache;
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
     * @see #getUnCachedCountry(String)
     */
    public static String getCountry(String ipAddress) {
        String country = getCachedCountry(ipAddress);

        if (country != null) {
            return country;
        } else {
            country = getUnCachedCountry(ipAddress);
            getInstance().geolocationCache.put(ipAddress, country);

            return country;
        }
    }

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     * <p>
     * This product includes GeoLite2 data created by MaxMind, available from
     * <a href="http://www.maxmind.com">http://www.maxmind.com</a>.
     *
     * @param ipAddress The IP Address from which the country is retrieved
     * @return The name of the country in full length.
     * <p>
     * An exception from that rule is when the country is unknown or the retrieval of the country failed in any way,
     * if that happens, "Not Known" will be returned.
     * @see <a href="http://maxmind.com">http://maxmind.com</a>
     * @see #getCountry(String)
     */
    private static String getUnCachedCountry(String ipAddress) {
        if ("127.0.0.1".equals(ipAddress)) {
            return "Local Machine";
        }
        try {
            checkDB();

            try (DatabaseReader reader = new DatabaseReader.Builder(getInstance().geolocationDB).build()) {
                InetAddress inetAddress = InetAddress.getByName(ipAddress);

                CountryResponse response = reader.country(inetAddress);
                Country country = response.getCountry();

                return country.getName();
            }

        } catch (IOException | GeoIp2Exception e) {
            return "Not Known";
        }
    }

    /**
     * Checks if the DB exists, if not, it downloads it
     *
     * @throws IOException when an error at download or saving the DB happens
     */
    public static void checkDB() throws IOException {
        if (getInstance().geolocationDB.exists()) {
            return;
        }
        URL downloadSite = new URL("http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.mmdb.gz");
        try (ReadableByteChannel rbc = Channels.newChannel(new GZIPInputStream(downloadSite.openStream()));
             FileOutputStream fos = new FileOutputStream(getInstance().geolocationDB.getAbsoluteFile())) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Returns the cached country
     *
     * @param ipAddress The IP Address which is retrieved out of the cache
     * @return The cached country, {@code null} if the country is not cached
     */
    private static String getCachedCountry(String ipAddress) {
        return getInstance().geolocationCache.get(ipAddress);
    }

    /**
     * Checks if the IP Address is cached
     *
     * @param ipAddress The IP Address which is checked
     * @return true if the IP Address is cached
     */
    public static boolean isCached(String ipAddress) {
        return getInstance().geolocationCache.containsKey(ipAddress);
    }

    @Override
    public void enable() throws EnableException {
        geolocationDB = new File(FileSystem.getDataFolder(), "GeoIP.dat");
        if (Settings.DATA_GEOLOCATIONS.isTrue()) {
            try {
                GeolocationCache.checkDB();
            } catch (UnknownHostException e) {
                Log.error("Plan Requires internet access on first run to download GeoLite2 Geolocation database.");
            } catch (IOException e) {
                throw new EnableException("Something went wrong saving the downloaded GeoLite2 Geolocation database", e);
            }
        }
    }

    @Override
    public void disable() {
    }

    /**
     * Clears the cache
     */
    public void clearCache() {
        geolocationCache.clear();
    }
}
