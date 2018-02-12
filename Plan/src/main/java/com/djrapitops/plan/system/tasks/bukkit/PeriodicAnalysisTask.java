package com.djrapitops.plan.system.tasks.bukkit;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;

public class PeriodicAnalysisTask extends AbsRunnable {

    public PeriodicAnalysisTask() {
        super(PeriodicAnalysisTask.class.getSimpleName());
    }

    @Override
    public void run() {
        try {
            if (!Analysis.isAnalysisBeingRun()) {
                WebExceptionLogger.logIfOccurs(this.getClass(), () ->
                        InfoSystem.getInstance().sendRequest(new GenerateAnalysisPageRequest(ServerInfo.getServerUUID()))
                );
            }
        } catch (IllegalStateException e) {
            if (!PlanPlugin.getInstance().isReloading()) {
                Log.toLog(this.getClass(), e);
            }
        } finally {
            cancel();
        }
    }
}
