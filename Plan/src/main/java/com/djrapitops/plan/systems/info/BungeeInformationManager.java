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
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.AnalyzeWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.IsOnlineWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.RequestPluginsTabWebAPI;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BungeeInformationManager extends InformationManager {

    private PlanBungee plugin;
    private Map<UUID, ServerInfo> bukkitServers;

    private Map<UUID, Map<UUID, String>> pluginsTabContent;

    public BungeeInformationManager(PlanBungee plugin) throws SQLException {
        usingBungeeWebServer = true;
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
        // TODO Request Inspect from server where the player is online or any if offline
//        PageCache.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(this, uuid));
        getWebAPI().getAPI(RequestPluginsTabWebAPI.class).sendRequestsToBukkitServers(plugin, uuid);
    }

    public ServerInfo getInspectRequestProcessorServer(UUID uuid) throws SQLException {
        List<ServerInfo> bukkitServers = plugin.getDB().getServerTable().getBukkitServers();
        for (ServerInfo server : bukkitServers) {
            try {
                getWebAPI().getAPI(IsOnlineWebAPI.class).sendRequest(server.getWebAddress());
                return server;
            } catch (WebAPINotFoundException e) {
                    /*continue*/
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        return bukkitServers.get(new Random().nextInt(bukkitServers.size()));
    }

    @Override
    public DataCache getDataCache() {
        return null;
    }

    @Override
    public boolean attemptConnection() {
        return false;
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
        return null;
    }

    @Override
    public String getAnalysisHtml() {
        return null;
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

}