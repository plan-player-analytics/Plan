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
package com.djrapitops.plan.db.access.transactions.events;

import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.db.access.queries.DataStoreQueries;
import com.djrapitops.plan.db.access.transactions.Transaction;

import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.function.Function;

/**
 * Transaction to update Geo information of a player in the database.
 *
 * @author Rsl1122
 */
public class GeoInfoStoreTransaction extends Transaction {

    private static boolean hasFailed = false;

    private final UUID playerUUID;
    private InetAddress ip;
    private long time;
    private Function<String, String> geolocationFunction;

    private GeoInfo geoInfo;

    public GeoInfoStoreTransaction(
            UUID playerUUID,
            InetAddress ip,
            long time,
            Function<String, String> geolocationFunction
    ) {
        this.playerUUID = playerUUID;
        this.ip = ip;
        this.time = time;
        this.geolocationFunction = geolocationFunction;
    }

    public GeoInfoStoreTransaction(UUID playerUUID, GeoInfo geoInfo) {
        this.playerUUID = playerUUID;
        this.geoInfo = geoInfo;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return !hasFailed;
    }

    @Override
    protected void performOperations() {
        try {
            if (geoInfo == null) geoInfo = createGeoInfo();

            execute(DataStoreQueries.storeGeoInfo(playerUUID, geoInfo));
        } catch (NoSuchAlgorithmException noSHA256Available) {
            // SHA256 not available.
            hasFailed = true;
        }
    }

    private GeoInfo createGeoInfo() throws NoSuchAlgorithmException {
        String country = geolocationFunction.apply(ip.getHostAddress());
        return new GeoInfo(ip, country, time);
    }
}