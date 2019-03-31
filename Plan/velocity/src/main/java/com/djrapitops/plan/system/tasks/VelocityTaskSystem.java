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

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.db.tasks.DBCleanTask;
import com.djrapitops.plan.extension.ExtensionServerMethodCallerTask;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.tasks.proxy.NetworkConfigStoreTask;
import com.djrapitops.plan.system.tasks.proxy.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.velocity.PingCountTimerVelocity;
import com.djrapitops.plan.system.tasks.velocity.VelocityTPSCountTimer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * TaskSystem responsible for registering tasks for Velocity.
 *
 * @author Rsl1122
 */
@Singleton
public class VelocityTaskSystem extends TaskSystem {

    private final PlanVelocity plugin;
    private final PlanConfig config;
    private final NetworkPageRefreshTask networkPageRefreshTask;
    private final PingCountTimerVelocity pingCountTimer;
    private final LogsFolderCleanTask logsFolderCleanTask;
    private final PlayersPageRefreshTask playersPageRefreshTask;
    private final NetworkConfigStoreTask networkConfigStoreTask;
    private final DBCleanTask dbCleanTask;
    private final ExtensionServerMethodCallerTask extensionServerMethodCallerTask;

    @Inject
    public VelocityTaskSystem(
            PlanVelocity plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            VelocityTPSCountTimer velocityTPSCountTimer,
            NetworkPageRefreshTask networkPageRefreshTask,
            PingCountTimerVelocity pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask,
            NetworkConfigStoreTask networkConfigStoreTask,
            DBCleanTask dbCleanTask,
            ExtensionServerMethodCallerTask extensionServerMethodCallerTask
    ) {
        super(runnableFactory, velocityTPSCountTimer);
        this.plugin = plugin;
        this.config = config;

        this.networkPageRefreshTask = networkPageRefreshTask;
        this.pingCountTimer = pingCountTimer;
        this.logsFolderCleanTask = logsFolderCleanTask;
        this.playersPageRefreshTask = playersPageRefreshTask;
        this.networkConfigStoreTask = networkConfigStoreTask;
        this.dbCleanTask = dbCleanTask;
        this.extensionServerMethodCallerTask = extensionServerMethodCallerTask;
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
        registerTask(pingCountTimer).runTaskTimer(startDelay, PingCountTimerVelocity.PING_INTERVAL);

        registerTask(playersPageRefreshTask).runTaskTimerAsynchronously(TimeAmount.toTicks(5L, TimeUnit.MINUTES), TimeAmount.toTicks(5L, TimeUnit.MINUTES));

        // +40 ticks / 2 seconds so that update check task runs first.
        long storeDelay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        registerTask(networkConfigStoreTask).runTaskLaterAsynchronously(storeDelay);

        registerTask(dbCleanTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(20, TimeUnit.SECONDS),
                TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS)
        );

        long extensionRefreshPeriod = TimeAmount.toTicks(config.get(TimeSettings.EXTENSION_DATA_REFRESH_PERIOD), TimeUnit.MILLISECONDS);
        registerTask(extensionServerMethodCallerTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(30, TimeUnit.SECONDS), extensionRefreshPeriod
        );
    }
}
