/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plugin.api.utility.log.Log;

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
        String country = GeolocationCache.getCountry(ip);
        try {
            Database.getActive().save().geoInfo(uuid, new GeoInfo(ip, country, time));
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        }
    }
}