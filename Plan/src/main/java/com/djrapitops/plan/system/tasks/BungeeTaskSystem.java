/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.system.tasks.bungee.BungeeTPSCountTimer;
import com.djrapitops.plan.system.tasks.bungee.EnableConnectionTask;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import com.google.inject.Inject;

/**
 * TaskSystem responsible for registering tasks for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeTaskSystem extends TaskSystem {

    @Inject
    public BungeeTaskSystem(RunnableFactory runnableFactory) {
        super(runnableFactory, new BungeeTPSCountTimer());
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
