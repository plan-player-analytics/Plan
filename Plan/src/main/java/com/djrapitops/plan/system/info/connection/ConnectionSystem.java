/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.CacheInspectPageRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.NullCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ConnectionSystem manages out- and inbound InfoRequest connections.
 * <p>
 * It decides what server to use for each request.
 *
 * @author Rsl1122
 */
public abstract class ConnectionSystem {

    protected final Map<String, InfoRequest> dataRequests;
    protected final UUID serverUUID;
    protected Map<UUID, ServerInfo> servers;

    public ConnectionSystem(UUID serverUUID) {
        this.serverUUID = serverUUID;

        servers = new HashMap<>();
        dataRequests = loadDataRequests();
    }

    public static ConnectionSystem getInstance() {
        ConnectionSystem connectionSystem = InfoSystem.getInstance().getConnectionSystem();
        NullCheck.check(connectionSystem, new IllegalStateException("Connection System was not initialized"));
        return connectionSystem;
    }

    private Map<String, InfoRequest> loadDataRequests() {
        Map<String, InfoRequest> requests = new HashMap<>();
        putRequest(requests, CacheInspectPageRequest.createHandler());
        return requests;
    }

    private void putRequest(Map<String, InfoRequest> requests, InfoRequest request) {
        requests.put(request.getClass().getSimpleName(), request);
    }

    protected abstract ServerInfo selectServerForRequest(InfoRequest infoRequest) throws NoServersException;

    public void sendInfoRequest(InfoRequest infoRequest) throws WebException {
        ServerInfo serverInfo = selectServerForRequest(infoRequest);
        String address = serverInfo.getWebAddress();

        new ConnectionOut(address, serverUUID, infoRequest).sendRequest();
    }
}