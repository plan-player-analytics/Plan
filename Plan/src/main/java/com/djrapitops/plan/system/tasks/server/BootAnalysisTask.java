package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BootAnalysisTask extends AbsRunnable {

    private final InfoSystem infoSystem;
    private final InfoRequestFactory infoRequestFactory;
    private final ServerInfo serverInfo;
    private final WebExceptionLogger webExceptionLogger;

    @Inject
    public BootAnalysisTask(
            InfoSystem infoSystem,
            InfoRequestFactory infoRequestFactory,
            ServerInfo serverInfo,
            WebExceptionLogger webExceptionLogger
    ) {
        this.infoSystem = infoSystem;
        this.infoRequestFactory = infoRequestFactory;
        this.serverInfo = serverInfo;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        try {
            webExceptionLogger.logIfOccurs(this.getClass(), () ->
                    infoSystem.sendRequest(infoRequestFactory.generateAnalysisPageRequest(serverInfo.getServerUUID()))
            );
        } catch (IllegalStateException ignore) {
            /* Plugin was reloading */
        } finally {
            cancel();
        }
    }
}
