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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.Predicates;
import com.djrapitops.plan.utilities.analysis.Median;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.List;
import java.util.UUID;

/**
 * Transaction to store player's Ping value on a server.
 *
 * @author AuroraLS3
 */
public class PingStoreTransaction extends Transaction {

    private final UUID playerUUID;
    private final ServerUUID serverUUID;
    private final List<DateObj<Integer>> pingList;

    public PingStoreTransaction(UUID playerUUID, ServerUUID serverUUID, List<DateObj<Integer>> pingList) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.pingList = pingList;
    }

    @Override
    protected void performOperations() {
        Ping ping = calculateAggregatePing();

        DBOpException userInsertError = null;
        if (Boolean.FALSE.equals(query(PlayerFetchQueries.isPlayerRegistered(playerUUID)))) {
            userInsertError = tryToRegisterUser(ping.getDate());
        }

        try {
            execute(DataStoreQueries.storePing(playerUUID, serverUUID, ping));
        } catch (DBOpException failed) {
            if (userInsertError != null) failed.addSuppressed(userInsertError);
            if (failed.isUserIdConstraintViolation() || failed.isDuplicateKeyViolation()) {
                retry(ping, failed);
            } else {
                throw failed;
            }
        }
    }

    private void retry(Ping ping, DBOpException failed) {
        DBOpException userInsertError = null;
        try {
            userInsertError = tryToRegisterUser(ping.getDate());
            execute(DataStoreQueries.storePing(playerUUID, serverUUID, ping));
        } catch (DBOpException failedAgain) {
            if (userInsertError != null) failedAgain.addSuppressed(userInsertError);
            failedAgain.addSuppressed(failed);
            throw failedAgain;
        }
    }

    private DBOpException tryToRegisterUser(long date) {
        try {
            execute(DataStoreQueries.registerBaseUser(playerUUID, date, playerUUID.toString()));
            return null;
        } catch (DBOpException failedInsert) {
            return failedInsert;
        }
    }

    private Ping calculateAggregatePing() {
        long lastDate = pingList.get(pingList.size() - 1).getDate();

        int minValue = getMinValue();
        int meanValue = getMeanValue();
        int maxValue = getMax();

        return new Ping(lastDate, serverUUID, minValue, maxValue, meanValue);
    }

    private int getMinValue() {
        return pingList.stream()
                .mapToInt(DateObj::getValue)
                .filter(Predicates::pingInRange)
                .min().orElse(-1);
    }

    private int getMax() {
        return pingList.stream()
                .mapToInt(DateObj::getValue)
                .filter(Predicates::pingInRange)
                .max().orElse(-1);
    }

    // VisibleForTesting
    int getMeanValue() {
        List<Integer> values = Lists.map(pingList, DateObj::getValue);
        return (int) Median.forList(values).calculate();
    }
}