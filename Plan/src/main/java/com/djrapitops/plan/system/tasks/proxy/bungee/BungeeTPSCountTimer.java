package com.djrapitops.plan.system.tasks.proxy.bungee;

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
public class BungeeTPSCountTimer extends TPSCountTimer {

    private final ServerProperties serverProperties;

    @Inject
    public BungeeTPSCountTimer(
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
