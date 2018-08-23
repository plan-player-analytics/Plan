/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.UUID;

/**
 * ConnectionSystem for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeConnectionSystem extends ConnectionSystem {

    private final Database database;
    private final ServerInfo serverInfo;
    private final WebServer webServer;
    private ErrorHandler errorHandler;

    private long latestServerMapRefresh;

    @Inject
    public BungeeConnectionSystem(
            Database database,
            ServerInfo serverInfo,
            WebServer webServer,
            ErrorHandler errorHandler
    ) {
        this.database = database;
        this.serverInfo = serverInfo;
        this.webServer = webServer;
        this.errorHandler = errorHandler;

        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        if (latestServerMapRefresh < System.currentTimeMillis() - TimeAmount.SECOND.ms() * 15L) {
            try {
                bukkitServers = database.fetch().getBukkitServers();
                latestServerMapRefresh = System.currentTimeMillis();
            } catch (DBOpException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        }
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        refreshServerMap();
        Server server = null;
        if (infoRequest instanceof CacheRequest || infoRequest instanceof GenerateInspectPageRequest) {
            // Run locally
            return serverInfo.getServer();
        } else if (infoRequest instanceof GenerateAnalysisPageRequest) {
            UUID serverUUID = ((GenerateAnalysisPageRequest) infoRequest).getServerUUID();
            server = bukkitServers.get(serverUUID);
        }
        if (server == null) {
            throw new NoServersException("Proper server is not available to process request: " + infoRequest.getClass().getSimpleName());
        }
        return server;
    }

    @Override
    public void sendWideInfoRequest(WideRequest infoRequest) throws NoServersException {
        refreshServerMap();
        if (bukkitServers.isEmpty()) {
            throw new NoServersException("No Servers available to make wide-request: " + infoRequest.getClass().getSimpleName());
        }
        for (Server server : bukkitServers.values()) {
            WebExceptionLogger.logIfOccurs(this.getClass(), () -> sendInfoRequest(infoRequest, server));
        }
    }

    @Override
    public boolean isServerAvailable() {
        return true;
    }

    @Override
    public String getMainAddress() {
        return webServer.getAccessAddress();
    }

    @Override
    public void enable() {
        refreshServerMap();
    }

}
