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
    protected Map<UUID, Server> bukkitServers;
    private boolean setupAllowed;

    public ConnectionSystem(Map<String, InfoRequest> dataRequests) {
        setupAllowed = false;
        bukkitServers = new HashMap<>();
        this.dataRequests = dataRequests;
        connectionLog = new ConnectionLog();
    }

    @Deprecated
    public static ConnectionSystem getInstance() {
        ConnectionSystem connectionSystem = InfoSystem.getInstance().getConnectionSystem();
        Verify.nullCheck(connectionSystem, () -> new IllegalStateException("Connection System was not initialized"));
        return connectionSystem;
    }

    @Deprecated
    public static boolean isSetupAllowed_Old() {
        return getInstance().setupAllowed;
    }

    @Deprecated
    public static String getAddress() {
        return getInstance().getMainAddress();
    }

    public InfoRequest getInfoRequest(String name) {
        return dataRequests.get(name.toLowerCase());
    }

    public void setSetupAllowed(boolean setupAllowed) {
        this.setupAllowed = setupAllowed;
    }

    private void putRequest(InfoRequest request) {
        dataRequests.put(request.getClass().getSimpleName().toLowerCase(), request);
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
        if (ServerInfo.getServerUUID_Old().equals(toServer.getUuid())) {
            InfoSystem.getInstance().runLocally(infoRequest);
        } else {
            new ConnectionOut(toServer, ServerInfo.getServerUUID_Old(), infoRequest).sendRequest();
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
