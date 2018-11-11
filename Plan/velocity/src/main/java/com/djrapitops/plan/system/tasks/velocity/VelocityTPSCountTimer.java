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
package com.djrapitops.plan.system.tasks.velocity;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.tasks.TPSCountTimer;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VelocityTPSCountTimer extends TPSCountTimer {

    private final ServerProperties serverProperties;

    @Inject
    public VelocityTPSCountTimer(
            Processors processors,
            Processing processing,
            ServerProperties serverProperties,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(processors, processing, logger, errorHandler);
        this.serverProperties = serverProperties;
    }

    @Override
    public void addNewTPSEntry(long nanoTime, long now) {
        int onlineCount = serverProperties.getOnlinePlayers();
        TPS tps = TPSBuilder.get()
                .date(now)
                .skipTPS()
                .playersOnline(onlineCount)
                .usedCPU(getCPUUsage())
                .usedMemory(getUsedMemory())
                .entities(-1)
                .chunksLoaded(-1)
                .freeDiskSpace(getFreeDiskSpace())
                .toTPS();

        history.add(tps);
        latestPlayersOnline = onlineCount;
    }
}
