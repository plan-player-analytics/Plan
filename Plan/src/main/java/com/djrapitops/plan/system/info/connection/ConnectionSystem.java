/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.NullCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    protected Map<UUID, Server> servers;

    public ConnectionSystem() {
        servers = new HashMap<>();
        dataRequests = loadDataRequests();
        connectionLog = new ConnectionLog();
    }

    public static ConnectionSystem getInstance() {
        ConnectionSystem connectionSystem = InfoSystem.getInstance().getConnectionSystem();
        NullCheck.check(connectionSystem, new IllegalStateException("Connection System was not initialized"));
        return connectionSystem;
    }

    public InfoRequest getInfoRequest(String name) {
        return dataRequests.get(name.toLowerCase());
    }

    private Map<String, InfoRequest> loadDataRequests() {
        Map<String, InfoRequest> requests = new HashMap<>();
        putRequest(requests, CacheInspectPageRequest.createHandler());
        putRequest(requests, CacheAnalysisPageRequest.createHandler());
        putRequest(requests, CacheNetworkPageContentRequest.createHandler());

        putRequest(requests, GenerateAnalysisPageRequest.createHandler());
        putRequest(requests, GenerateInspectPageRequest.createHandler());
        return requests;
    }

    private void putRequest(Map<String, InfoRequest> requests, InfoRequest request) {
        requests.put(request.getClass().getSimpleName().toLowerCase(), request);
    }

    protected abstract Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException;

    public void sendInfoRequest(InfoRequest infoRequest) throws WebException {
        try {
            Server server = selectServerForRequest(infoRequest);

            new ConnectionOut(server, ServerInfo.getServerUUID(), infoRequest).sendRequest();
        } catch (NoServersException e) {
            // At this point ConnectionSystem will return false on isServerAvailable
            // -> InfoSystem will try to run request locally.
            InfoSystem.getInstance().sendRequest(infoRequest);
        }
    }

    public ConnectionLog getConnectionLog() {
        return connectionLog;
    }

    public abstract boolean isServerAvailable();

    public abstract Optional<String> getMainAddress();
}