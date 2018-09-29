package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;

import javax.inject.Inject;
import javax.inject.Singleton;
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
@Singleton
public class GeolocationCache implements SubSystem {

    private final Locale locale;
    private final PlanFiles files;
    private final PlanConfig config;
    private final PluginLogger logger;
    private final Map<String, String> cached;

    private File geolocationDB;

    @Inject
    public GeolocationCache(
            Locale locale,
            PlanFiles files,
            PlanConfig config,
            PluginLogger logger
    ) {
        this.locale = locale;
        this.files = files;
        this.config = config;
        this.logger = logger;

        this.cached = new HashMap<>();
    }

    @Override
    public void enable() throws EnableException {
        geolocationDB = files.getFileFromPluginFolder("GeoIP.dat");
        if (config.isTrue(Settings.DATA_GEOLOCATIONS)) {
            try {
                checkDB();
            } catch (UnknownHostException e) {
                logger.error(locale.getString(PluginLang.ENABLE_NOTIFY_GEOLOCATIONS_INTERNET_REQUIRED));
            } catch (IOException e) {
                throw new EnableException(locale.getString(PluginLang.ENABLE_FAIL_GEODB_WRITE), e);
            }
        } else {
            logger.log(L.INFO_COLOR, "§e" + locale.getString(PluginLang.ENABLE_NOTIFY_GEOLOCATIONS_DISABLED));
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
    public String getCountry(String ipAddress) {
        String country = getCachedCountry(ipAddress);

        if (country != null) {
            return country;
        } else {
            country = getUnCachedCountry(ipAddress);
            cached.put(ipAddress, country);

            return country;
        }
    }

    /**
     * Returns the cached country
     *
     * @param ipAddress The IP Address which is retrieved out of the cache
     * @return The cached country, {@code null} if the country is not cached
     */
    private String getCachedCountry(String ipAddress) {
        return cached.get(ipAddress);
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
    private String getUnCachedCountry(String ipAddress) {
        if ("127.0.0.1".equals(ipAddress)) {
            return "Local Machine";
        }
        try {
            checkDB();

            try (DatabaseReader reader = new DatabaseReader.Builder(geolocationDB).build()) {
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
    private void checkDB() throws IOException {
        if (geolocationDB.exists()) {
            return;
        }
        URL downloadSite = new URL("http://geolite.maxmind.com/download/geoip/database/GeoLite2-Country.mmdb.gz");
        try (ReadableByteChannel rbc = Channels.newChannel(new GZIPInputStream(downloadSite.openStream()));
             FileOutputStream fos = new FileOutputStream(geolocationDB.getAbsoluteFile())) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    /**
     * Checks if the IP Address is cached
     *
     * @param ipAddress The IP Address which is checked
     * @return true if the IP Address is cached
     */
    boolean isCached(String ipAddress) {
        return cached.containsKey(ipAddress);
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
