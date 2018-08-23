package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateNetworkPageContentRequest;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;

public class EnableConnectionTask extends AbsRunnable {

    private ConnectionSystem connectionSystem;

    @Inject
    public EnableConnectionTask(ConnectionSystem connectionSystem) {
        this.connectionSystem = connectionSystem;
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(),
                () -> connectionSystem.sendWideInfoRequest(new GenerateNetworkPageContentRequest())
        );
        cancel();
    }
}
