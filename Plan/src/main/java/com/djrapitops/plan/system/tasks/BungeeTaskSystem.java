/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.tasks.bukkit.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.bungee.BungeeTPSCountTimer;
import com.djrapitops.plan.system.tasks.bungee.EnableConnectionTask;
import com.djrapitops.plugin.api.TimeAmount;

/**
 * TaskSystem responsible for registering tasks for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeTaskSystem extends TaskSystem {

    public BungeeTaskSystem(PlanBungee plugin) {
        tpsCountTimer = new BungeeTPSCountTimer(plugin);
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        registerTask(new EnableConnectionTask()).runTaskAsynchronously();
        registerTask(tpsCountTimer).runTaskTimerAsynchronously(1000, TimeAmount.SECOND.ticks());
        registerTask(new NetworkPageRefreshTask()).runTaskTimerAsynchronously(1500, TimeAmount.MINUTE.ticks());
    }
}