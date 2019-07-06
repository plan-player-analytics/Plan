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

import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.request.GenerateRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.InfoRequestFactory;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Lazy;

import java.util.UUID;

/**
 * Information management system.
 * <p>
 * Subclasses should decide how InfoRequests are run locally if necessary.
 * <p>
 * Everything should be called from an Async thread.
 *
 * @author Rsl1122
 */
public abstract class InfoSystem implements SubSystem {

    protected final InfoRequestFactory infoRequestFactory;
    protected final ConnectionSystem connectionSystem;
    protected final ServerInfo serverInfo;
    protected final Lazy<WebServer> webServer;
    protected final PluginLogger logger;

    protected InfoSystem(
            InfoRequestFactory infoRequestFactory,
            ConnectionSystem connectionSystem,
            ServerInfo serverInfo,
            Lazy<WebServer> webServer,
            PluginLogger logger
    ) {
        this.infoRequestFactory = infoRequestFactory;
        this.connectionSystem = connectionSystem;
        this.serverInfo = serverInfo;
        this.webServer = webServer;
        this.logger = logger;
    }

    /**
     * Refreshes Player page.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param player UUID of the player.
     * @throws WebException If fails.
     */
    public void generateAndCachePlayerPage(UUID player) throws WebException {
        GenerateRequest infoRequest = infoRequestFactory.generateInspectPageRequest(player);
        try {
            sendRequest(infoRequest);
        } catch (ConnectionFailException e) {
            runLocally(infoRequest);
        }
    }

    /**
     * Send an InfoRequest to another server or run locally if necessary.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param infoRequest InfoRequest to send or run.
     * @throws WebException If fails.
     */
    public void sendRequest(InfoRequest infoRequest) throws WebException {
        try {
            if (!connectionSystem.isServerAvailable()) {
                logger.getDebugLogger().logOn(DebugChannels.INFO_REQUESTS, "Main server unavailable, running locally.");
                runLocally(infoRequest);
                return;
            }
            connectionSystem.sendInfoRequest(infoRequest);
        } catch (WebException original) {
            try {
                logger.getDebugLogger().logOn(DebugChannels.INFO_REQUESTS, "Exception during request: " + original.toString() + ", running locally.");
                runLocally(infoRequest);
            } catch (NoServersException noServers) {
                throw original;
            }
        }
    }

    /**
     * Run the InfoRequest locally.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param infoRequest InfoRequest to run.
     * @throws WebException If fails.
     */
    public abstract void runLocally(InfoRequest infoRequest) throws WebException;

    @Override
    public void enable() {
        connectionSystem.enable();
    }

    @Override
    public void disable() {
        connectionSystem.disable();
    }

    public ConnectionSystem getConnectionSystem() {
        return connectionSystem;
    }

    /**
     * Requests Set up from Bungee.
     * <p>
     * No calls from non-async thread found on 09.02.2018
     *
     * @param addressToRequestServer Address of Bungee server.
     * @throws WebException If fails.
     */
    public void requestSetUp(String addressToRequestServer) throws WebException {
        if (serverInfo.getServer().isProxy()) {
            throw new BadRequestException("Method not available on a Proxy server.");
        }
        Server bungee = new Server(-1, null, "Bungee", addressToRequestServer, -1);
        String addressOfThisServer = webServer.get().getAccessAddress();

        connectionSystem.setSetupAllowed(true);
        connectionSystem.sendInfoRequest(infoRequestFactory.sendDBSettingsRequest(addressOfThisServer), bungee);
    }
}
