/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Updates the IP and Geolocation of a user.
 *
 * @author Rsl1122
 */
public class IPUpdateProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final InetAddress ip;
    private final long time;

    private final Database database;
    private final GeolocationCache geolocationCache;

    IPUpdateProcessor(
            UUID uuid, InetAddress ip, long time,
            Database database,
            GeolocationCache geolocationCache
    ) {
        this.uuid = uuid;
        this.ip = ip;
        this.time = time;
        this.database = database;
        this.geolocationCache = geolocationCache;
    }

    @Override
    public void run() {
        try {
            String country = geolocationCache.getCountry(ip.getHostAddress());
            GeoInfo geoInfo = new GeoInfo(ip, country, time);
            database.save().geoInfo(uuid, geoInfo);
        } catch (NoSuchAlgorithmException ignore) {
            // Ignored, SHA-256 should be available
        }
    }
}
