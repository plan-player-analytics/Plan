/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Updates the IP and Geolocation of a user.
 *
 * @author Rsl1122
 */
public class IPUpdateProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final String ip;
    private final long time;

    public IPUpdateProcessor(UUID uuid, String ip, long time) {
        this.uuid = uuid;
        this.ip = ip;
        this.time = time;
    }

    @Override
    public void run() {
        if (Settings.DATA_GEOLOCATIONS.isTrue()) {
            String country = GeolocationCache.getCountry(ip);
            try {
                GeoInfo geoInfo = new GeoInfo(ip, country, time);
                Database.getActive().save().geoInfo(uuid, geoInfo);
            } catch (DBException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
                Log.toLog(this.getClass(), e);
            }
        }
    }
}
