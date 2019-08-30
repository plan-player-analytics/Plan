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
package com.djrapitops.plan.system.storage.database.transactions.events;

import com.djrapitops.plan.system.delivery.domain.DateObj;
import com.djrapitops.plan.system.gathering.domain.Ping;
import com.djrapitops.plan.system.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.system.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.analysis.Median;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transaction to store player's Ping value on a server.
 *
 * @author Rsl1122
 */
public class PingStoreTransaction extends Transaction {

    private final UUID playerUUID;
    private final UUID serverUUID;
    private final List<DateObj<Integer>> pingList;

    private OptionalInt max;

    public PingStoreTransaction(UUID playerUUID, UUID serverUUID, List<DateObj<Integer>> pingList) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.pingList = pingList;
    }

    @Override
    protected boolean shouldBeExecuted() {
        max = getMax();
        return max.isPresent();
    }

    @Override
    protected void performOperations() {
        Ping ping = calculateAggregatePing();
        execute(DataStoreQueries.storePing(playerUUID, serverUUID, ping));
    }

    private Ping calculateAggregatePing() {
        long lastDate = pingList.get(pingList.size() - 1).getDate();

        int minValue = getMinValue();
        int meanValue = getMeanValue();
        int maxValue = max.getAsInt();

        return new Ping(lastDate, serverUUID, minValue, maxValue, meanValue);
    }

    private int getMinValue() {
        return pingList.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i > 0 && i < 4000)
                .min().orElse(-1);
    }

    private OptionalInt getMax() {
        return pingList.stream()
                .mapToInt(DateObj::getValue)
                .filter(i -> i > 0 && i < 4000)
                .max();
    }

    // VisibleForTesting
    int getMeanValue() {
        return (int) Median.forList(pingList.stream().map(DateObj::getValue).collect(Collectors.toList())).calculate();
    }
}