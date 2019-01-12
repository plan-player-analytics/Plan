/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.tasks.bungee.BungeeTPSCountTimer;
import com.djrapitops.plan.system.tasks.bungee.PingCountTimerBungee;
import com.djrapitops.plan.system.tasks.proxy.NetworkConfigStoreTask;
import com.djrapitops.plan.system.tasks.proxy.NetworkPageRefreshTask;
import com.djrapitops.plugin.api.TimeAmount;
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
    private final NetworkConfigStoreTask networkConfigStoreTask;

    @Inject
    public BungeeTaskSystem(
            PlanBungee plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            BungeeTPSCountTimer bungeeTPSCountTimer,
            NetworkPageRefreshTask networkPageRefreshTask,
            PingCountTimerBungee pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask,
            NetworkConfigStoreTask networkConfigStoreTask
    ) {
        super(runnableFactory, bungeeTPSCountTimer);
        this.plugin = plugin;
        this.config = config;

        this.networkPageRefreshTask = networkPageRefreshTask;
        this.pingCountTimer = pingCountTimer;
        this.logsFolderCleanTask = logsFolderCleanTask;
        this.playersPageRefreshTask = playersPageRefreshTask;
        this.networkConfigStoreTask = networkConfigStoreTask;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        registerTask(tpsCountTimer).runTaskTimerAsynchronously(1000, TimeAmount.toTicks(1L, TimeUnit.SECONDS));
        registerTask(networkPageRefreshTask).runTaskTimerAsynchronously(1500, TimeAmount.toTicks(5L, TimeUnit.MINUTES));
        registerTask(logsFolderCleanTask).runTaskLaterAsynchronously(TimeAmount.toTicks(30L, TimeUnit.SECONDS));

        plugin.registerListener(pingCountTimer);
        long startDelay = TimeAmount.toTicks(config.get(TimeSettings.PING_SERVER_ENABLE_DELAY), TimeUnit.MILLISECONDS);
        runnableFactory.create("PingCountTimer", pingCountTimer).runTaskTimer(startDelay, PingCountTimerBungee.PING_INTERVAL);

        registerTask(playersPageRefreshTask)
                .runTaskTimerAsynchronously(TimeAmount.toTicks(5L, TimeUnit.MINUTES), TimeAmount.toTicks(5L, TimeUnit.MINUTES));

        // +40 ticks / 2 seconds so that update check task runs first.
        long storeDelay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        registerTask("Config Store Task", networkConfigStoreTask).runTaskLaterAsynchronously(storeDelay);
    }
}
