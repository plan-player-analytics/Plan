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
package com.djrapitops.plan.system.gathering.timed;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.identification.ServerInfo;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.World;

import javax.inject.Inject;

public class PaperTPSCounter extends BukkitTPSCounter {

    @Inject
    public PaperTPSCounter(
            Plan plugin,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(plugin, dbSystem, serverInfo, serverInfo.getServerProperties(), logger, errorHandler);
    }

    @Override
    protected TPS getTPS(long diff, long now, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded, int playersOnline, long freeDiskSpace) {
        double tps;
        try {
            tps = plugin.getServer().getTPS()[0];
        } catch (NoSuchMethodError e) {
            // This method is from Paper
            return super.getTPS(diff, now, cpuUsage, usedMemory, entityCount, chunksLoaded, playersOnline, freeDiskSpace);
        }

        if (tps > 20) {
            tps = 20;
        }

        return TPSBuilder.get()
                .date(now)
                .tps(tps)
                .playersOnline(playersOnline)
                .usedCPU(cpuUsage)
                .usedMemory(usedMemory)
                .entities(entityCount)
                .chunksLoaded(chunksLoaded)
                .freeDiskSpace(freeDiskSpace)
                .toTPS();
    }

    @Override
    protected int getEntityCount() {
        try {
            return plugin.getServer().getWorlds().stream().mapToInt(World::getEntityCount).sum();
        } catch (BootstrapMethodError | NoSuchMethodError e) {
            return super.getEntityCount();
        }
    }
}
