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

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.gathering.SystemUsage;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.analysis.Average;
import com.djrapitops.plan.utilities.analysis.Maximum;
import com.djrapitops.plan.utilities.analysis.TimerAverage;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.World;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class PaperTPSCounter extends TPSCounter {

    private final Plan plugin;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerProperties serverProperties;

    private TimerAverage tps;
    private Maximum.ForInteger playersOnline;
    private Average cpu;
    private Average ram;

    @Inject
    public PaperTPSCounter(
            Plan plugin,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(logger, errorHandler);
        this.plugin = plugin;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        serverProperties = serverInfo.getServerProperties();

        tps = new TimerAverage();
        playersOnline = new Maximum.ForInteger(0);
        cpu = new Average();
        ram = new Average();
    }

    @Override
    public void pulse() {
        long time = System.currentTimeMillis();
        boolean shouldSave = tps.add(time, plugin.getServer().getTPS()[0]);
        playersOnline.add(getOnlinePlayerCount());
        cpu.add(SystemUsage.getAverageSystemLoad());
        ram.add(SystemUsage.getUsedMemory());
        if (shouldSave) save(time);
    }

    private void save(long time) {
        long timeLastMinute = time - TimeUnit.MINUTES.toMillis(1L);
        double averageTPS = tps.getAverageAndReset(time);
        int maxPlayers = playersOnline.getMaxAndReset();
        double averageCPU = cpu.getAverageAndReset();
        long averageRAM = (long) ram.getAverageAndReset();
        int entityCount = 0;
        int chunkCount = 0;
        for (World world : plugin.getServer().getWorlds()) {
            entityCount += getEntityCount(world);
            chunkCount += getChunkCount(world);
        }
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

    private int getChunkCount(World world) {
        try {
            return getChunkCountPaperWay(world);
        } catch (BootstrapMethodError | NoSuchMethodError e) {
            return getChunkCountSpigotWay(world);
        }
    }

    private int getChunkCountSpigotWay(World world) {
        return world.getLoadedChunks().length;
    }

    private int getChunkCountPaperWay(World world) {
        return world.getChunkCount();
    }

    private int getEntityCount(World world) {
        try {
            return getEntitiesPaperWay(world);
        } catch (BootstrapMethodError | NoSuchMethodError e) {
            return getEntitiesSpigotWay(world);
        }
    }

    private int getEntitiesSpigotWay(World world) {
        return world.getEntities().size();
    }

    private int getEntitiesPaperWay(World world) {
        return world.getEntityCount();
    }
}
