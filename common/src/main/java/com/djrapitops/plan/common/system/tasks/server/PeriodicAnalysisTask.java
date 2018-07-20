package com.djrapitops.plan.common.system.tasks.server;

import com.djrapitops.plan.common.PlanHelper;
import com.djrapitops.plan.common.PlanPlugin;
import com.djrapitops.plan.common.system.info.InfoSystem;
import com.djrapitops.plan.common.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.common.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.common.system.info.server.ServerInfo;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;

public class PeriodicAnalysisTask extends AbsRunnable {

    public PeriodicAnalysisTask() {
        super(PeriodicAnalysisTask.class.getSimpleName());
    }

    @Override
    public void run() {
        try {
            WebExceptionLogger.logIfOccurs(this.getClass(), () ->
                    InfoSystem.getInstance().sendRequest(new GenerateAnalysisPageRequest(ServerInfo.getServerUUID()))
            );
        } catch (IllegalStateException e) {
            if (!PlanHelper.getInstance().isReloading()) {
                Log.toLog(this.getClass(), e);
            }
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            Log.error("Periodic Analysis Task Disabled due to error, reload Plan to re-enable.");
            Log.toLog(this.getClass(), e);
            cancel();
        }
    }
}
