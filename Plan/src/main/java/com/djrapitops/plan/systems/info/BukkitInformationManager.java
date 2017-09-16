/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIConnectionFailException;
import main.java.com.djrapitops.plan.api.exceptions.WebAPIException;
import main.java.com.djrapitops.plan.command.commands.AnalyzeCommand;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.info.parsing.AnalysisPageParser;
import main.java.com.djrapitops.plan.systems.info.parsing.InspectPageParser;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.AnalysisPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.InspectPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;
import main.java.com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bukkit.RequestInspectPluginsTabBukkitWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.IsCachedWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.PostHtmlWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.PostInspectPluginsTabWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.bungee.RequestPluginsTabWebAPI;
import main.java.com.djrapitops.plan.systems.webserver.webapi.universal.PingWebAPI;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;

import java.io.Serializable;
import java.util.*;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BukkitInformationManager extends InformationManager {

    private final Plan plugin;
    private final DataCache dataCache;
    private final Analysis analysis;

    private AnalysisData analysisData;
    private String analysisPluginsTab;
    private Long refreshDate;

    private final Map<UUID, String> pluginsTabContents;

    public BukkitInformationManager(Plan plugin) {
        this.plugin = plugin;
        dataCache = new DataCache(plugin);
        analysis = new Analysis(plugin);
        pluginsTabContents = new HashMap<>();

        Optional<String> bungeeConnectionAddress = plugin.getServerInfoManager().getBungeeConnectionAddress();
        if (bungeeConnectionAddress.isPresent()) {
            webServerAddress = bungeeConnectionAddress.get();
            usingBungeeWebServer = attemptConnection();
        } else {
            usingBungeeWebServer = false;
        }

    }

    @Override
    public void refreshAnalysis() {
        plugin.getDataCache().cacheSavedNames();
        analysis.runAnalysis(this);
    }

    @Override
    public void cachePlayer(UUID uuid) {
        if (usingBungeeWebServer) {
            try {
                getWebAPI().getAPI(PostHtmlWebAPI.class).sendInspectHtml(webServerAddress, uuid, getPlayerHtml(uuid));
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        } else {
            PageCache.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(this, uuid));
        }
        plugin.addToProcessQueue(new Processor<UUID>(uuid) {
            @Override
            public void process() {
                cacheInspectPluginsTab(object);
            }
        });
    }

    public void cacheInspectPluginsTab(UUID uuid) {
        cacheInspectPluginsTab(uuid, this.getClass());
    }

    public void cacheInspectPluginsTab(UUID uuid, Class origin) {
        if (usingBungeeWebServer && !origin.equals(RequestInspectPluginsTabBukkitWebAPI.class)) {
            try {
                getWebAPI().getAPI(RequestPluginsTabWebAPI.class).sendRequest(webServerAddress, uuid);
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        } else {
            String serverName = plugin.getServerInfoManager().getServerName();
            HookHandler hookHandler = plugin.getHookHandler();
            List<PluginData> plugins = hookHandler.getAdditionalDataSources();
            Map<String, Serializable> replaceMap = hookHandler.getAdditionalInspectReplaceRules(uuid);
            String contents = HtmlStructure.createInspectPageTabContent(serverName, plugins, replaceMap);
            cacheInspectPluginsTab(uuid, contents);
        }
    }

    public void cacheInspectPluginsTab(UUID uuid, String contents) {
        if (usingBungeeWebServer) {
            try {
                getWebAPI().getAPI(PostInspectPluginsTabWebAPI.class).sendPluginsTab(webServerAddress, uuid, contents);
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        } else {
            pluginsTabContents.put(uuid, contents);
            Response inspectResponse = PageCache.loadPage("inspectPage: " + uuid);
            if (inspectResponse != null) {
                ((InspectPageResponse) inspectResponse).setInspectPagePluginsTab(contents);
            }
        }
    }

    @Override
    public String getPluginsTabContent(UUID uuid) {
        String calculating = HtmlStructure.createInspectPageTabContentCalculating();
        return pluginsTabContents.getOrDefault(uuid, calculating);
    }

    @Override
    public boolean isCached(UUID uuid) {
        if (usingBungeeWebServer) {
            try {
                return getWebAPI().getAPI(IsCachedWebAPI.class).isInspectCached(webServerAddress, uuid);
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        return super.isCached(uuid);
    }

    @Override
    public boolean isAnalysisCached() {
        if (usingBungeeWebServer) {
            try {
                return getWebAPI().getAPI(IsCachedWebAPI.class).isAnalysisCached(webServerAddress);
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        return PageCache.isCached("analysisPage");
    }

    private WebAPIManager getWebAPI() {
        return plugin.getWebServer().getWebAPI();
    }

    @Override
    public String getAnalysisHtml() {
        try {
            return Theme.replaceColors(new AnalysisPageParser(analysisData, plugin).parse());
        } catch (ParseException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return "";
    }

    @Override
    public String getPlayerHtml(UUID uuid) {
        try {
            return Theme.replaceColors(new InspectPageParser(uuid, plugin).parse());
        } catch (ParseException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return "";
    }

    @Override
    public DataCache getDataCache() {
        return dataCache;
    }

    public void cacheAnalysisdata(AnalysisData analysisData) {
        this.analysisData = analysisData;
        refreshDate = MiscUtils.getTime();
        cacheAnalysisHtml();
        AnalyzeCommand.sendAnalysisMessage(analysisNotification);
        analysisNotification.clear();
    }

    private void cacheAnalysisHtml() {
        if (usingBungeeWebServer) {
            try {
                getWebAPI().getAPI(PostHtmlWebAPI.class).sendAnalysisHtml(webServerAddress, getAnalysisHtml());
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        } else {
            PageCache.cachePage("analysisPage", () -> new AnalysisPageResponse(this));
        }
    }

    public AnalysisData getAnalysisData() {
        return analysisData;
    }

    public Optional<Long> getAnalysisRefreshDate() {
        return refreshDate != null ? Optional.of(refreshDate) : Optional.empty();
    }

    @Override
    public boolean attemptConnection() {
        PingWebAPI api = getWebAPI().getAPI(PingWebAPI.class);
        try {
            api.sendRequest(webServerAddress);
            return true;
        } catch (WebAPIConnectionFailException e) {
            plugin.getServerInfoManager().markConnectionFail();
        } catch (WebAPIException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return false;
    }
}