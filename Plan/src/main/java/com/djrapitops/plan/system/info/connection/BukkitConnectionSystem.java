/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.UnsupportedTransferDatabaseException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.CacheRequest;
import com.djrapitops.plan.system.info.request.GenerateAnalysisPageRequest;
import com.djrapitops.plan.system.info.request.GenerateInspectPageRequest;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Connection system for Bukkit servers.
 *
 * @author Rsl1122
 */
public class BukkitConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh = 0;

    private Server mainServer;
    private Map<UUID, Server> servers;

    public BukkitConnectionSystem() {
    }

    private void refreshServerMap() {
        if (latestServerMapRefresh < MiscUtils.getTime() - TimeAmount.MINUTE.ms() * 2L) {
            try {
                Database database = Database.getActive();
                Optional<Server> bungeeInformation = database.fetch().getBungeeInformation();
                bungeeInformation.ifPresent(server -> mainServer = server);
                servers = database.fetch().getBukkitServers();
                latestServerMapRefresh = MiscUtils.getTime();
            } catch (DBException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        refreshServerMap();

        if (mainServer == null && servers.isEmpty()) {
            throw new NoServersException("No Servers available to process requests.");
        }

        Server server = null;
        if (infoRequest instanceof CacheRequest) {
            server = mainServer;
        } else if (infoRequest instanceof GenerateAnalysisPageRequest) {
            UUID serverUUID = ((GenerateAnalysisPageRequest) infoRequest).getServerUUID();
            server = servers.get(serverUUID);
        } else if (infoRequest instanceof GenerateInspectPageRequest) {
            UUID serverUUID = getServerWherePlayerIsOnline((GenerateInspectPageRequest) infoRequest);
            server = servers.getOrDefault(serverUUID, ServerInfo.getServer());
        }
        if (server == null) {
            throw new NoServersException("Proper server is not available to process requests.");
        }
        return server;
    }

    private UUID getServerWherePlayerIsOnline(GenerateInspectPageRequest infoRequest) {
        UUID playerUUID = infoRequest.getPlayerUUID();
        try {
            return Database.getActive().transfer().getServerPlayerIsOnline(playerUUID);
        } catch (UnsupportedTransferDatabaseException e) {
            /* Do nothing */
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return null;
    }

    @Override
    public boolean isServerAvailable() {
        return ConnectionLog.hasConnectionSucceeded(mainServer);
    }

    @Override
    public String getMainAddress() {
        return isServerAvailable() ? mainServer.getWebAddress() : ServerInfo.getServer().getWebAddress();

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