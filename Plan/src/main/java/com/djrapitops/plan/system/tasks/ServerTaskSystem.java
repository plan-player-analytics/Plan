package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.PluginTask;

/**
 * Abstracted TaskSystem implementation for both Bukkit and Sponge.
 *
 * @author Rsl1122
 */
public class ServerTaskSystem extends TaskSystem {

    protected final PlanPlugin plugin;
    protected PluginTask bootAnalysisTask;

    public ServerTaskSystem(PlanPlugin plugin, TPSCountTimer tpsCountTimer) {
        super(plugin, tpsCountTimer);
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        registerTasks();
    }

    private void registerTasks() {
        Benchmark.start("Task Registration");

        // Analysis refresh settings
        int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
        boolean analysisRefreshTaskIsEnabled = analysisRefreshMinutes > 0;
        long analysisPeriod = analysisRefreshMinutes * TimeAmount.MINUTE.ticks();

        registerTask(tpsCountTimer).runTaskTimer(1000, TimeAmount.SECOND.ticks());
        registerTask(new NetworkPageRefreshTask()).runTaskTimerAsynchronously(20L, 5L * TimeAmount.MINUTE.ticks());
        bootAnalysisTask = registerTask(new BootAnalysisTask()).runTaskLaterAsynchronously(30L * TimeAmount.SECOND.ticks());

        if (analysisRefreshTaskIsEnabled) {
            registerTask(new PeriodicAnalysisTask()).runTaskTimerAsynchronously(analysisPeriod, analysisPeriod);
        }
        if (Settings.ANALYSIS_EXPORT.isTrue()) {
            Processing.submitNonCritical(new HtmlExport(plugin));
        }
        Benchmark.stop("Enable", "Task Registration");
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