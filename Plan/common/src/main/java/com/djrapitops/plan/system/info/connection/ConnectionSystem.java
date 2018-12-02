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
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.request.InfoRequests;
import com.djrapitops.plan.system.info.request.WideRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import dagger.Lazy;

import java.util.*;

/**
 * ConnectionSystem manages out- and inbound InfoRequest connections.
 * <p>
 * It decides what server to use for each request.
 *
 * @author Rsl1122
 */
public abstract class ConnectionSystem implements SubSystem {

    protected final ConnectionLog connectionLog;
    protected final InfoRequests infoRequests;
    protected final Lazy<InfoSystem> infoSystem;
    protected final ServerInfo serverInfo;

    protected Map<UUID, Server> bukkitServers;
    private boolean setupAllowed;

    public ConnectionSystem(
            ConnectionLog connectionLog,
            InfoRequests infoRequests,
            Lazy<InfoSystem> infoSystem,
            ServerInfo serverInfo
    ) {
        this.connectionLog = connectionLog;
        this.infoSystem = infoSystem;
        this.serverInfo = serverInfo;
        setupAllowed = false;
        bukkitServers = new HashMap<>();
        this.infoRequests = infoRequests;
    }

    public InfoRequest getInfoRequest(String name) {
        return infoRequests.get(name.toLowerCase());
    }

    public void setSetupAllowed(boolean setupAllowed) {
        this.setupAllowed = setupAllowed;
    }

    protected abstract Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException;

    public boolean isSetupAllowed() {
        return setupAllowed;
    }

    public void sendInfoRequest(InfoRequest infoRequest) throws WebException {
        Server server = selectServerForRequest(infoRequest);
        sendInfoRequest(infoRequest, server);
    }

    public void sendInfoRequest(InfoRequest infoRequest, Server toServer) throws WebException {
        UUID serverUUID = serverInfo.getServerUUID();
        if (serverUUID.equals(toServer.getUuid())) {
            infoSystem.get().runLocally(infoRequest);
        } else {
            new ConnectionOut(toServer, serverUUID, infoRequest, connectionLog).sendRequest();
        }
    }

    public ConnectionLog getConnectionLog() {
        return connectionLog;
    }

    public abstract boolean isServerAvailable();

    public abstract String getMainAddress();

    public abstract void sendWideInfoRequest(WideRequest infoRequest) throws NoServersException;

    public List<Server> getBukkitServers() {
        return new ArrayList<>(bukkitServers.values());
    }

    @Override
    public void enable() {
        infoRequests.initializeRequests();
    }

    @Override
    public void disable() {
        setupAllowed = false;
        bukkitServers.clear();
        infoRequests.clear();
    }
}
