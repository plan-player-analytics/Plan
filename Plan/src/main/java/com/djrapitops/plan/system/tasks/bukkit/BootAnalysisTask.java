package com.djrapitops.plan.system.tasks.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;

public class BootAnalysisTask extends AbsRunnable {

    public BootAnalysisTask() {
        super(BootAnalysisTask.class.getSimpleName());
    }

    @Override
    public void run() {
        String bootAnalysisRunMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_RUN_INFO).toString();
        Log.info(bootAnalysisRunMsg);
        Plan.getInstance().getInfoManager().refreshAnalysis(Plan.getServerUUID());
        cancel();
    }
}
