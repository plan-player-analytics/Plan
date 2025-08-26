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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.analysis.Average;
import com.djrapitops.plan.utilities.analysis.Distribution;
import com.djrapitops.plan.utilities.analysis.Maximum;
import com.djrapitops.plan.utilities.analysis.TimerAverage;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * TPSCounter extension for game server platforms.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerTPSCounter<W> extends TPSCounter {

    private final boolean noDirectTPS;
    private final ServerSensor<W> serverSensor;
    private final SystemUsageBuffer systemUsage;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private TPSCalculator indirectTPS;
    private TimerAverage directTPS;
    private final Maximum.ForInteger playersOnline;
    private final Average cpu;
    private final Average ram;
    private final Average mspt;
    private final Distribution msptDistribution;

    private int pulseCounter = 0;

    @Inject
    public ServerTPSCounter(
            ServerSensor<W> serverSensor,
            SystemUsageBuffer systemUsage,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(logger, errorLogger);

        noDirectTPS = !serverSensor.supportsDirectTPS();
        this.serverSensor = serverSensor;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.systemUsage = systemUsage;
        if (noDirectTPS) {
            indirectTPS = new TPSCalculator();
        } else {
            directTPS = new TimerAverage();
        }
        playersOnline = new Maximum.ForInteger(0);
        cpu = new Average();
        ram = new Average();
        mspt = new Average();
        msptDistribution = new Distribution();
    }

    @Override
    public void pulse() {
        long time = System.currentTimeMillis();
        Optional<Double> result = pulseTPS(time);
        playersOnline.add(serverSensor.getOnlinePlayerCount());
        cpu.add(systemUsage.getCpu());
        ram.add(systemUsage.getRam());

        // TPSCounter is pulsed once every 20 ticks, so this should prevent duplicate values.
        if (pulseCounter > 0 && pulseCounter % 5 == 0) {
            serverSensor.getMspt().ifPresent(last100ticks -> {
                mspt.addPositive(last100ticks);
                msptDistribution.addPositive(last100ticks);
            });
        }
        result.ifPresent(tps -> save(tps, time));
        pulseCounter++;
    }

    private void save(double averageTPS, long time) {
        long timeLastMinute = time - TimeUnit.MINUTES.toMillis(1L);
        int maxPlayers = playersOnline.getMaxAndReset();
        double averageCPU = cpu.getAverageAndReset();
        long averageRAM = (long) ram.getAverageAndReset();
        int entityCount = 0;
        int chunkCount = 0;
        for (W world : serverSensor.getWorlds()) {
            entityCount += serverSensor.getEntityCount(world);
            chunkCount += serverSensor.getChunkCount(world);
        }
        long freeDiskSpace = systemUsage.getFreeDiskSpace();
        Double msptAverage = mspt.getAverageAndReset();
        if (msptAverage <= 0) msptAverage = null;
        Double mspt95thPercentile = msptDistribution.getNthPercentile(0.95).orElse(null);

        dbSystem.getDatabase().executeTransaction(new TPSStoreTransaction(
                logger,
                serverInfo.getServerUUID(),
                TPSBuilder.get()
                        .date(timeLastMinute)
                        .tps(averageTPS)
                        .playersOnline(maxPlayers)
                        .usedCPU(averageCPU)
                        .usedMemory(averageRAM)
                        .entities(entityCount)
                        .chunksLoaded(chunkCount)
                        .freeDiskSpace(freeDiskSpace)
                        .msptAverage(msptAverage)
                        .mspt95thPercentile(mspt95thPercentile)
                        .toTPS()
        ));
    }

    public Optional<Double> pulseTPS(long time) {
        if (noDirectTPS) {
            return indirectTPS.pulse(time);
        } else {
            if (directTPS.add(time, serverSensor.getTPS())) {
                return Optional.of(directTPS.getAverageAndReset(time));
            } else {
                return Optional.empty();
            }
        }
    }
}
