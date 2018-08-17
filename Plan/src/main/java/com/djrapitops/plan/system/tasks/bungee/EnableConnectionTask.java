package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateNetworkPageContentRequest;
import com.djrapitops.plugin.task.AbsRunnable;

public class EnableConnectionTask extends AbsRunnable {

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(),
                () -> ConnectionSystem.getInstance().sendWideInfoRequest(new GenerateNetworkPageContentRequest())
        );
        cancel();
    }
}
