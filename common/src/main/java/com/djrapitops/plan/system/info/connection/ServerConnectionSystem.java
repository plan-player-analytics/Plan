/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.request.*;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.Optional;
import java.util.UUID;

/**
 * Connection system for Bukkit servers.
 *
 * @author Rsl1122
 */
public class ServerConnectionSystem extends ConnectionSystem {

    private long latestServerMapRefresh;

    private Server mainServer;

    public ServerConnectionSystem() {
        latestServerMapRefresh = 0;
    }

    private void refreshServerMap() {
        Processing.submitNonCritical(() -> {
            if (latestServerMapRefresh < System.currentTimeMillis() - TimeAmount.SECOND.ms() * 15L) {
                Database database = Database.getActive();
                Optional<Server> bungeeInformation = database.fetch().getBungeeInformation();
                bungeeInformation.ifPresent(server -> mainServer = server);
                bukkitServers = database.fetch().getBukkitServers();
                latestServerMapRefresh = System.currentTimeMillis();
            }
        });
    }

    @Override
    protected Server selectServerForRequest(InfoRequest infoRequest) throws NoServersException {
        refreshServerMap();

        if (mainServer == null && bukkitServers.isEmpty()) {
            throw new NoServersException("Zero servers available to process requests.");
        }

        Server server = null;
        if (infoRequest instanceof CacheRequest ||
                infoRequest instanceof GenerateInspectPageRequest) {
            server = mainServer;
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
        if (bukkitServers.isEmpty()) {
            throw new NoServersException("No Servers available to make wide-request: " + infoRequest.getClass().getSimpleName());
        }
        for (Server server : bukkitServers.values()) {
            WebExceptionLogger.logIfOccurs(this.getClass(), () -> {
                try {
                    sendInfoRequest(infoRequest, server);
                } catch (ConnectionFailException ignored) {
                    /* Wide Requests are used when at least one result is wanted. */
                }
            });
        }
    }

    @Override
    public boolean isServerAvailable() {
        return mainServer != null && Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isFalse();
    }

    @Override
    public String getMainAddress() {
        return isServerAvailable() ? mainServer.getWebAddress() : ServerInfo.getServer().getWebAddress();

    }

    @Override
    public void enable() {
        refreshServerMap();

        boolean usingBungeeWebServer = getInstance().isServerAvailable();
        boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();

        if (!usingAlternativeIP && ServerInfo.getServerProperties().getIp().isEmpty()) {
            Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_EMPTY_IP).toString());
        }
        if (usingBungeeWebServer && usingAlternativeIP) {
            String webServerAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
            Log.info("Make sure that this address points to THIS Bukkit Server: " + webServerAddress);
        }
    }

    @Override
    public void disable() {

    }
}
