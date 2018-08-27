package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;

public class BootAnalysisTask extends AbsRunnable {

    private final InfoSystem infoSystem;
    private final InfoRequestFactory infoRequestFactory;
    private final ServerInfo serverInfo;

    @Inject
    public BootAnalysisTask(
            InfoSystem infoSystem,
            InfoRequestFactory infoRequestFactory,
            ServerInfo serverInfo
    ) {
        this.infoSystem = infoSystem;
        this.infoRequestFactory = infoRequestFactory;
        this.serverInfo = serverInfo;
    }

    @Override
    public void run() {
        try {
            WebExceptionLogger.logIfOccurs(this.getClass(), () ->
                    infoSystem.sendRequest(infoRequestFactory.generateAnalysisPageRequest(serverInfo.getServerUUID()))
            );
        } catch (IllegalStateException ignore) {
            /* Plugin was reloading */
        } finally {
            cancel();
        }
    }
}
