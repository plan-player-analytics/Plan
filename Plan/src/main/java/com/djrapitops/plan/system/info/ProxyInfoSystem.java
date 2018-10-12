/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * InfoSystem for Bungee.
 *
 * @author Rsl1122
 */
@Singleton
public class ProxyInfoSystem extends InfoSystem {

    @Inject
    public ProxyInfoSystem(
            InfoRequestFactory infoRequestFactory,
            ConnectionSystem connectionSystem,
            ServerInfo serverInfo,
            Lazy<WebServer> webServer,
            PluginLogger logger
    ) {
        super(infoRequestFactory, connectionSystem, serverInfo, webServer, logger);
    }

    @Override
    public void runLocally(InfoRequest infoRequest) throws WebException {
        if (infoRequest instanceof CacheRequest
                || infoRequest instanceof GenerateInspectPageRequest
                || infoRequest instanceof GenerateInspectPluginsTabRequest
        ) {
            infoRequest.runLocally();
        } else {
            // runLocally is called when ConnectionSystem has no servers.
            throw new NoServersException("No servers were available to process this request (Local attempt): " + infoRequest.getClass().getSimpleName());
        }
    }
}
