/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.CacheRequest;
import com.djrapitops.plan.system.info.request.GenerateInspectPageRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * InfoSystem for Bungee.
 *
 * @author Rsl1122
 */
@Singleton
public class BungeeInfoSystem extends InfoSystem {

    private final ResponseFactory responseFactory;
    private final ServerInfo serverInfo;

    @Inject
    public BungeeInfoSystem(
            InfoRequestFactory infoRequestFactory,
            ResponseFactory responseFactory,
            ConnectionSystem connectionSystem,
            ServerInfo serverInfo,
            WebServer webServer,
            PluginLogger logger
    ) {
        super(infoRequestFactory, connectionSystem, serverInfo, webServer, logger);
        this.responseFactory = responseFactory;

        this.serverInfo = serverInfo;
    }

    @Override
    public void runLocally(InfoRequest infoRequest) throws WebException {
        if (infoRequest instanceof CacheRequest
                || infoRequest instanceof GenerateInspectPageRequest) {
            infoRequest.runLocally();
        } else {
            // runLocally is called when ConnectionSystem has no servers.
            throw new NoServersException("No servers were available to process this request (Local attempt): " + infoRequest.getClass().getSimpleName());
        }
    }

    @Override
    public void updateNetworkPage() {
        ResponseCache.cacheResponse(PageId.SERVER.of(serverInfo.getServerUUID()), responseFactory::networkPageResponse);
    }
}
