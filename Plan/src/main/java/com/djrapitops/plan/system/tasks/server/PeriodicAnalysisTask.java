package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;

public class PeriodicAnalysisTask extends AbsRunnable {

    private final InfoSystem infoSystem;
    private final ServerInfo serverInfo;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public PeriodicAnalysisTask(
            InfoSystem infoSystem,
            ServerInfo serverInfo,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.infoSystem = infoSystem;
        this.serverInfo = serverInfo;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            WebExceptionLogger.logIfOccurs(this.getClass(), () ->
                    infoSystem.sendRequest(new GenerateAnalysisPageRequest(serverInfo.getServerUUID()))
            );
        } catch (IllegalStateException e) {
            if (!PlanPlugin.getInstance().isReloading()) {
                errorHandler.log(L.WARN, this.getClass(), e);
            }
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            logger.error("Periodic Analysis Task Disabled due to error, reload Plan to re-enable.");
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }
}
