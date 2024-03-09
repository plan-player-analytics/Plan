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
import com.djrapitops.plan.utilities.analysis.Maximum;
import com.djrapitops.plan.utilities.analysis.TimerAverage;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * TPSCounter extension for game server platforms.
 *
 * @author AuroraLS3
 */
@Singleton
public class ProxyTPSCounter extends TPSCounter {

    private final ServerSensor<?> serverSensor;
    private final SystemUsageBuffer systemUsage;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Maximum.ForInteger playersOnline;
    private final Average cpu;
    private final TimerAverage ram;

    @Inject
    public ProxyTPSCounter(
            ServerSensor<?> serverSensor,
            SystemUsageBuffer systemUsage,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(logger, errorLogger);

        this.serverSensor = serverSensor;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.systemUsage = systemUsage;
        playersOnline = new Maximum.ForInteger(0);
        cpu = new Average();
        ram = new TimerAverage();
    }

    @Override
    public void pulse() {
        long time = System.currentTimeMillis();
        boolean shouldSave = ram.add(time, systemUsage.getRam());
        playersOnline.add(serverSensor.getOnlinePlayerCount());
        cpu.add(systemUsage.getCpu());
        if (shouldSave) save(time);
    }

    private void save(long time) {
        long timeLastMinute = time - TimeUnit.MINUTES.toMillis(1L);
        int maxPlayers = playersOnline.getMaxAndReset();
        double averageCPU = cpu.getAverageAndReset();
        long averageRAM = (long) ram.getAverageAndReset(time);
        long freeDiskSpace = systemUsage.getFreeDiskSpace();

        dbSystem.getDatabase().executeTransaction(new TPSStoreTransaction(
                logger,
                serverInfo.getServerUUID(),
                TPSBuilder.get()
                        .date(timeLastMinute)
                        .playersOnline(maxPlayers)
                        .usedCPU(averageCPU)
                        .usedMemory(averageRAM)
                        .freeDiskSpace(freeDiskSpace)
                        .toTPS()
        ));
    }
}
