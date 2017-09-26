/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIConnectionFailException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPINotFoundException;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.info.parsing.NetworkPageParser;
import main.java.com.djrapitops.plan.systems.info.server.BungeeServerInfoManager;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.InspectPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.InternalErrorResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.NotFoundResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.AnalysisReadyWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.AnalyzeWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.InspectWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.IsOnlineWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.RequestPluginsTabWebAPI;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages information going to the PageCache from Bukkit servers.
 *
 * @author Rsl1122
 */
public class BungeeInformationManager extends InformationManager {

    private final PlanBungee plugin;
    private Map<UUID, ServerInfo> bukkitServers;

    private final Map<UUID, String> networkPageContent;
    private final Map<UUID, Map<UUID, String>> pluginsTabContent;
    private final BungeeServerInfoManager serverInfoManager;

    public BungeeInformationManager(PlanBungee plugin) throws SQLException {
        usingAnotherWebServer = false;
        pluginsTabContent = new HashMap<>();
        networkPageContent = new HashMap<>();
        this.plugin = plugin;
        serverInfoManager = plugin.getServerInfoManager();
        refreshBukkitServerMap();
    }

    /**
     * Refreshes the Offline Bukkit server Map (UUID - Server Address Map) from DB.
     *
     * @throws SQLException If DB Error occurs.
     */
    private void refreshBukkitServerMap() throws SQLException {
        bukkitServers = plugin.getDB().getServerTable().getBukkitServers().stream().collect(Collectors.toMap(ServerInfo::getUuid, Function.identity()));
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
        ServerInfo serverInfo = getOnlineServerInfo(serverUUID);
        if (serverInfo == null) {
            return;
        }

        AnalyzeWebAPI api = plugin.getWebServer().getWebAPI().getAPI(AnalyzeWebAPI.class);
        try {
            api.sendRequest(serverInfo.getWebAddress(), serverUUID);
        } catch (WebAPIConnectionFailException e) {
            attemptConnection();
        } catch (WebAPIException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    /**
     * Attempts to get info of a server that is online.
     * <p>
     * Returns null if server doesn't exist.
     *
     * @param serverUUID UUID of server
     * @return Online ServerInfo or null
     */
    private ServerInfo getOnlineServerInfo(UUID serverUUID) {
        ServerInfo serverInfo = bukkitServers.get(serverUUID);
        if (serverInfo == null) {
            try {
                refreshBukkitServerMap();
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
            serverInfo = bukkitServers.get(serverUUID);
        }
        if (serverInfo == null) {
            return null;
        }

        if (serverInfoManager.getOnlineBukkitServers().contains(serverInfo) || serverInfoManager.attemptConnection(serverInfo)) {
            return serverInfo;
        }
        return null;
    }

    /**
     * Caches the inspect page for a matching player.
     * <p>
     * Attempt is made to use the server where the player is online.
     * <p>
     * If there is no Bukkit server to handle the request it is not fulfilled.
     *
     * @param uuid UUID of a player.
     */
    @Override
    public void cachePlayer(UUID uuid) {
        ServerInfo inspectServer = null;
        try {
            inspectServer = getInspectRequestProcessorServer(uuid);

            WebAPIManager apiManager = getWebAPI();

            apiManager.getAPI(InspectWebAPI.class).sendRequest(inspectServer.getWebAddress(), uuid);
            apiManager.getAPI(RequestPluginsTabWebAPI.class).sendRequestsToBukkitServers(plugin, uuid);
        } catch (IllegalStateException ignored) {
            /* Ignored */
        } catch (WebAPIException e) {
            plugin.getServerInfoManager().attemptConnection(inspectServer);
        }
    }

    /**
     * Get ServerInfo of an online server that should process an inspect request.
     * <p>
     * If the player is online, an attempt to use the server where the player resides is made.
     * <p>
     * If the player is offline or in the lobby, any server can be used.
     *
     * @param uuid UUID of the player
     * @return ServerInfo of the server that should handle the request.
     * @throws IllegalStateException If no Bukkit servers are online.
     */
    private ServerInfo getInspectRequestProcessorServer(UUID uuid) {
        if (bukkitServers.isEmpty()) {
            throw new IllegalStateException("No Bukkit Servers.");
        }

        Collection<ServerInfo> onlineServers = serverInfoManager.getOnlineBukkitServers();
        if (plugin.getProxy().getPlayer(uuid) != null) {
            for (ServerInfo server : onlineServers) {
                try {
                    getWebAPI().getAPI(IsOnlineWebAPI.class).sendRequest(server.getWebAddress(), uuid);
                    return server;
                } catch (WebAPINotFoundException ignored) {
                    /*continue*/
                } catch (WebAPIException e) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
        }

        Optional<ServerInfo> bukkitServer = onlineServers.stream().findAny();
        if (bukkitServer.isPresent()) {
            return bukkitServer.get();
        }
        throw new IllegalStateException("No Bukkit servers online");
    }

    /**
     * PlanBungee has no DataCache so this method should not be used.
     * <p>
     * DataCache is meant for storing player data.
     *
     * @return null
     */
    @Override
    public DataCache getDataCache() {
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
            List<ServerInfo> bukkitServers = plugin.getDB().getServerTable().getBukkitServers();
            for (ServerInfo server : bukkitServers) {
                serverInfoManager.attemptConnection(server);
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return true;
    }

    /**
     * Check if analysis page for an UUID is cached.
     * <p>
     * If serverUUID is that of Bungee, network page state is returned.
     *
     * @param serverUUID UUID of the server
     * @return true/false
     */
    @Override
    public boolean isAnalysisCached(UUID serverUUID) {
        if (PlanBungee.getServerUUID().equals(serverUUID)) {
            return PageCache.isCached("networkPage");
        } else {
            return PageCache.isCached("analysisPage:" + serverUUID);
        }
    }

    /**
     * Returns the Html players inspect page.
     * <p>
     * If no Bukkit servers are online a 404 is returned instead.
     *
     * @param uuid UUID of the player
     * @return Html string (Full page)
     */
    @Override
    public String getPlayerHtml(UUID uuid) {
        Response response = PageCache.loadPage("inspectPage:" + uuid,
                () -> new NotFoundResponse("No Bukkit Servers were online to process this request"));
        if (response instanceof InspectPageResponse) {
            ((InspectPageResponse) response).setInspectPagePluginsTab(pluginsTabContent.get(uuid));
        }
        return response.getContent();
    }

    /**
     * Get the Network page html.
     *
     * @return Html string (Full page)
     */
    @Override
    public String getAnalysisHtml() {
        try {
            return new NetworkPageParser(plugin).parse();
        } catch (ParseException e) {
            return new InternalErrorResponse(e, this.getClass().getSimpleName()).getContent();
        }
    }

    /**
     * Used to parse the Plugins tab html String out of all sent to Bungee.
     *
     * @param uuid UUID of the player
     * @return Html string.
     */
    @Override
    public String getPluginsTabContent(UUID uuid) {
        Map<UUID, String> pluginsTab = pluginsTabContent.get(uuid);
        if (pluginsTab == null) {
            return HtmlStructure.createInspectPageTabContentCalculating();
        }

        StringBuilder builder = new StringBuilder();
        for (String tab : pluginsTab.values()) {
            builder.append(tab);
        }
        return builder.toString();
    }

    /**
     * Places plugins tab content for a single player to the pluginsTabContent map.
     *
     * @param serverUUID UUID of the server
     * @param uuid       UUID of the player
     * @param html       Plugins tab html for the player on the server
     */
    public void cachePluginsTabContent(UUID serverUUID, UUID uuid, String html) {
        Map<UUID, String> perServerPluginsTab = pluginsTabContent.getOrDefault(uuid, new HashMap<>());
        perServerPluginsTab.put(serverUUID, html);
        pluginsTabContent.put(uuid, perServerPluginsTab);
        Response inspectResponse = PageCache.loadPage("inspectPage: " + uuid);
        if (inspectResponse != null) {
            ((InspectPageResponse) inspectResponse).setInspectPagePluginsTab(getPluginsTabContent(uuid));
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

    public void askForNetWorkPageContent() {
        // TODO WebAPI for network page content
    }

    public void cacheNetworkPageContent(UUID serverUUID, String html) {
        networkPageContent.put(serverUUID, html);
    }

    public void removeNetworkPageContent(UUID serverUUID) {
        networkPageContent.remove(serverUUID);
    }

    public Map<UUID, String> getNetworkPageContent() {
        return networkPageContent;
    }

    /**
     * Send notification of analysis being ready to all online bukkit servers via WebAPI.
     *
     * @param serverUUID UUID of a server which analysis is ready.
     */
    @Override
    public void analysisReady(UUID serverUUID) {
        AnalysisReadyWebAPI api = getWebAPI().getAPI(AnalysisReadyWebAPI.class);
        for (ServerInfo serverInfo : serverInfoManager.getOnlineBukkitServers()) {
            try {
                api.sendRequest(serverInfo.getWebAddress(), serverUUID);
            } catch (WebAPIException ignored) {
                /*Ignored*/
            }
        }
    }
}
