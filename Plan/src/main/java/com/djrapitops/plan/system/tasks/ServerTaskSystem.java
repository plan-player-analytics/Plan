package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.RunnableFactory;

/**
 * Abstracted TaskSystem implementation for both Bukkit and Sponge.
 *
 * @author Rsl1122
 */
public abstract class ServerTaskSystem extends TaskSystem {

    private final PlanConfig config;
    private final NetworkPageRefreshTask networkPageRefreshTask;
    private final PeriodicAnalysisTask periodicAnalysisTask;
    protected BootAnalysisTask bootAnalysisTask;

    public ServerTaskSystem(
            RunnableFactory runnableFactory,
            TPSCountTimer tpsCountTimer,
            PlanConfig config,
            NetworkPageRefreshTask networkPageRefreshTask,
            BootAnalysisTask bootAnalysisTask,
            PeriodicAnalysisTask periodicAnalysisTask
    ) {
        super(runnableFactory, tpsCountTimer);
        this.config = config;
        this.networkPageRefreshTask = networkPageRefreshTask;
        this.bootAnalysisTask = bootAnalysisTask;
        this.periodicAnalysisTask = periodicAnalysisTask;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        // Analysis refresh settings
        int analysisRefreshMinutes = config.getNumber(Settings.ANALYSIS_AUTO_REFRESH);
        boolean analysisRefreshTaskIsEnabled = analysisRefreshMinutes > 0;
        long analysisPeriod = analysisRefreshMinutes * TimeAmount.MINUTE.ticks();

        registerTask(tpsCountTimer).runTaskTimer(1000, TimeAmount.SECOND.ticks());
        registerTask(networkPageRefreshTask).runTaskTimerAsynchronously(20L, 5L * TimeAmount.MINUTE.ticks());
        registerTask(bootAnalysisTask).runTaskLaterAsynchronously(30L * TimeAmount.SECOND.ticks());

        if (analysisRefreshTaskIsEnabled) {
            registerTask(periodicAnalysisTask).runTaskTimerAsynchronously(analysisPeriod, analysisPeriod);
        }
    }

    public void cancelBootAnalysis() {
        try {
            if (bootAnalysisTask != null) {
                bootAnalysisTask.cancel();
                bootAnalysisTask = null;
            }
        } catch (Exception ignored) {
            /* Ignored */
        }
    }
}