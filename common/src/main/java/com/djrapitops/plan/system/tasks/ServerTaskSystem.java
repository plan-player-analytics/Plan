package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.tasks.server.BootAnalysisTask;
import com.djrapitops.plan.system.tasks.server.NetworkPageRefreshTask;
import com.djrapitops.plan.system.tasks.server.PeriodicAnalysisTask;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;

/**
 * Abstracted TaskSystem implementation for both Bukkit and Sponge.
 *
 * @author Rsl1122
 */
public class ServerTaskSystem extends TaskSystem {

    protected final PlanPlugin plugin;
    protected ITask bootAnalysisTask;

    public ServerTaskSystem(PlanPlugin plugin, TPSCountTimer tpsCountTimer) {
        super(tpsCountTimer);
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

        Log.info(Locale.get(Msg.ENABLE_BOOT_ANALYSIS_INFO).toString());

        registerTask(tpsCountTimer).runTaskTimer(1000, TimeAmount.SECOND.ticks());
        registerTask(new NetworkPageRefreshTask()).runTaskTimerAsynchronously(20L, 5L * TimeAmount.MINUTE.ticks());
        bootAnalysisTask = registerTask(new BootAnalysisTask()).runTaskLaterAsynchronously(30L * TimeAmount.SECOND.ticks());

        if (analysisRefreshTaskIsEnabled) {
            registerTask(new PeriodicAnalysisTask()).runTaskTimerAsynchronously(analysisPeriod, analysisPeriod);
        }
        if (Settings.ANALYSIS_EXPORT.isTrue()) {
            RunnableFactory.createNew(new HtmlExport(plugin)).runTaskAsynchronously();
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