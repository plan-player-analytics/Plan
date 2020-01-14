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

import cn.nukkit.level.Level;
import com.djrapitops.plan.PlanNukkit;
import com.djrapitops.plan.gathering.SystemUsage;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.analysis.Average;
import com.djrapitops.plan.utilities.analysis.Maximum;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Singleton
public class NukkitTPSCounter extends TPSCounter {

    protected final PlanNukkit plugin;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private ServerProperties serverProperties;

    private TPSCalculator tps;
    private Maximum.ForInteger playersOnline;
    private Average cpu;
    private Average ram;

    @Inject
    public NukkitTPSCounter(
            PlanNukkit plugin,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(logger, errorHandler);
        this.plugin = plugin;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.serverProperties = serverInfo.getServerProperties();

        tps = new TPSCalculator();
        playersOnline = new Maximum.ForInteger(0);
        cpu = new Average();
        ram = new Average();
    }

    @Override
    public void pulse() {
        long time = System.currentTimeMillis();
        Optional<Double> result = tps.pulse(time);
        playersOnline.add(getOnlinePlayerCount());
        cpu.add(SystemUsage.getAverageSystemLoad());
        ram.add(SystemUsage.getUsedMemory());
        result.ifPresent(averageTPS -> save(averageTPS, time));
    }

    private void save(double averageTPS, long time) {
        long timeLastMinute = time - TimeUnit.MINUTES.toMillis(1L);
        int maxPlayers = playersOnline.getMaxAndReset();
        double averageCPU = cpu.getAverageAndReset();
        long averageRAM = (long) ram.getAverageAndReset();
        int entityCount = getEntityCount();
        int chunkCount = getLoadedChunks();
        long freeDiskSpace = getFreeDiskSpace();

        dbSystem.getDatabase().executeTransaction(new TPSStoreTransaction(
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
                        .toTPS()
        ));
    }

    private int getOnlinePlayerCount() {
        return serverProperties.getOnlinePlayers();
    }

    private int getLoadedChunks() {
        int sum = 0;
        for (Level world : plugin.getServer().getLevels().values()) {
            sum += world.getChunks().size();
        }
        return sum;
    }

    protected int getEntityCount() {
        int sum = 0;
        for (Level world : plugin.getServer().getLevels().values()) {
            sum += world.getEntities().length;
        }
        return sum;
    }
}
