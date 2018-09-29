/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.request.SetupRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * InfoSystem for Bukkit servers.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerInfoSystem extends InfoSystem {

    private final ServerInfo serverInfo;
    private final PluginLogger logger;

    @Inject
    public ServerInfoSystem(
            ConnectionSystem connectionSystem,
            ServerInfo serverInfo,
            InfoRequestFactory infoRequestFactory,
            Lazy<WebServer> webServer,
            PluginLogger logger
    ) {
        super(infoRequestFactory, connectionSystem, serverInfo, webServer, logger);
        this.serverInfo = serverInfo;
        this.logger = logger;
    }

    @Override
    public void runLocally(InfoRequest infoRequest) throws WebException {
        if (infoRequest instanceof SetupRequest) {
            throw new NoServersException("Set-up requests can not be run locally.");
        }
        logger.getDebugLogger().logOn(DebugChannels.INFO_REQUESTS, "Local: " + infoRequest.getClass().getSimpleName());
        infoRequest.runLocally();
    }

    @Override
    public void updateNetworkPage() throws WebException {
        String html = HtmlStructure.createServerContainer();
        sendRequest(infoRequestFactory.cacheNetworkPageContentRequest(serverInfo.getServerUUID(), html));
    }
}
