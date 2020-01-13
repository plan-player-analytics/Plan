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

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.gathering.SystemUsage;
import com.djrapitops.plan.gathering.domain.builders.TPSBuilder;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.TPSStoreTransaction;
import com.djrapitops.plan.utilities.analysis.Maximum;
import com.djrapitops.plan.utilities.analysis.TimerAverager;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.spongepowered.api.world.World;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SpongeTPSCounter extends TPSCounter {

    private final PlanSponge plugin;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerProperties serverProperties;

    private TimerAverager tps;
    private Maximum.ForInteger playersOnline;
    private TimerAverager cpu;
    private TimerAverager ram;

    @Inject
    public SpongeTPSCounter(
            PlanSponge plugin,
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

        tps = new TimerAverager();
        playersOnline = new Maximum.ForInteger(0);
        cpu = new TimerAverager();
        ram = new TimerAverager();
    }

    @Override
    public void pulse() {
        boolean shouldSave = tps.add(plugin.getGame().getServer().getTicksPerSecond());
        playersOnline.add(getOnlinePlayerCount());
        cpu.add(SystemUsage.getAverageSystemLoad());
        ram.add(SystemUsage.getUsedMemory());
        if (shouldSave) save();
    }

    private void save() {
        long time = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1L);
        double averageTPS = tps.getAverageAndReset();
        int maxPlayers = playersOnline.getMaxAndReset();
        double averageCPU = cpu.getAverageAndReset();
        long averageRAM = (long) ram.getAverageAndReset();
        int entityCount = getEntityCount();
        int chunkCount = -1;
        long freeDiskSpace = getFreeDiskSpace();

        dbSystem.getDatabase().executeTransaction(new TPSStoreTransaction(
                serverInfo.getServerUUID(),
                TPSBuilder.get()
                        .date(time)
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

    /**
     * Gets the amount of loaded chunks
     *
     * @return amount of loaded chunks
     */
    private int getLoadedChunks() {
        // DISABLED
        int loaded = 0;
        for (World world : plugin.getGame().getServer().getWorlds()) {
            loaded += world.getLoadedChunks().spliterator().estimateSize();
        }
        return loaded;
    }

    /**
     * Gets the amount of entities on the server
     *
     * @return amount of entities
     */
    private int getEntityCount() {
        int sum = 0;
        for (World world : plugin.getGame().getServer().getWorlds()) {
            sum += world.getEntities().size();
        }
        return sum;
    }
}
