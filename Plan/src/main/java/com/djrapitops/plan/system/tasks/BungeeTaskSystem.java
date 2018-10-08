/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.bungee.BungeeTPSCountTimer;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * TaskSystem responsible for registering tasks for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeTaskSystem extends TaskSystem {

    private final PlanConfig config;
    private final NetworkPageRefreshTask networkPageRefreshTask;
    private final LogsFolderCleanTask logsFolderCleanTask;

    @Inject
    public BungeeTaskSystem(
            PlanConfig config,
            RunnableFactory runnableFactory,
            BungeeTPSCountTimer bungeeTPSCountTimer,
            NetworkPageRefreshTask networkPageRefreshTask,
            LogsFolderCleanTask logsFolderCleanTask
    ) {
        super(runnableFactory, bungeeTPSCountTimer);
        this.config = config;

        this.networkPageRefreshTask = networkPageRefreshTask;
        this.logsFolderCleanTask = logsFolderCleanTask;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        registerTask(tpsCountTimer).runTaskTimerAsynchronously(1000, TimeAmount.toTicks(1L, TimeUnit.SECONDS));
        registerTask(networkPageRefreshTask).runTaskTimerAsynchronously(1500, TimeAmount.toTicks(5L, TimeUnit.MINUTES));
        registerTask(logsFolderCleanTask).runTaskLaterAsynchronously(TimeAmount.toTicks(30L, TimeUnit.SECONDS));
        registerTask("Settings Save", new AbsRunnable() {
            @Override
            public void run() {
                config.getNetworkSettings().placeSettingsToDB();
            }
        }).runTaskAsynchronously();
    }
}
