package com.djrapitops.plan.bungee.tasks.bungee;

import com.djrapitops.plan.common.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.common.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.common.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.common.system.info.request.GenerateNetworkPageContentRequest;
import com.djrapitops.plugin.task.AbsRunnable;

public class EnableConnectionTask extends AbsRunnable {

    public EnableConnectionTask() {
        super(EnableConnectionTask.class.getSimpleName());
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(),
                () -> {
                    try {
                        ConnectionSystem.getInstance().sendWideInfoRequest(new GenerateNetworkPageContentRequest());
                    } catch (NoServersException e) {
                        e.printStackTrace();
                    }
                }
        );
        cancel();
    }
}
