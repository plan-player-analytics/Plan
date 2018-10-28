/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.tasks.server.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.tasks.TPSCountTimer;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.World;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class BukkitTPSCountTimer extends TPSCountTimer {

    protected final Plan plugin;
    private ServerProperties serverProperties;
    private long lastCheckNano;

    @Inject
    public BukkitTPSCountTimer(
            Plan plugin,
            Processors processors,
            Processing processing,
            ServerProperties serverProperties,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(processors, processing, logger, errorHandler);
        this.plugin = plugin;
        this.serverProperties = serverProperties;
        lastCheckNano = -1;
    }

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        long diff = nanoTime - lastCheckNano;

        lastCheckNano = nanoTime;

        if (diff > nanoTime) { // First run's diff = nanoTime + 1, no calc possible.
            logger.debug("First run of TPSCountTimer Task.");
            return;
        }

        history.add(calculateTPS(diff, now));
    }

    /**
     * Calculates the TPS
     *
     * @param diff The time difference between the last run and the new run
     * @param now  The time right now
     * @return the TPS
     */
    private TPS calculateTPS(long diff, long now) {
        double averageCPUUsage = getCPUUsage();
        long usedMemory = getUsedMemory();
        long freeDiskSpace = getFreeDiskSpace();

        int playersOnline = serverProperties.getOnlinePlayers();
        latestPlayersOnline = playersOnline;
        int loadedChunks = getLoadedChunks();
        int entityCount = getEntityCount();

        return getTPS(diff, now, averageCPUUsage, usedMemory, entityCount, loadedChunks, playersOnline, freeDiskSpace);
    }

    protected TPS getTPS(long diff, long now,
                         double cpuUsage, long usedMemory,
                         int entityCount, int chunksLoaded,
                         int playersOnline, long freeDiskSpace) {
        long difference = diff;
        if (difference < TimeUnit.SECONDS.toNanos(1L)) { // No tick count above 20
            difference = TimeUnit.SECONDS.toNanos(1L);
        }

        long twentySeconds = TimeUnit.SECONDS.toNanos(20L);
        while (difference > twentySeconds) {
            // Add 0 TPS since more than 20 ticks has passed.
            history.add(TPSBuilder.get()
                    .date(now)
                    .tps(0)
                    .playersOnline(playersOnline)
                    .usedCPU(cpuUsage)
                    .usedMemory(usedMemory)
                    .entities(entityCount)
                    .chunksLoaded(chunksLoaded)
                    .freeDiskSpace(freeDiskSpace)
                    .toTPS());
            difference -= twentySeconds;
        }

        double tpsN = twentySeconds * 1.0 / difference;

        return TPSBuilder.get()
                .date(now)
                .tps(0)
                .playersOnline(playersOnline)
                .usedCPU(cpuUsage)
                .usedMemory(usedMemory)
                .entities(entityCount)
                .chunksLoaded(chunksLoaded)
                .freeDiskSpace(freeDiskSpace)
                .toTPS();
    }

    /**
     * Gets the amount of loaded chunks
     *
     * @return amount of loaded chunks
     */
    private int getLoadedChunks() {
        int sum = 0;
        for (World world : plugin.getServer().getWorlds()) {
            sum += world.getLoadedChunks().length;
        }
        return sum;
    }

    /**
     * Gets the amount of entities on the server for Bukkit / Spigot
     *
     * @return amount of entities
     */
    protected int getEntityCount() {
        int sum = 0;
        for (World world : plugin.getServer().getWorlds()) {
            sum += world.getEntities().size();
        }
        return sum;
    }
}
