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

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.db.tasks.DBCleanTask;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.ConfigStoreTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plan.system.tasks.sponge.PingCountTimerSponge;
import com.djrapitops.plan.system.tasks.sponge.SpongeTPSCountTimer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class SpongeTaskSystem extends ServerTaskSystem {

    private final PlanSponge plugin;
    private final ShutdownHook shutdownHook;
    private final PingCountTimerSponge pingCountTimer;
    private final ConfigStoreTask configStoreTask;
    private final DBCleanTask dbCleanTask;

    @Inject
    public SpongeTaskSystem(
            PlanSponge plugin,
            PlanConfig config,
            ShutdownHook shutdownHook,
            RunnableFactory runnableFactory,
            SpongeTPSCountTimer spongeTPSCountTimer,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask,
            PingCountTimerSponge pingCountTimer,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask,
            ConfigStoreTask configStoreTask,
            DBCleanTask dbCleanTask
    ) {
        super(
                runnableFactory,
                spongeTPSCountTimer,
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
    }

    @Override
    public void enable() {
        super.enable();

        plugin.registerListener(pingCountTimer);
        long startDelay = TimeAmount.toTicks(config.get(TimeSettings.PING_SERVER_ENABLE_DELAY), TimeUnit.MILLISECONDS);
        runnableFactory.create("PingCountTimer", pingCountTimer)
                .runTaskTimer(startDelay, PingCountTimerSponge.PING_INTERVAL);

        // +40 ticks / 2 seconds so that update check task runs first.
        long storeDelay = TimeAmount.toTicks(config.get(TimeSettings.CONFIG_UPDATE_INTERVAL), TimeUnit.MILLISECONDS) + 40;
        registerTask("Config Store Task", configStoreTask).runTaskLaterAsynchronously(storeDelay);

        registerTask("DB Clean Task", dbCleanTask).runTaskTimerAsynchronously(
                TimeAmount.toTicks(20, TimeUnit.SECONDS),
                TimeAmount.toTicks(config.get(TimeSettings.CLEAN_DATABASE_PERIOD), TimeUnit.MILLISECONDS)
        );

        shutdownHook.register();
    }

    @Override
    public void disable() {
        super.disable();
        for (Task task : Sponge.getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }
    }
}
