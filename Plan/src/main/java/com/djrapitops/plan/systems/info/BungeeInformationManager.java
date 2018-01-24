/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.webserver.pages.parsing.NetworkPage;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.webapi.WebAPIManager;
import com.djrapitops.plan.system.webserver.webapi.bukkit.AnalysisReadyWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bukkit.AnalyzeWebAPI;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages information going to the ResponseCache from Bukkit servers.
 *
 * @author Rsl1122
 */
@Deprecated
public class BungeeInformationManager extends InformationManager {

    private final PlanBungee plugin;
    private final BungeeServerInfo serverInfoManager;
    private Map<UUID, Server> bukkitServers;

    public BungeeInformationManager(PlanBungee plugin) {
        usingAnotherWebServer = false;
        this.plugin = plugin;
        serverInfoManager = plugin.getServerInfoManager();
        refreshBukkitServerMap();
    }

    /**
     * Refreshes the Offline Bukkit server Map (UUID - Server Address Map) from DB.
     *
     * @throws SQLException If DB Error occurs.
     */
    private void refreshBukkitServerMap() {
        bukkitServers = plugin.getDB().getServerTable().getBukkitServers().stream().collect(Collectors.toMap(Server::getUuid, Function.identity()));
    }

    /**
     * Sends a "Refresh Analysis" WebAPI call to the appropriate Bukkit server.
     * <p>
     * if server is not online, api request will not be made.
     *
     * @param serverUUID Server UUID of the server in question.
     */
    @Override
    public void refreshAnalysis(UUID serverUUID) {
        if (PlanBungee.getServerUUID().equals(serverUUID)) {
            return;
        }
        Server server = getOnlineServerInfo(serverUUID);
        if (server == null) {
            return;
        }

        AnalyzeWebAPI api = plugin.getWebServer().getWebAPI().getAPI(AnalyzeWebAPI.class);
        try {
            api.sendRequest(server.getWebAddress(), serverUUID);
        } catch (ConnectionFailException e) {
            attemptConnection();
        } catch (WebException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get info of a server that is online.
     * <p>
     * Returns null if server doesn't exist.
     *
     * @param serverUUID UUID of server
     * @return Online Server or null
     */
    private Server getOnlineServerInfo(UUID serverUUID) {
        Server server = bukkitServers.get(serverUUID);
        if (server == null) {
            try {
                refreshBukkitServerMap();
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
            server = bukkitServers.get(serverUUID);
        }
        if (server == null) {
            return null;
        }

        if (serverInfoManager.getOnlineBukkitServers().contains(server) || serverInfoManager.attemptConnection(server)) {
            return server;
        }
        return null;
    }

    /**
     * Attempts a connection to every Bukkit server in the database.
     *
     * @return true (always)
     */
    @Override
    public boolean attemptConnection() {
        try {
            List<Server> bukkitServers = plugin.getDB().getServerTable().getBukkitServers();
            for (Server server : bukkitServers) {
                serverInfoManager.attemptConnection(server);
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return true;
    }

    /**
     * Get the Network page html.
     *
     * @return Html string (Full page)
     */
    @Override
    public String getAnalysisHtml() {
        try {
            return new NetworkPage(plugin).toHtml();
        } catch (ParseException e) {
            return new InternalErrorResponse(e, this.getClass().getSimpleName()).getContent();
        }
    }

    /**
     * Shortcut for getting WebAPIManager
     *
     * @return WebAPIManager
     */
    private WebAPIManager getWebAPI() {
        return plugin.getWebServer().getWebAPI();
    }

    /**
     * Get address of Bungee WebServer.
     *
     * @return URL String
     */
    @Override
    public String getWebServerAddress() {
        return plugin.getWebServer().getAccessAddress();
    }

    /**
     * Send notification of analysis being ready to all online bukkit servers via WebAPI.
     *
     * @param serverUUID UUID of a server which analysis is ready.
     */
    @Override
    public void analysisReady(UUID serverUUID) {
        AnalysisReadyWebAPI api = getWebAPI().getAPI(AnalysisReadyWebAPI.class);
        for (Server server : serverInfoManager.getOnlineBukkitServers()) {
            try {
                api.sendRequest(server.getWebAddress(), serverUUID);
            } catch (WebException ignored) {
                /*Ignored*/
            }
        }
    }

    public void sendConfigSettings() {
        Collection<Server> online = serverInfoManager.getOnlineBukkitServers();
        online.stream().map(Server::getUuid)
                .forEach(serverInfoManager::sendConfigSettings);
    }

}
