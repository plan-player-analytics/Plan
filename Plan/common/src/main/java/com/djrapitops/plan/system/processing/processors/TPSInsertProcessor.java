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
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.List;

/**
 * Processes 60s average of a TPS list.
 *
 * @author Rsl1122
 */
public class TPSInsertProcessor implements CriticalRunnable {

    private final List<TPS> tpsList;

    private final Database database;

    TPSInsertProcessor(List<TPS> tpsList, Database database) {
        this.tpsList = tpsList;
        this.database = database;
    }

    @Override
    public void run() {
        List<TPS> history = tpsList;
        long lastDate = history.get(history.size() - 1).getDate();
        double averageTPS = history.stream().mapToDouble(TPS::getTicksPerSecond).average().orElse(0);
        int peakPlayersOnline = history.stream().mapToInt(TPS::getPlayers).max().orElse(0);
        double averageCPUUsage = history.stream().mapToDouble(TPS::getCPUUsage).average().orElse(0);
        long averageUsedMemory = (long) history.stream().mapToLong(TPS::getUsedMemory).average().orElse(0);
        int averageEntityCount = (int) history.stream().mapToInt(TPS::getEntityCount).average().orElse(0);
        int averageChunksLoaded = (int) history.stream().mapToInt(TPS::getChunksLoaded).average().orElse(0);
        long freeDiskSpace = (long) history.stream().mapToLong(TPS::getFreeDiskSpace).average().orElse(0);

        TPS tps = TPSBuilder.get()
                .date(lastDate)
                .tps(averageTPS)
                .playersOnline(peakPlayersOnline)
                .usedCPU(averageCPUUsage)
                .usedMemory(averageUsedMemory)
                .entities(averageEntityCount)
                .chunksLoaded(averageChunksLoaded)
                .freeDiskSpace(freeDiskSpace)
                .toTPS();

        database.save().insertTPSforThisServer(tps);
    }
}
