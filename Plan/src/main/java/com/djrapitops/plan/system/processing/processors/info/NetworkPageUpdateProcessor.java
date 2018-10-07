/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;

/**
 * Processor for updating the network page.
 *
 * @author Rsl1122
 */
public class NetworkPageUpdateProcessor implements Runnable {

    private final ServerInfo serverInfo;

    NetworkPageUpdateProcessor(
            ServerInfo serverInfo
    ) {
        this.serverInfo = serverInfo;
    }

    @Override
    public void run() {
        ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
    }
}
