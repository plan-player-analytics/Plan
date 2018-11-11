/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
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
