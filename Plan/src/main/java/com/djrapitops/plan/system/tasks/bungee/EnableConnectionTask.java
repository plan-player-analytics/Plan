package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;

public class EnableConnectionTask extends AbsRunnable {

    private ConnectionSystem connectionSystem;
    private final InfoRequestFactory infoRequestFactory;

    @Inject
    public EnableConnectionTask(
            ConnectionSystem connectionSystem,
            InfoRequestFactory infoRequestFactory
    ) {
        this.connectionSystem = connectionSystem;
        this.infoRequestFactory = infoRequestFactory;
    }

    @Override
    public void run() {
        WebExceptionLogger.logIfOccurs(this.getClass(),
                () -> connectionSystem.sendWideInfoRequest(infoRequestFactory.generateNetworkPageContentRequest())
        );
        cancel();
    }
}
