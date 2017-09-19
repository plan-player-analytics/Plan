/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIConnectionFailException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPINotFoundException;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfo;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.InspectPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.NotFoundResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
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

    private PlanBungee plugin;
    private Map<UUID, ServerInfo> bukkitServers;

    private Map<UUID, Map<UUID, String>> pluginsTabContent;

    public BungeeInformationManager(PlanBungee plugin) throws SQLException {
        usingAnotherWebServer = false;
        pluginsTabContent = new HashMap<>();
        this.plugin = plugin;
        refreshBukkitServerMap();
    }

    private void refreshBukkitServerMap() throws SQLException {
        bukkitServers = plugin.getDB().getServerTable().getBukkitServers().stream().collect(Collectors.toMap(ServerInfo::getUuid, Function.identity()));
    }

    @Override
    public void refreshAnalysis() {
        // TODO Refresh network page
    }

    public void refreshAnalysis(UUID serverUUID) {
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
            return;
        }

        AnalyzeWebAPI api = plugin.getWebServer().getWebAPI().getAPI(AnalyzeWebAPI.class);
        try {
            api.sendRequest(serverInfo.getWebAddress());
        } catch (WebAPIConnectionFailException e) {
            attemptConnection();
        } catch (WebAPIException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    @Override
    public void cachePlayer(UUID uuid) {
        ServerInfo inspectServer = null;
        try {
            inspectServer = getInspectRequestProcessorServer(uuid);

            WebAPIManager apiManager = getWebAPI();

            apiManager.getAPI(InspectWebAPI.class).sendRequest(inspectServer.getWebAddress(), uuid);
            apiManager.getAPI(RequestPluginsTabWebAPI.class).sendRequestsToBukkitServers(plugin, uuid);
        } catch (IllegalStateException e) {
            Log.error("Attempted to process Inspect request with 0 Bukkit servers online.");
        } catch (WebAPIException e) {
            plugin.getServerInfoManager().attemptConnection(inspectServer);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public ServerInfo getInspectRequestProcessorServer(UUID uuid) throws SQLException {
        if (bukkitServers.isEmpty()) {
            throw new IllegalStateException("No Bukkit Servers.");
        }
        Collection<ServerInfo> bukkitServers = plugin.getServerInfoManager().getOnlineBukkitServers();
        for (ServerInfo server : bukkitServers) {
            try {
                getWebAPI().getAPI(IsOnlineWebAPI.class).sendRequest(server.getWebAddress(), uuid);
                return server;
            } catch (WebAPINotFoundException e) {
                    /*continue*/
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }

        Optional<ServerInfo> bukkitServer = bukkitServers.stream().findAny();
        if (bukkitServer.isPresent()) {
            return bukkitServer.get();
        }
        throw new IllegalStateException("No Bukkit servers online");
    }

    @Override
    public DataCache getDataCache() {
        return null;
    }

    @Override
    public boolean attemptConnection() {
        try {
            List<ServerInfo> bukkitServers = plugin.getDB().getServerTable().getBukkitServers();
            for (ServerInfo server : bukkitServers) {
                plugin.getServerInfoManager().attemptConnection(server);
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return true;
    }

    @Override
    public boolean isAnalysisCached(UUID serverUUID) {
        if (PlanBungee.getServerUUID().equals(serverUUID)) {
            return PageCache.isCached("networkPage");
        } else {
            return PageCache.isCached("analysisPage:" + serverUUID);
        }
    }

    @Override
    public String getPlayerHtml(UUID uuid) {
        Response response = PageCache.loadPage("inspectPage:" + uuid,
                () -> new NotFoundResponse("No Bukkit Servers were online to process this request"));
        if (response instanceof InspectPageResponse) {
            ((InspectPageResponse) response).setInspectPagePluginsTab(pluginsTabContent.get(uuid));
        }
        return response.getContent();
    }

    @Override
    public String getAnalysisHtml() {
        return new NotFoundResponse("Network page not yet created").getContent();
    }

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

    public void cachePluginsTabContent(UUID serverUUID, UUID uuid, String html) {
        Map<UUID, String> perServerPluginsTab = pluginsTabContent.getOrDefault(uuid, new HashMap<>());
        perServerPluginsTab.put(serverUUID, html);
        pluginsTabContent.put(uuid, perServerPluginsTab);
    }

    private WebAPIManager getWebAPI() {
        return plugin.getWebServer().getWebAPI();
    }

    @Override
    public String getWebServerAddress() {
        return plugin.getWebServer().getAccessAddress();
    }
}