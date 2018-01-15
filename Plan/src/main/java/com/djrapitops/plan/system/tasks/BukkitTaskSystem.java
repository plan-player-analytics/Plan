/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.tasks.bukkit.*;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.ITask;

/**
 * TaskSystem responsible for registering tasks for Bukkit.
 *
 * @author Rsl1122
 */
public class BukkitTaskSystem extends TaskSystem {

    private ITask bootAnalysisTask;

    public BukkitTaskSystem(Plan plugin) {
        tpsCountTimer = Check.isPaperAvailable()
                ? new PaperTPSCountTimer(plugin)
                : new BukkitTPSCountTimer(plugin);

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
        Benchmark.stop("Enable", "Task Registration");
    }

    public void cancelBootAnalysis() {
        try {
            bootAnalysisTask.cancel();
        } catch (Exception ignored) {
            /* Ignored */
        }
    }
}