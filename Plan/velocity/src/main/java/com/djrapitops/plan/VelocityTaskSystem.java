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
package com.djrapitops.plan;

import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.extension.ExtensionServerMethodCallerTask;
import com.djrapitops.plan.gathering.timed.ProxyTPSCounter;
import com.djrapitops.plan.gathering.timed.SystemUsageBuffer;
import com.djrapitops.plan.gathering.timed.TPSCounter;
import com.djrapitops.plan.gathering.timed.VelocityPingCounter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.upkeep.NetworkConfigStoreTask;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import com.djrapitops.plan.storage.upkeep.LogsFolderCleanTask;
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
    private final TPSCounter tpsCounter;
    private final VelocityPingCounter pingCounter;
    private final LogsFolderCleanTask logsFolderCleanTask;
    private final NetworkConfigStoreTask networkConfigStoreTask;
    private final DBCleanTask dbCleanTask;
    private final JSONCache.CleanTask jsonCacheCleanTask;
    private final ExtensionServerMethodCallerTask extensionServerMethodCallerTask;
    private final SystemUsageBuffer.RamAndCpuTask ramAndCpuTask;
    private final SystemUsageBuffer.DiskTask diskTask;

    @Inject
    public VelocityTaskSystem(
            PlanVelocity plugin,
            PlanConfig config,
            RunnableFactory runnableFactory,
            ProxyTPSCounter tpsCounter,
            VelocityPingCounter pingCounter,
            LogsFolderCleanTask logsFolderCleanTask,
            NetworkConfigStoreTask networkConfigStoreTask,
            DBCleanTask dbCleanTask,
            JSONCache.CleanTask jsonCacheCleanTask,
            ExtensionServerMethodCallerTask extensionServerMethodCallerTask,
            SystemUsageBuffer.RamAndCpuTask ramAndCpuTask,
            SystemUsageBuffer.DiskTask diskTask
    ) {
        super(runnableFactory);
        this.plugin = plugin;
        this.config = config;
        this.tpsCounter = tpsCounter;
        this.pingCounter = pingCounter;
        this.logsFolderCleanTask = logsFolderCleanTask;
        this.networkConfigStoreTask = networkConfigStoreTask;
        this.dbCleanTask = dbCleanTask;
        this.jsonCacheCleanTask = jsonCacheCleanTask;
        this.extensionServerMethodCallerTask = extensionServerMethodCallerTask;
        this.ramAndCpuTask = ramAndCpuTask;
        this.diskTask = diskTask;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTPSCounter() {
        long halfSecondTicks = TimeAmount.toTicks(500L, TimeUnit.MILLISECONDS);
        long secondTicks = TimeAmount.toTicks(1L, TimeUnit.SECONDS);
        long minuteTicks = TimeAmount.toTicks(1L, TimeUnit.MINUTES);
        registerTask(tpsCounter).runTaskTimer(minuteTicks, secondTicks);
        registerTask(ramAndCpuTask).runTaskTimerAsynchronously(minuteTicks - halfSecondTicks, secondTicks);
        registerTask(diskTask).runTaskTimerAsynchronously(50L * secondTicks, minuteTicks);
    }

    private void registerTasks() {
        registerTPSCounter();
        registerTask(logsFolderCleanTask).runTaskLaterAsynchronously(TimeAmount.toTicks(30L, TimeUnit.SECONDS));

        Long pingDelay = config.get(TimeSettings.PING_SERVER_ENABLE_DELAY);
        if (pingDelay < TimeUnit.HOURS.toMillis(1L) && config.isTrue(DataGatheringSettings.PING)) {
            plugin.registerListener(pingCounter);
            long startDelay = TimeAmount.toTicks(pingDelay, TimeUnit.MILLISECONDS);
            registerTask(pingCounter).runTaskTimer(startDelay, 40L);
        }

        // +40 ticks / 2 seconds so that update check task runs first.
        long storeDelay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        registerTask(networkConfigStoreTask).runTaskLaterAsynchronously(storeDelay);

        registerTask(dbCleanTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(20, TimeUnit.SECONDS),
                TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS)
        );
        long minute = TimeAmount.toTicks(1, TimeUnit.MINUTES);
        registerTask(jsonCacheCleanTask).runTaskTimerAsynchronously(minute, minute);

        long extensionRefreshPeriod = TimeAmount.toTicks(config.get(TimeSettings.EXTENSION_DATA_REFRESH_PERIOD), TimeUnit.MILLISECONDS);
        registerTask(extensionServerMethodCallerTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(30, TimeUnit.SECONDS), extensionRefreshPeriod
        );
    }
}
