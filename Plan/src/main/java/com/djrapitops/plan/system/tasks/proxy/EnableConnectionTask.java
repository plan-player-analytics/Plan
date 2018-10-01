package com.djrapitops.plan.system.tasks.proxy;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.GenerateNetworkPageContentRequest;
import com.djrapitops.plugin.task.AbsRunnable;

public class EnableConnectionTask extends AbsRunnable {

    public EnableConnectionTask() {
        super(EnableConnectionTask.class.getSimpleName());
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(),
                () -> ConnectionSystem.getInstance().sendWideInfoRequest(new GenerateNetworkPageContentRequest())
        );
        cancel();
    }
}
