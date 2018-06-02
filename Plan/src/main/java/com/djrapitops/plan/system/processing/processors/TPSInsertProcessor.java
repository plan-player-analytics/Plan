/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.CriticalRunnable;
import com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.List;

/**
 * Processes 60s average of a TPS list.
 *
 * @author Rsl1122
 */
public class TPSInsertProcessor implements CriticalRunnable {

    private final List<TPS> tpsList;

    public TPSInsertProcessor(List<TPS> tpsList) {
        this.tpsList = tpsList;
    }

    @Override
    public void run() {
        List<TPS> history = tpsList;
        final long lastDate = history.get(history.size() - 1).getDate();
        final double averageTPS = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getTicksPerSecond)));
        final int peakPlayersOnline = history.stream().mapToInt(TPS::getPlayers).max().orElse(0);
        final double averageCPUUsage = MathUtils.round(MathUtils.averageDouble(history.stream().map(TPS::getCPUUsage)));
        final long averageUsedMemory = MathUtils.averageLong(history.stream().map(TPS::getUsedMemory));
        final int averageEntityCount = (int) MathUtils.averageInt(history.stream().map(TPS::getEntityCount));
        final int averageChunksLoaded = (int) MathUtils.averageInt(history.stream().map(TPS::getChunksLoaded));

        TPS tps = TPSBuilder.get()
                .date(lastDate)
                .tps(averageTPS)
                .playersOnline(peakPlayersOnline)
                .usedCPU(averageCPUUsage)
                .usedMemory(averageUsedMemory)
                .entities(averageEntityCount)
                .chunksLoaded(averageChunksLoaded)
                .toTPS();

        Database.getActive().save().insertTPSforThisServer(tps);
    }
}