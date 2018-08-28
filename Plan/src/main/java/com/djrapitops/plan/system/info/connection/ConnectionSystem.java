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
import com.djrapitops.plan.system.info.request.WideRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plugin.utilities.Verify;
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
    protected final Map<String, InfoRequest> dataRequests;
    protected final Lazy<InfoSystem> infoSystem;
    protected final ServerInfo serverInfo;

    protected Map<UUID, Server> bukkitServers;
    private boolean setupAllowed;

    public ConnectionSystem(
            ConnectionLog connectionLog,
            Map<String, InfoRequest> dataRequests,
            Lazy<InfoSystem> infoSystem,
            ServerInfo serverInfo
    ) {
        this.connectionLog = connectionLog;
        this.infoSystem = infoSystem;
        this.serverInfo = serverInfo;
        setupAllowed = false;
        bukkitServers = new HashMap<>();
        this.dataRequests = dataRequests;
    }

    @Deprecated
    public static ConnectionSystem getInstance() {
        ConnectionSystem connectionSystem = InfoSystem.getInstance().getConnectionSystem();
        Verify.nullCheck(connectionSystem, () -> new IllegalStateException("Connection System was not initialized"));
        return connectionSystem;
    }

    public InfoRequest getInfoRequest(String name) {
        return dataRequests.get(name.toLowerCase());
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
    public void disable() {
        setupAllowed = false;
        bukkitServers.clear();
        dataRequests.clear();
    }
}
