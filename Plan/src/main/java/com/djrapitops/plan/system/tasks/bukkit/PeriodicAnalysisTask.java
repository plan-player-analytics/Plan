package com.djrapitops.plan.system.tasks.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plugin.task.AbsRunnable;

public class PeriodicAnalysisTask extends AbsRunnable {

    public PeriodicAnalysisTask() {
        super(PeriodicAnalysisTask.class.getSimpleName());
    }

    @Override
    public void run() {
        Plan.getInstance().getInfoManager().refreshAnalysis(Plan.getServerUUID());
    }
}
