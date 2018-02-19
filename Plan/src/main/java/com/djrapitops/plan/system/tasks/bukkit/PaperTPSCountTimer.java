package com.djrapitops.plan.system.tasks.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import org.bukkit.World;

public class PaperTPSCountTimer extends BukkitTPSCountTimer {

    public PaperTPSCountTimer(Plan plugin) {
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

        tps = MathUtils.round(tps);

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
