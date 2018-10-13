/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
