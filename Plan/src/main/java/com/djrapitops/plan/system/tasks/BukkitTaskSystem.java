/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.tasks.server.BukkitTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PaperTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.PingCountTimer;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitTaskSystem extends ServerTaskSystem {

    public BukkitTaskSystem(Plan plugin) {
        super(plugin,
                Check.isPaperAvailable()
                        ? new PaperTPSCountTimer(plugin)
                        : new BukkitTPSCountTimer(plugin)
        );
    }

    @Override
    public void enable() {
        super.enable();
        if (Check.isSpigotAvailable()) {
            PingCountTimer pingCountTimer = new PingCountTimer();
            ((Plan) plugin).registerListener(pingCountTimer);
            RunnableFactory.createNew("PingCountTimer", pingCountTimer)
                    .runTaskTimer(20L, PingCountTimer.PING_INTERVAL);
        }
    }

    @Override
    public void disable() {
        super.disable();
        Bukkit.getScheduler().cancelTasks((Plan) plugin);
    }
}
