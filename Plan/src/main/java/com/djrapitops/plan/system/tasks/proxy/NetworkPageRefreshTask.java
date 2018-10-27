package com.djrapitops.plan.system.tasks.proxy;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.task.AbsRunnable;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NetworkPageRefreshTask extends AbsRunnable {

    private final ServerInfo serverInfo;

    @Inject
    public NetworkPageRefreshTask(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void run() {
        ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
    }
}
