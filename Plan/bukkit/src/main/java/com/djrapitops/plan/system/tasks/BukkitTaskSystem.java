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

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.db.tasks.DBCleanTask;
import com.djrapitops.plan.extension.ExtensionServerMethodCallerTask;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.tasks.bukkit.BukkitTPSCountTimer;
import com.djrapitops.plan.system.tasks.bukkit.PaperTPSCountTimer;
import com.djrapitops.plan.system.tasks.bukkit.PingCountTimerBukkit;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.ConfigStoreTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
@Singleton
public class BukkitTaskSystem extends ServerTaskSystem {

    private final Plan plugin;
    private final ShutdownHook shutdownHook;
    private final PingCountTimerBukkit pingCountTimer;
    private final ConfigStoreTask configStoreTask;
    private final DBCleanTask dbCleanTask;
    private final ExtensionServerMethodCallerTask extensionServerMethodCallerTask;

    @Inject
    public BukkitTaskSystem(
            Plan plugin,
            PlanConfig config,
            ShutdownHook shutdownHook,
            RunnableFactory runnableFactory,
            PaperTPSCountTimer paperTPSCountTimer,
            BukkitTPSCountTimer bukkitTPSCountTimer,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask,
            PingCountTimerBukkit pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask,
            ConfigStoreTask configStoreTask,
            DBCleanTask dbCleanTask,
            ExtensionServerMethodCallerTask extensionServerMethodCallerTask
    ) {
        super(
                runnableFactory,
                Check.isPaperAvailable() ? paperTPSCountTimer : bukkitTPSCountTimer,
                config,
                bootAnalysisTask,
                periodicAnalysisTask,
                logsFolderCleanTask,
                playersPageRefreshTask);
        this.plugin = plugin;
        this.shutdownHook = shutdownHook;
        this.pingCountTimer = pingCountTimer;
        this.configStoreTask = configStoreTask;
        this.dbCleanTask = dbCleanTask;
        this.extensionServerMethodCallerTask = extensionServerMethodCallerTask;
    }

    @Override
    public void enable() {
        super.enable();
        try {
            Long pingDelay = config.get(TimeSettings.PING_SERVER_ENABLE_DELAY);
            if (pingDelay < TimeUnit.HOURS.toMillis(1L)) {
                plugin.registerListener(pingCountTimer);
                long startDelay = TimeAmount.toTicks(pingDelay, TimeUnit.MILLISECONDS);
                registerTask(pingCountTimer).runTaskTimer(startDelay, 40L);
            }
        } catch (ExceptionInInitializerError | NoClassDefFoundError ignore) {
            // Running CraftBukkit
        }

        // +40 ticks / 2 seconds so that update check task runs first.
        long storeDelay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        registerTask(configStoreTask).runTaskLaterAsynchronously(storeDelay);

        registerTask(dbCleanTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(20, TimeUnit.SECONDS),
                TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS)
        );

        long extensionRefreshPeriod = TimeAmount.toTicks(config.get(TimeSettings.EXTENSION_DATA_REFRESH_PERIOD), TimeUnit.MILLISECONDS);
        registerTask(extensionServerMethodCallerTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(30, TimeUnit.SECONDS), extensionRefreshPeriod
        );
        shutdownHook.register();
    }

    @Override
    public void disable() {
        super.disable();
        Optional.ofNullable(Bukkit.getScheduler()).ifPresent(scheduler -> scheduler.cancelTasks(plugin));
    }
}
