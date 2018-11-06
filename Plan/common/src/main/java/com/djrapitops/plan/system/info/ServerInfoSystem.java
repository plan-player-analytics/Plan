/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
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
}
