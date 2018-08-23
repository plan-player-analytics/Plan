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

import javax.inject.Inject;

/**
 * TaskSystem responsible for registering tasks for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeTaskSystem extends TaskSystem {

    private final EnableConnectionTask enableConnectionTask;
    private final NetworkPageRefreshTask networkPageRefreshTask;

    @Inject
    public BungeeTaskSystem(
            RunnableFactory runnableFactory,
            BungeeTPSCountTimer bungeeTPSCountTimer,
            EnableConnectionTask enableConnectionTask,
            NetworkPageRefreshTask networkPageRefreshTask
    ) {
        super(runnableFactory, bungeeTPSCountTimer);

        this.enableConnectionTask = enableConnectionTask;
        this.networkPageRefreshTask = networkPageRefreshTask;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        registerTask(enableConnectionTask).runTaskAsynchronously();
        registerTask(tpsCountTimer).runTaskTimerAsynchronously(1000, TimeAmount.SECOND.ticks());
        registerTask(networkPageRefreshTask).runTaskTimerAsynchronously(1500, TimeAmount.MINUTE.ticks());
    }
}
