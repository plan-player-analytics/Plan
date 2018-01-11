/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.WebAPIConnectionFailException;
import com.djrapitops.plan.api.exceptions.WebAPIException;
import com.djrapitops.plan.api.exceptions.WebAPINotFoundException;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.pagecache.PageCache;
import com.djrapitops.plan.system.webserver.pagecache.PageId;
import com.djrapitops.plan.system.webserver.response.*;
import com.djrapitops.plan.system.webserver.webapi.WebAPIManager;
import com.djrapitops.plan.system.webserver.webapi.bukkit.AnalysisReadyWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bukkit.AnalyzeWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bukkit.InspectWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bukkit.IsOnlineWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bungee.RequestPluginsTabWebAPI;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plan.systems.info.parsing.NetworkPageParser;
import com.djrapitops.plan.systems.info.server.BungeeServerInfoManager;
import com.djrapitops.plan.systems.info.server.ServerInfo;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plugin.api.utility.log.ErrorLogger;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;
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
    private final Map<UUID, Map<UUID, String[]>> pluginsTabContent;
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
        if (PlanBungee.getServerUUID().equals(serverUUID)) {
            return;
        }
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
            try {
                refreshBukkitServerMap();
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
            if (bukkitServers.isEmpty()) {
                throw new IllegalStateException("No Bukkit Servers.");
            }
        }

        Collection<ServerInfo> onlineServers = serverInfoManager.getOnlineBukkitServers();
        if (plugin.getProxy().getPlayer(uuid) != null) {
            for (ServerInfo server : onlineServers) {
                try {
                    getWebAPI().getAPI(IsOnlineWebAPI.class).sendRequest(server.getWebAddress(), uuid);
                    return server;
                } catch (WebAPIConnectionFailException e) {
                    serverInfoManager.serverHasGoneOffline(server.getUuid());
                } catch (WebAPINotFoundException ignored) {
                    /*continue*/
                } catch (WebAPIException e) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
        }

        Optional<ServerInfo> bukkitServer = serverInfoManager.getOnlineBukkitServers().stream().findAny();
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
     * Condition if analysis page for an UUID is cached.
     * <p>
     * If serverUUID is that of Bungee, network page state is returned.
     *
     * @param serverUUID UUID of the server
     * @return true/false
     */
    @Override
    public boolean isAnalysisCached(UUID serverUUID) {
        return PageCache.isCached(PageId.SERVER.of(serverUUID));
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
        Response response = PageCache.copyPage(PageId.PLAYER.of(uuid),
                () -> new NotFoundResponse("No Bukkit Servers were online to process this request"));
        if (response instanceof InspectPageResponse) {
            ((InspectPageResponse) response).setInspectPagePluginsTab(getPluginsTabContent(uuid));
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
    public String[] getPluginsTabContent(UUID uuid) {
        Map<UUID, String[]> pluginsTab = pluginsTabContent.get(uuid);
        if (pluginsTab == null) {
            return HtmlStructure.createInspectPageTabContentCalculating();
        }

        List<String[]> order = new ArrayList<>(pluginsTab.values());
        // Sort serverNames alphabetically
        order.sort(new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                return o1[0].compareTo(o2[1]);
            }
        });

        StringBuilder nav = new StringBuilder();
        StringBuilder tabs = new StringBuilder();
        for (String[] tab : order) {
            nav.append(tab[0]);
            tabs.append(tab[1]);
        }
        return new String[]{nav.toString(), tabs.toString()};
    }

    /**
     * Places plugins tab content for a single player to the pluginsTabContent map.
     *
     * @param serverUUID UUID of the server
     * @param uuid       UUID of the player
     * @param html       Plugins tab html for the player on the server
     */
    public void cachePluginsTabContent(UUID serverUUID, UUID uuid, String[] html) {
        Map<UUID, String[]> perServerPluginsTab = pluginsTabContent.getOrDefault(uuid, new HashMap<>());
        perServerPluginsTab.put(serverUUID, html);
        pluginsTabContent.put(uuid, perServerPluginsTab);
        Response inspectResponse = PageCache.loadPage(PageId.PLAYER.of(uuid));
        if (inspectResponse != null && inspectResponse instanceof InspectPageResponse) {
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

    public void cacheNetworkPageContent(UUID serverUUID, String html) {
        networkPageContent.put(serverUUID, html);
        updateNetworkPageContent();
    }

    public void removeNetworkPageContent(UUID serverUUID) {
        networkPageContent.put(serverUUID, HtmlStructure.parseOfflineServerContainer(networkPageContent.get(serverUUID)));
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

    @Override
    public void updateNetworkPageContent() {
        UUID serverUUID = PlanPlugin.getInstance().getServerUuid();
        PageCache.cachePage(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(this));
        if (Settings.ANALYSIS_EXPORT.isTrue()) {
            HtmlExport.exportServer(plugin, serverUUID);
        }
    }

    public void sendConfigSettings() {
        Collection<ServerInfo> online = serverInfoManager.getOnlineBukkitServers();
        online.stream().map(ServerInfo::getUuid)
                .forEach(serverInfoManager::sendConfigSettings);
    }

    @Override
    public TreeMap<String, List<String>> getErrors() throws IOException {
        // TODO Request Bukkit servers for errors
        return ErrorLogger.getLoggedErrors(plugin);
    }
}
