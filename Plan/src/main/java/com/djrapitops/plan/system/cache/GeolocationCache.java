package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
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

    private final Supplier<Locale> locale;
    private final Map<String, String> cached;
    private File geolocationDB;

    public GeolocationCache(Supplier<Locale> locale) {
        this.locale = locale;
        cached = new HashMap<>();
    }

    @Override
    public void enable() throws EnableException {
        geolocationDB = new File(FileSystem.getDataFolder(), "GeoIP.dat");
        if (Settings.DATA_GEOLOCATIONS.isTrue()) {
            try {
                GeolocationCache.checkDB();
            } catch (UnknownHostException e) {
                Log.error(locale.get().getString(PluginLang.ENABLE_NOTIFY_GEOLOCATIONS_INTERNET_REQUIRED));
            } catch (IOException e) {
                throw new EnableException(locale.get().getString(PluginLang.ENABLE_FAIL_GEODB_WRITE), e);
            }
        } else {
            Log.infoColor("§e" + locale.get().getString(PluginLang.ENABLE_NOTIFY_GEOLOCATIONS_DISABLED));
        }
    }

    /**
     * Retrieves the country in full length (e.g. United States) from the IP Address.
     * <p>
     * This method uses {@code cached}, every first access is getting cached and then retrieved later.
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
            getInstance().cached.put(ipAddress, country);

            return country;
        }
    }

    private static GeolocationCache getInstance() {
        GeolocationCache geolocationCache = CacheSystem.getInstance().getGeolocationCache();
        Verify.nullCheck(geolocationCache, () -> new IllegalStateException("GeolocationCache was not initialized."));
        return geolocationCache;
    }

    /**
     * Returns the cached country
     *
     * @param ipAddress The IP Address which is retrieved out of the cache
     * @return The cached country, {@code null} if the country is not cached
     */
    private static String getCachedCountry(String ipAddress) {
        return getInstance().cached.get(ipAddress);
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
        try (
                InputStream in = downloadSite.openStream();
                GZIPInputStream gzipIn = new GZIPInputStream(in);
                ReadableByteChannel rbc = Channels.newChannel(gzipIn);
                FileOutputStream fos = new FileOutputStream(getInstance().geolocationDB.getAbsoluteFile())
        ) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Checks if the IP Address is cached
     *
     * @param ipAddress The IP Address which is checked
     * @return true if the IP Address is cached
     */
    public static boolean isCached(String ipAddress) {
        return getInstance().cached.containsKey(ipAddress);
    }

    @Override
    public void disable() {
        cached.clear();
    }

    /**
     * Clears the cache
     */
    public void clearCache() {
        cached.clear();
    }
}
