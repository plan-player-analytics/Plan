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
import com.djrapitops.plan.gathering.ShutdownHook;
import com.djrapitops.plan.gathering.timed.ServerTPSCounter;
import com.djrapitops.plan.gathering.timed.SpongePingCounter;
import com.djrapitops.plan.gathering.timed.SystemUsageBuffer;
import com.djrapitops.plan.gathering.timed.TPSCounter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.upkeep.ConfigStoreTask;
import com.djrapitops.plan.storage.upkeep.DBCleanTask;
import com.djrapitops.plan.storage.upkeep.LogsFolderCleanTask;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class SpongeTaskSystem extends TaskSystem {

    private final PlanSponge plugin;
    private final PlanConfig config;
    private final ShutdownHook shutdownHook;
    private final TPSCounter tpsCounter;
    private final JSONCache.CleanTask jsonCacheCleanTask;
    private final SystemUsageBuffer.RamAndCpuTask ramAndCpuTask;
    private final SystemUsageBuffer.DiskTask diskTask;
    private final SpongePingCounter pingCounter;
    private final LogsFolderCleanTask logsFolderCleanTask;
    private final ConfigStoreTask configStoreTask;
    private final DBCleanTask dbCleanTask;
    private final ExtensionServerMethodCallerTask extensionServerMethodCallerTask;

    @Inject
    public SpongeTaskSystem(
            PlanSponge plugin,
            PlanConfig config,
            ShutdownHook shutdownHook,
            RunnableFactory runnableFactory,

            ServerTPSCounter<World> tpsCounter,
            SpongePingCounter pingCounter,
            ExtensionServerMethodCallerTask extensionServerMethodCallerTask,

            LogsFolderCleanTask logsFolderCleanTask,
            ConfigStoreTask configStoreTask,
            DBCleanTask dbCleanTask,
            JSONCache.CleanTask jsonCacheCleanTask,
            SystemUsageBuffer.RamAndCpuTask ramAndCpuTask,
            SystemUsageBuffer.DiskTask diskTask
    ) {
        super(runnableFactory);
        this.plugin = plugin;
        this.config = config;
        this.shutdownHook = shutdownHook;

        this.tpsCounter = tpsCounter;
        this.pingCounter = pingCounter;
        this.extensionServerMethodCallerTask = extensionServerMethodCallerTask;

        this.logsFolderCleanTask = logsFolderCleanTask;
        this.configStoreTask = configStoreTask;
        this.dbCleanTask = dbCleanTask;
        this.jsonCacheCleanTask = jsonCacheCleanTask;

        this.ramAndCpuTask = ramAndCpuTask;
        this.diskTask = diskTask;
    }

    @Override
    public void enable() {
        registerTPSCounter();
        registerPingCounter();
        registerExtensionDataGatheringTask();
        registerUpkeepTasks();

        shutdownHook.register();
    }

    private void registerUpkeepTasks() {
        // +40 ticks / 2 seconds so that update check task runs first.
        long storeDelay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        registerTask(configStoreTask).runTaskLaterAsynchronously(storeDelay);
        registerTask(logsFolderCleanTask).runTaskLaterAsynchronously(TimeAmount.toTicks(30L, TimeUnit.SECONDS));
        registerTask(dbCleanTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(20, TimeUnit.SECONDS),
                TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS)
        );
        long minute = TimeAmount.toTicks(1, TimeUnit.MINUTES);
        registerTask(jsonCacheCleanTask).runTaskTimerAsynchronously(minute, minute);
    }

    private void registerTPSCounter() {
        long halfSecondTicks = TimeAmount.toTicks(500L, TimeUnit.MILLISECONDS);
        long secondTicks = TimeAmount.toTicks(1L, TimeUnit.SECONDS);
        long minuteTicks = TimeAmount.toTicks(1L, TimeUnit.MINUTES);
        registerTask(tpsCounter).runTaskTimer(minuteTicks, secondTicks);
        registerTask(ramAndCpuTask).runTaskTimerAsynchronously(minuteTicks - halfSecondTicks, secondTicks);
        registerTask(diskTask).runTaskTimerAsynchronously(50L * secondTicks, minuteTicks);
    }

    private void registerPingCounter() {
        Long pingDelay = config.get(TimeSettings.PING_SERVER_ENABLE_DELAY);
        if (pingDelay < TimeUnit.HOURS.toMillis(1L) && config.isTrue(DataGatheringSettings.PING)) {
            plugin.registerListener(pingCounter);
            long startDelay = TimeAmount.toTicks(pingDelay, TimeUnit.MILLISECONDS);
            registerTask(pingCounter).runTaskTimer(startDelay, 40L);
        }
    }

    private void registerExtensionDataGatheringTask() {
        long extensionRefreshPeriod = TimeAmount.toTicks(config.get(TimeSettings.EXTENSION_DATA_REFRESH_PERIOD), TimeUnit.MILLISECONDS);
        registerTask(extensionServerMethodCallerTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(30, TimeUnit.SECONDS), extensionRefreshPeriod
        );
    }

    @Override
    public void disable() {
        super.disable();
        for (Task task : Sponge.getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }
    }
}
