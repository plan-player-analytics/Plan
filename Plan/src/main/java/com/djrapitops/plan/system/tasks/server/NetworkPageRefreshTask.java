package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NetworkPageRefreshTask extends AbsRunnable {

    private InfoSystem infoSystem;
    private final WebExceptionLogger webExceptionLogger;

    @Inject
    public NetworkPageRefreshTask(
            InfoSystem infoSystem,
            WebExceptionLogger webExceptionLogger
    ) {
        this.infoSystem = infoSystem;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        webExceptionLogger.logIfOccurs(this.getClass(), () -> infoSystem.updateNetworkPage());
    }
}
