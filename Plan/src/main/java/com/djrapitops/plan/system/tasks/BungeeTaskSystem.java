/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.proxy.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.proxy.bungee.BungeeTPSCountTimer;
import com.djrapitops.plan.system.tasks.proxy.bungee.PingCountTimerBungee;
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

    private final PlanBungee plugin;
    private final PlanConfig config;
    private final NetworkPageRefreshTask networkPageRefreshTask;
    private final PingCountTimerBungee pingCountTimer;
    private final LogsFolderCleanTask logsFolderCleanTask;
    private final PlayersPageRefreshTask playersPageRefreshTask;

    @Inject
    public BungeeTaskSystem(
            PlanBungee plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            BungeeTPSCountTimer bungeeTPSCountTimer,
            NetworkPageRefreshTask networkPageRefreshTask,
            PingCountTimerBungee pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask) {
        super(runnableFactory, bungeeTPSCountTimer);
        this.plugin = plugin;
        this.config = config;

        this.networkPageRefreshTask = networkPageRefreshTask;
        this.pingCountTimer = pingCountTimer;
        this.logsFolderCleanTask = logsFolderCleanTask;
        this.playersPageRefreshTask = playersPageRefreshTask;
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

        plugin.registerListener(pingCountTimer);
        long startDelay = TimeAmount.toTicks(config.getNumber(Settings.PING_SERVER_ENABLE_DELAY), TimeUnit.SECONDS);
        runnableFactory.create("PingCountTimer", pingCountTimer).runTaskTimer(startDelay, PingCountTimerBungee.PING_INTERVAL);

        registerTask(playersPageRefreshTask)
                .runTaskTimerAsynchronously(TimeAmount.toTicks(5L, TimeUnit.MINUTES), TimeAmount.toTicks(5L, TimeUnit.MINUTES));
    }
}
