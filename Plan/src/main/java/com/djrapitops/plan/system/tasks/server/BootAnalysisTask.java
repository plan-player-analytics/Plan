package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;

public class BootAnalysisTask extends AbsRunnable {

    private InfoSystem infoSystem;
    private ErrorHandler errorHandler;

    @Inject
    public BootAnalysisTask(InfoSystem infoSystem, ErrorHandler errorHandler) {
        this.infoSystem = infoSystem;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            WebExceptionLogger.logIfOccurs(this.getClass(), () ->
                    infoSystem.sendRequest(new GenerateAnalysisPageRequest(ServerInfo.getServerUUID_Old()))
            );
        } catch (IllegalStateException e) {
            if (!PlanPlugin.getInstance().isReloading()) {
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        } finally {
            cancel();
        }
    }
}
