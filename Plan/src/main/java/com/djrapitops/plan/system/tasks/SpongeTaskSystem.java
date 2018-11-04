/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.ShutdownHook;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plan.system.tasks.server.sponge.PingCountTimerSponge;
import com.djrapitops.plan.system.tasks.server.sponge.SpongeTPSCountTimer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SpongeTaskSystem extends ServerTaskSystem {

    private final PlanSponge plugin;
    private final ShutdownHook shutdownHook;
    private final PingCountTimerSponge pingCountTimer;

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
            PlayersPageRefreshTask playersPageRefreshTask
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
    }

    @Override
    public void enable() {
        super.enable();

        plugin.registerListener(pingCountTimer);
        long startDelay = TimeAmount.toTicks(config.getNumber(Settings.PING_SERVER_ENABLE_DELAY), TimeUnit.SECONDS);
        runnableFactory.create("PingCountTimer", pingCountTimer)
                .runTaskTimer(startDelay, PingCountTimerSponge.PING_INTERVAL);

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
