package com.djrapitops.plan.system.tasks.server.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.builders.TPSBuilder;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.World;

import javax.inject.Inject;

public class PaperTPSCountTimer extends BukkitTPSCountTimer {

    @Inject
    public PaperTPSCountTimer(
            Plan plugin,
            Processors processors,
            Processing processing,
            ServerProperties serverProperties,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        super(plugin, processors, processing, serverProperties, logger, errorHandler);
    }

    @Override
    protected TPS getTPS(long diff, long now, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded, int playersOnline, long freeDiskSpace) {
        double tps;
        try {
            tps = plugin.getServer().getTPS()[0];
        } catch (NoSuchMethodError e) {
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
