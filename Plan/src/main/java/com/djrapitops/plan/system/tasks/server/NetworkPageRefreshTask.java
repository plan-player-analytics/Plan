package com.djrapitops.plan.system.tasks.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.task.AbsRunnable;

public class NetworkPageRefreshTask extends AbsRunnable {

    public NetworkPageRefreshTask() {
        super(NetworkPageRefreshTask.class.getSimpleName());
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(), () -> InfoSystem.getInstance().updateNetworkPage());
    }
}
