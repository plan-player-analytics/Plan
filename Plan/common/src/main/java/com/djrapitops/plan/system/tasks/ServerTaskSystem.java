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

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.concurrent.TimeUnit;

/**
 * Abstracted TaskSystem implementation for both Bukkit and Sponge.
 *
 * @author Rsl1122
 */
public abstract class ServerTaskSystem extends TaskSystem {

    protected final PlanConfig config;
    private final BootAnalysisTask bootAnalysisTask;
    private final PeriodicAnalysisTask periodicAnalysisTask;
    private final LogsFolderCleanTask logsFolderCleanTask;
    private final PlayersPageRefreshTask playersPageRefreshTask;

    public ServerTaskSystem(
            RunnableFactory runnableFactory,
            TPSCountTimer tpsCountTimer,
            PlanConfig config,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask,
            LogsFolderCleanTask logsFolderCleanTask,
            PlayersPageRefreshTask playersPageRefreshTask) {
        super(runnableFactory, tpsCountTimer);
        this.config = config;
        this.bootAnalysisTask = bootAnalysisTask;
        this.periodicAnalysisTask = periodicAnalysisTask;
        this.logsFolderCleanTask = logsFolderCleanTask;
        this.playersPageRefreshTask = playersPageRefreshTask;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        // Analysis refresh settings
        long analysisRefreshMs = config.get(TimeSettings.ANALYSIS_REFRESH_PERIOD);
        boolean analysisRefreshTaskIsEnabled = analysisRefreshMs > 0;
        long analysisPeriod = TimeAmount.toTicks(analysisRefreshMs, TimeUnit.MILLISECONDS);

        registerTask(tpsCountTimer).runTaskTimer(1000, TimeAmount.toTicks(1L, TimeUnit.SECONDS));
        registerTask(bootAnalysisTask).runTaskLaterAsynchronously(TimeAmount.toTicks(30L, TimeUnit.SECONDS));

        if (analysisRefreshTaskIsEnabled) {
            registerTask(periodicAnalysisTask).runTaskTimerAsynchronously(analysisPeriod, analysisPeriod);
        }

        registerTask(logsFolderCleanTask).runTaskLaterAsynchronously(TimeAmount.toTicks(30L, TimeUnit.SECONDS));
        registerTask("Settings Load", new AbsRunnable() {
            @Override
            public void run() {
                config.getNetworkSettings().loadSettingsFromDB();
            }
        }).runTaskAsynchronously();
        registerTask(playersPageRefreshTask)
                .runTaskTimerAsynchronously(TimeAmount.toTicks(5L, TimeUnit.MINUTES), TimeAmount.toTicks(5L, TimeUnit.MINUTES));
    }
}