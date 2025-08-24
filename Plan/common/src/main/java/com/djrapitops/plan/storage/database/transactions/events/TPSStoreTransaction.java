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

import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import net.playeranalytics.plugin.server.PluginLogger;

import java.util.concurrent.TimeUnit;

/**
 * Transaction to store server's TPS data.
 *
 * @author AuroraLS3
 */
public class TPSStoreTransaction extends Transaction {

    private static long lastStorageCheck = 0L;

    private final PluginLogger logger;
    private final ServerUUID serverUUID;
    private final TPS tps;

    public TPSStoreTransaction(PluginLogger logger, ServerUUID serverUUID, TPS tps) {
        this.serverUUID = serverUUID;
        this.tps = tps;
        this.logger = logger;
    }

    public TPSStoreTransaction(ServerUUID serverUUID, TPS tps) {
        this.serverUUID = serverUUID;
        this.tps = tps;
        logger = null;
    }

    public static void setLastStorageCheck(long lastStorageCheck) {
        TPSStoreTransaction.lastStorageCheck = lastStorageCheck;
    }

    @Override
    protected void performOperations() {
        long now = System.currentTimeMillis();
        if (logger != null && now - lastStorageCheck > TimeUnit.MINUTES.toMillis(30)) {
            performDuplicateServerUUIDServerCheck(now);
            TPSStoreTransaction.setLastStorageCheck(now);
        }

        execute(DataStoreQueries.storeTPS(serverUUID, tps));
    }

    private void performDuplicateServerUUIDServerCheck(long now) {
        Long lastStoredData = query(TPSQueries.fetchLastStoredTpsDate(serverUUID))
                .orElse(0L);
        long diff = now - lastStoredData;
        if (logger != null && diff < TimeUnit.SECONDS.toMillis(30)) {
            logger.warn("Database had TPS data which was stored " + diff + "ms ago, this is a sign that two servers are storing data as " + serverUUID +
                    " - Check that you have not copied /plugins/Plan/ServerInfoFile.yml between two servers. (This warning will show on both servers)");
        }
    }
}