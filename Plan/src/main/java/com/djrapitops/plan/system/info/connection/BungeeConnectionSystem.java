/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ConnectionSystem for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh;
    private Map<UUID, Server> servers;

    public BungeeConnectionSystem() {
        servers = new HashMap<>();
        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        if (latestServerMapRefresh < MiscUtils.getTime() - TimeAmount.MINUTE.ms() * 2L) {
            try {
                servers = Database.getActive().fetch().getBukkitServers();
                latestServerMapRefresh = MiscUtils.getTime();
            } catch (DBException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        Server server = null;
        if (infoRequest instanceof CacheRequest) {
            throw new NoServersException("Bungee should not send Cache requests.");
        } else if (infoRequest instanceof GenerateAnalysisPageRequest) {
            UUID serverUUID = ((GenerateAnalysisPageRequest) infoRequest).getServerUUID();
            server = servers.get(serverUUID);
        } else if (infoRequest instanceof GenerateInspectPageRequest) {
            Optional<UUID> serverUUID = getServerWherePlayerIsOnline((GenerateInspectPageRequest) infoRequest);
            if (serverUUID.isPresent()) {
                server = servers.getOrDefault(serverUUID.get(), ServerInfo.getServer());
            }
        }
        if (server == null) {
            throw new NoServersException("Proper server is not available to process requests.");
        }
        return server;
    }

    @Override
    public void sendWideInfoRequest(WideRequest infoRequest) throws NoServersException {
        if (servers.isEmpty()) {
            throw new NoServersException("No Servers Available to make process request.");
        }
        for (Server server : servers.values()) {
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
        RunnableFactory.createNew("Server List Update Task", new AbsRunnable() {
            @Override
            public void run() {
                refreshServerMap();
            }
        }).runTaskTimerAsynchronously(TimeAmount.SECOND.ticks() * 30L, TimeAmount.MINUTE.ticks() * 5L);
    }

    @Override
    public void disable() {

    }
}