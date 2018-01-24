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
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
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
 * Connection system for Bukkit servers.
 *
 * @author Rsl1122
 */
public class BukkitConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh;

    private Server mainServer;
    private Map<UUID, Server> servers;

    public BukkitConnectionSystem() {
        servers = new HashMap<>();
        latestServerMapRefresh = 0;
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

        boolean usingBungeeWebServer = ConnectionSystem.getInstance().isServerAvailable();
        boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();

        if (!usingAlternativeIP && ServerInfo.getServerProperties().getIp().isEmpty()) {
            Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_EMPTY_IP).toString());
        }
        if (usingBungeeWebServer && usingAlternativeIP) {
            String webServerAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
            Log.info("Make sure that this address points to the Bukkit Server: " + webServerAddress);
        }
    }

    @Override
    public void disable() {

    }
}