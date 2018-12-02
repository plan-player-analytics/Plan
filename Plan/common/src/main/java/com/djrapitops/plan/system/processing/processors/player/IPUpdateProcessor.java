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
