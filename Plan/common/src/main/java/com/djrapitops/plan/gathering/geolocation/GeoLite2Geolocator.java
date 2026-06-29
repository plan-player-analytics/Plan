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

import com.djrapitops.plan.exceptions.PreparationException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * {@link Geolocator} implementation for MaxMind GeoLite2 database.
 * <p>
 * This product includes GeoLite2 data created by MaxMind, available from
 * <a href="http://www.maxmind.com">http://www.maxmind.com</a>.
 *
 * @author AuroraLS3
 * @see <a href="http://maxmind.com">http://maxmind.com</a>
 */
@Singleton
public class GeoLite2Geolocator implements Geolocator {

    private final PlanFiles files;
    private final PlanConfig config;

    private File geolocationDB;

    @Inject
    public GeoLite2Geolocator(PlanFiles files, PlanConfig config) {
        this.files = files;
        this.config = config;
    }

    @Override
    public void prepare() throws IOException {
        if (config.isFalse(DataGatheringSettings.ACCEPT_GEOLITE2_EULA)) {
            throw new PreparationException("Downloading GeoLite2 requires accepting GeoLite2 EULA - see '"
                    + DataGatheringSettings.ACCEPT_GEOLITE2_EULA.getPath() + "' in the config.");
        }

        geolocationDB = files.getFileFromPluginFolder("GeoLite2-Country.mmdb");

        if (geolocationDB.exists()) {
            if (geolocationDB.lastModified() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7L)) {
                return; // Database is new enough
            } else {
                Files.delete(geolocationDB.toPath()); // Delete old data according to restriction 3. in EULA
            }
        }
        downloadDatabase();
        // Delete old Geolocation database file if it still exists (on success to avoid a no-file situation)
        Files.deleteIfExists(files.getFileFromPluginFolder("GeoIP.dat").toPath());
    }

    private void downloadDatabase() throws IOException {
        // Avoid Socket leak with the parameters in case download url has proxy
        Properties properties = System.getProperties();
        properties.setProperty("sun.net.client.defaultConnectTimeout", Long.toString(TimeUnit.MINUTES.toMillis(1L)));
        properties.setProperty("sun.net.client.defaultReadTimeout", Long.toString(TimeUnit.MINUTES.toMillis(1L)));
        properties.setProperty("sun.net.http.retryPost", Boolean.toString(false));

        String downloadURL = config.get(DataGatheringSettings.GEOLOCATION_DOWNLOAD_URL);
        URL downloadSite = URI.create(downloadURL).toURL();
        if (downloadURL.startsWith("https://download.maxmind.com/app/geoip_download")) {
            try (
                    InputStream in = downloadSite.openStream();
                    GZIPInputStream gzipIn = new GZIPInputStream(in);
                    TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
                    FileOutputStream fos = new FileOutputStream(geolocationDB.getAbsoluteFile())
            ) {
                findAndCopyFromTar(tarIn, fos);
            }
        } else {
            URLConnection connection = downloadSite.openConnection();
            connection.setRequestProperty("X-PLAN-GEODB-TOKEN", "68342d1f-5fc9-4853-bd1e-ba88c466b3a6");
            try (
                    InputStream in = connection.getInputStream();
                    FileOutputStream fos = new FileOutputStream(geolocationDB.getAbsoluteFile())
            ) {
                IOUtils.copy(in, fos);
            }
        }
    }

    private void findAndCopyFromTar(TarArchiveInputStream tarIn, FileOutputStream fos) throws IOException {
        // Breadth first search
        Queue<TarArchiveEntry> entries = new ArrayDeque<>();
        entries.add(tarIn.getNextEntry());
        while (!entries.isEmpty()) {
            TarArchiveEntry entry = entries.poll();
            if (entry.isDirectory()) {
                entries.addAll(Arrays.asList(entry.getDirectoryEntries()));
            }

            // Looking for this file
            if (entry.getName().endsWith("GeoLite2-Country.mmdb")) {
                IOUtils.copy(tarIn, fos);
                break; // Found it
            }

            TarArchiveEntry next = tarIn.getNextEntry();
            if (next != null) entries.add(next);
        }
    }

    @Override
    public Optional<String> getCountry(InetAddress inetAddress) {
        if (inetAddress == null) return Optional.empty();
        if (inetAddress.getHostAddress().contains("127.0.0.1")) return Optional.of("Local Machine");
        if (inetAddress.isSiteLocalAddress()) return Optional.of("Local Private Network");

        try (
                // See https://github.com/maxmind/MaxMind-DB-Reader-java#file-lock-on-windows
                // for why InputStream is being used here instead.
                InputStream in = Files.newInputStream(geolocationDB.toPath());
                DatabaseReader reader = new DatabaseReader.Builder(in).build()
        ) {
            CountryResponse response = reader.country(inetAddress);
            Country country = response.getCountry();
            String countryName = country.getName();

            return Optional.ofNullable(countryName);
        } catch (IOException | GeoIp2Exception e) {
            return Optional.empty();
        }
    }
}