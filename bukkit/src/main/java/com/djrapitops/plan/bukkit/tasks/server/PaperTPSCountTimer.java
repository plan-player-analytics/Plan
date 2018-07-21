package com.djrapitops.plan.bukkit.tasks.server;

import com.djrapitops.plan.bukkit.PlanBukkit;
import com.djrapitops.plan.data.container.TPS;
import org.bukkit.World;

public class PaperTPSCountTimer extends BukkitTPSCountTimer {

    public PaperTPSCountTimer(PlanBukkit plugin) {
        super(plugin);
    }

    @Override
    protected TPS getTPS(long diff, long now, double cpuUsage, long usedMemory, int entityCount, int chunksLoaded, int playersOnline) {
        double tps;
        try {
            tps = plugin.getServer().getTPS()[0];
        } catch (NoSuchMethodError e) {
            return super.getTPS(diff, now, cpuUsage, usedMemory, entityCount, chunksLoaded, playersOnline);
        }

        if (tps > 20) {
            tps = 20;
        }

        return new TPS(now, tps, playersOnline, cpuUsage, usedMemory, entityCount, chunksLoaded);
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
