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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.net.InetAddress;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Transaction to update Geo information of a player in the database.
 *
 * @author AuroraLS3
 */
public class GeoInfoStoreTransaction extends Transaction {

    private final UUID playerUUID;
    private String ip;
    private long time;
    private UnaryOperator<String> geolocationFunction;

    private GeoInfo geoInfo;

    public GeoInfoStoreTransaction(UUID playerUUID, String ip, long time, UnaryOperator<String> geolocationFunction) {
        this.playerUUID = playerUUID;
        this.ip = ip;
        this.time = time;
        this.geolocationFunction = geolocationFunction;
    }

    public GeoInfoStoreTransaction(
            UUID playerUUID,
            InetAddress ip,
            long time,
            UnaryOperator<String> geolocationFunction
    ) {
        this.playerUUID = playerUUID;
        this.ip = ip.getHostAddress();
        this.time = time;
        this.geolocationFunction = geolocationFunction;
    }

    public GeoInfoStoreTransaction(UUID playerUUID, GeoInfo geoInfo) {
        this.playerUUID = playerUUID;
        this.geoInfo = geoInfo;
    }

    private GeoInfo createGeoInfo() {
        // Can return null
        String country = geolocationFunction.apply(ip);
        return new GeoInfo(country, time);
    }

    @Override
    protected void performOperations() {
        if (geoInfo == null) geoInfo = createGeoInfo();
        if (geoInfo.getGeolocation() == null) return; // Don't save null geolocation.
        try {
            execute(DataStoreQueries.storeGeoInfo(playerUUID, geoInfo));
        } catch (DBOpException failed) {
            if (failed.isUserIdConstraintViolation()) {
                retry();
            } else {
                throw failed;
            }
        }
    }

    private void retry() {
        executeOther(new PlayerRegisterTransaction(playerUUID, System::currentTimeMillis, playerUUID.toString()));
        execute(DataStoreQueries.storeGeoInfo(playerUUID, geoInfo));
    }
}