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

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.db.access.queries.DataStoreQueries;
import com.djrapitops.plan.db.access.transactions.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Transaction to store server's TPS data.
 *
 * @author Rsl1122
 */
public class TPSStoreTransaction extends Transaction {

    private final UUID serverUUID;
    private final List<TPS> tpsList;

    public TPSStoreTransaction(UUID serverUUID, List<TPS> tpsList) {
        this.serverUUID = serverUUID;
        this.tpsList = tpsList;
    }

    @Override
    protected void performOperations() {
        TPS tps = calculateTPS();
        execute(DataStoreQueries.storeTPS(serverUUID, tps));
    }

    private TPS calculateTPS() {
        long lastDate = tpsList.get(tpsList.size() - 1).getDate();
        double averageTPS = tpsList.stream().mapToDouble(TPS::getTicksPerSecond).average().orElse(0);
        int peakPlayersOnline = tpsList.stream().mapToInt(TPS::getPlayers).max().orElse(0);
        double averageCPUUsage = tpsList.stream().mapToDouble(TPS::getCPUUsage).average().orElse(0);
        long averageUsedMemory = (long) tpsList.stream().mapToLong(TPS::getUsedMemory).average().orElse(0);
        int averageEntityCount = (int) tpsList.stream().mapToInt(TPS::getEntityCount).average().orElse(0);
        int averageChunksLoaded = (int) tpsList.stream().mapToInt(TPS::getChunksLoaded).average().orElse(0);
        long freeDiskSpace = (long) tpsList.stream().mapToLong(TPS::getFreeDiskSpace).average().orElse(0);

        return TPSBuilder.get()
                .date(lastDate)
                .tps(averageTPS)
                .playersOnline(peakPlayersOnline)
                .usedCPU(averageCPUUsage)
                .usedMemory(averageUsedMemory)
                .entities(averageEntityCount)
                .chunksLoaded(averageChunksLoaded)
                .freeDiskSpace(freeDiskSpace)
                .toTPS();
    }
}