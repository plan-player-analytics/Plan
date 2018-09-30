/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.UUID;

/**
 * ConnectionSystem for Velocity.
 *
 * Based on BungeeConnectionSystem
 *
 * @author MicleBrick
 */
public class VelocityConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh;

    public VelocityConnectionSystem() {
        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        if (latestServerMapRefresh < System.currentTimeMillis() - TimeAmount.SECOND.ms() * 15L) {
            try {
                bukkitServers = Database.getActive().fetch().getBukkitServers();
                latestServerMapRefresh = System.currentTimeMillis();
            } catch (DBOpException e) {
                Log.toLog(this.getClass(), e);
            }
        }
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        refreshServerMap();
        Server server = null;
        if (infoRequest instanceof CacheRequest
                || infoRequest instanceof GenerateInspectPageRequest
                || infoRequest instanceof GenerateInspectPluginsTabRequest) {
            // Run locally
            return ServerInfo.getServer();
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
        // Quick hack
        if (infoRequest instanceof GenerateInspectPluginsTabRequest) {
            WebExceptionLogger.logIfOccurs(this.getClass(), () -> InfoSystem.getInstance().sendRequest(infoRequest));
        }
    }

    @Override
    public boolean isServerAvailable() {
        return true;
    }

    @Override
    public String getMainAddress() {
        return WebServerSystem.getInstance().getWebServer().getAccessAddress();
    }

    @Override
    public void enable() {
        refreshServerMap();
    }

}
