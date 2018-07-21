/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.*;
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

    public ConnectionSystem() {
        setupAllowed = false;
        bukkitServers = new HashMap<>();
        dataRequests = loadDataRequests();
        connectionLog = new ConnectionLog();
    }

    public static ConnectionSystem getInstance() {
        ConnectionSystem connectionSystem = InfoSystem.getInstance().getConnectionSystem();
        Verify.nullCheck(connectionSystem, () -> new IllegalStateException("Connection System was not initialized"));
        return connectionSystem;
    }

    public static boolean isSetupAllowed() {
        return getInstance().setupAllowed;
    }

    public void setSetupAllowed(boolean setupAllowed) {
        this.setupAllowed = setupAllowed;
    }

    public static String getAddress() {
        return getInstance().getMainAddress();
    }

    public InfoRequest getInfoRequest(String name) {
        return dataRequests.get(name.toLowerCase());
    }

    private void putRequest(Map<String, InfoRequest> requests, InfoRequest request) {
        requests.put(request.getClass().getSimpleName().toLowerCase(), request);
    }

    protected abstract Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException;

    public void sendInfoRequest(InfoRequest infoRequest) throws WebException {
        Server server = selectServerForRequest(infoRequest);
        sendInfoRequest(infoRequest, server);
    }

    public void sendInfoRequest(InfoRequest infoRequest, Server toServer) throws WebException {
        if (ServerInfo.getServerUUID().equals(toServer.getUuid())) {
            InfoSystem.getInstance().runLocally(infoRequest);
        } else {
            new ConnectionOut(toServer, ServerInfo.getServerUUID(), infoRequest).sendRequest();
        }
    }

    public ConnectionLog getConnectionLog() {
        return connectionLog;
    }

    public abstract boolean isServerAvailable();

    public abstract String getMainAddress();

    public abstract void sendWideInfoRequest(WideRequest infoRequest) throws NoServersException;

    private Map<String, InfoRequest> loadDataRequests() {
        Map<String, InfoRequest> requests = new HashMap<>();
        putRequest(requests, CacheInspectPageRequest.createHandler());
        putRequest(requests, CacheInspectPluginsTabRequest.createHandler());
        putRequest(requests, CacheAnalysisPageRequest.createHandler());
        putRequest(requests, CacheNetworkPageContentRequest.createHandler());

        putRequest(requests, GenerateAnalysisPageRequest.createHandler());
        putRequest(requests, GenerateInspectPageRequest.createHandler());
        putRequest(requests, GenerateInspectPluginsTabRequest.createHandler());
        putRequest(requests, GenerateNetworkPageContentRequest.createHandler());

        putRequest(requests, SaveDBSettingsRequest.createHandler());
        putRequest(requests, SendDBSettingsRequest.createHandler());
        putRequest(requests, CheckConnectionRequest.createHandler());

//        putRequest(requests, UpdateRequest.createHandler());
//        putRequest(requests, UpdateCancelRequest.createHandler());
        return requests;
    }

    public List<Server> getBukkitServers() {
        return new ArrayList<>(bukkitServers.values());
    }
}
