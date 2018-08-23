package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;

public class NetworkPageRefreshTask extends AbsRunnable {

    private InfoSystem infoSystem;

    @Inject
    public NetworkPageRefreshTask(InfoSystem infoSystem) {
        this.infoSystem = infoSystem;
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(), () -> infoSystem.updateNetworkPage());
    }
}
