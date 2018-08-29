package com.djrapitops.plan.system.tasks.bungee;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnableConnectionTask extends AbsRunnable {

    private ConnectionSystem connectionSystem;
    private final InfoRequestFactory infoRequestFactory;
    private final WebExceptionLogger webExceptionLogger;

    @Inject
    public EnableConnectionTask(
            ConnectionSystem connectionSystem,
            InfoRequestFactory infoRequestFactory,
            WebExceptionLogger webExceptionLogger
    ) {
        this.connectionSystem = connectionSystem;
        this.infoRequestFactory = infoRequestFactory;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        webExceptionLogger.logIfOccurs(this.getClass(),
                () -> connectionSystem.sendWideInfoRequest(infoRequestFactory.generateNetworkPageContentRequest())
        );
        cancel();
    }
}
