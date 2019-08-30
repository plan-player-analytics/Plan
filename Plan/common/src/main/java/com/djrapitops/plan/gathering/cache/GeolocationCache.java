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
package com.djrapitops.plan.gathering.cache;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.file.PlanFiles;
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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * This class contains the geolocation cache.
 * <p>
 * It caches all IPs with their matching country.
 *
 * @author Fuzzlemann
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
        if (config.isTrue(DataGatheringSettings.GEOLOCATIONS)) {
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

            try (
                    // See https://github.com/maxmind/MaxMind-DB-Reader-java#file-lock-on-windows
                    // for why InputStream is being used here instead.
                    InputStream in = Files.newInputStream(geolocationDB.toPath());
                    DatabaseReader reader = new DatabaseReader.Builder(in).build()
            ) {
                InetAddress inetAddress = InetAddress.getByName(ipAddress);

                CountryResponse response = reader.country(inetAddress);
                Country country = response.getCountry();
                String countryName = country.getName();

                return countryName != null ? countryName : "Not Known";
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
        try (
                InputStream in = downloadSite.openStream();
                GZIPInputStream gzipIn = new GZIPInputStream(in);
                ReadableByteChannel rbc = Channels.newChannel(gzipIn);
                FileOutputStream fos = new FileOutputStream(geolocationDB.getAbsoluteFile());
                FileChannel channel = fos.getChannel()
        ) {
            channel.transferFrom(rbc, 0, Long.MAX_VALUE);
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
