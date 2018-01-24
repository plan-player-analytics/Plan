package com.djrapitops.plan.system.tasks.bukkit;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.analysis.Analysis;
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
        if (!Analysis.isAnalysisBeingRun()) {
            WebExceptionLogger.logIfOccurs(this.getClass(), () ->
                    InfoSystem.getInstance().sendRequest(new GenerateAnalysisPageRequest(ServerInfo.getServerUUID()))
            );
        }
        cancel();
    }
}
