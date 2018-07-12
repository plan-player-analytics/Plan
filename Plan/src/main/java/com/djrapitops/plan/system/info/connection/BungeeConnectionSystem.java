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
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ConnectionSystem for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh;

    public BungeeConnectionSystem() {
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
        if (infoRequest instanceof CacheRequest) {
            throw new NoServersException("Bungee should not send Cache requests.");
        } else if (infoRequest instanceof GenerateAnalysisPageRequest) {
            UUID serverUUID = ((GenerateAnalysisPageRequest) infoRequest).getServerUUID();
            server = bukkitServers.get(serverUUID);
        } else if (infoRequest instanceof GenerateInspectPageRequest) {
            // Run locally
            server = ServerInfo.getServer();
        }
        if (server == null) {
            throw new NoServersException("Proper server is not available to process request: " + infoRequest.getClass().getSimpleName());
        }
        return server;
    }

    private Server getOneBukkitServer() {
        int rand = ThreadLocalRandom.current().nextInt(bukkitServers.size());
        int i = 0;
        for (Server server : bukkitServers.values()) {
            if (i == rand) {
                return server;
            }
            i++;
        }
        // Fallback if code above fails (Shouldn't)
        Optional<Server> first = bukkitServers.values().stream().findAny();
        return first.orElse(null);
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
        return WebServerSystem.getInstance().getWebServer().getAccessAddress();
    }

    @Override
    public void enable() {
        refreshServerMap();
    }

    @Override
    public void disable() {

    }
}
