/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.*;
import com.djrapitops.plan.command.commands.AnalyzeCommand;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.settings.Settings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plan.systems.info.parsing.AnalysisPageParser;
import com.djrapitops.plan.systems.info.parsing.InspectPageParser;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plan.systems.webserver.WebServer;
import com.djrapitops.plan.systems.webserver.WebServerSystem;
import com.djrapitops.plan.systems.webserver.pagecache.PageCache;
import com.djrapitops.plan.systems.webserver.pagecache.PageId;
import com.djrapitops.plan.systems.webserver.response.*;
import com.djrapitops.plan.systems.webserver.webapi.WebAPIManager;
import com.djrapitops.plan.systems.webserver.webapi.bukkit.AnalysisReadyWebAPI;
import com.djrapitops.plan.systems.webserver.webapi.bukkit.AnalyzeWebAPI;
import com.djrapitops.plan.systems.webserver.webapi.bukkit.RequestInspectPluginsTabBukkitWebAPI;
import com.djrapitops.plan.systems.webserver.webapi.bungee.*;
import com.djrapitops.plan.systems.webserver.webapi.universal.PingWebAPI;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.structure.InspectPluginsTabContentCreator;
import com.djrapitops.plugin.api.utility.log.ErrorLogger;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Manages the Information going to the PageCache.
 * <p>
 * This means Inspect and Analysis pages as well as managing what is sent to Bungee WebServer when one is in use.
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

    private final Map<UUID, String[]> pluginsTabContents;

    public BukkitInformationManager(Plan plugin) {
        this.plugin = plugin;
        dataCache = new DataCache(plugin);
        analysis = new Analysis(plugin);
        pluginsTabContents = new HashMap<>();
        usingAnotherWebServer = false;
    }

    public void updateConnection() {
        Optional<String> bungeeConnectionAddress = plugin.getServerInfoManager().getBungeeConnectionAddress();
        if (bungeeConnectionAddress.isPresent() && Settings.BUNGEE_OVERRIDE_STANDALONE_MODE.isFalse()) {
            webServerAddress = bungeeConnectionAddress.get();
            attemptConnection();
        } else {
            usingAnotherWebServer = false;
        }
    }

    @Override
    public void refreshAnalysis(UUID serverUUID) {
        if (Plan.getServerUUID().equals(serverUUID)) {
            plugin.getDataCache().cacheSavedNames();
            analysis.runAnalysis(this);
        } else if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(AnalyzeWebAPI.class).sendRequest(webServerAddress, serverUUID);
            } catch (WebAPIFailException e) {
                Log.error("Failed to request Analysis refresh from Bungee.");
            } catch (WebAPIException e) {
                attemptConnection();
                refreshAnalysis(serverUUID);
            }
        }

    }

    @Override
    public void cachePlayer(UUID uuid) {
        if (uuid == null) {
            Log.debug("BukkitInformationManager.cachePlayer: UUID was null");
            return;
        }
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(PostHtmlWebAPI.class).sendInspectHtml(webServerAddress, uuid, getPlayerHtml(uuid));
            } catch (WebAPIFailException e) {
                Log.error("Failed to request Inspect from Bungee.");
            } catch (WebAPIException e) {
                attemptConnection();
                cachePlayer(uuid);
            } catch (ParseException e) {
                if (!(e.getCause() instanceof IllegalStateException)) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
        } else {
            PageCache.cachePage(PageId.PLAYER.of(uuid), () -> {
                try {
                    return new InspectPageResponse(this, uuid);
                } catch (ParseException e) {
                    if (e.getCause() instanceof IllegalStateException) {
                        return new NotFoundResponse(
                                "Player just registered, so the data was not yet in the database. " +
                                        "Please wait until they log off or use /plan inspect <player>"
                        );
                    }
                    return new InternalErrorResponse(e, this.getClass().getName());
                }
            });
            if (Settings.ANALYSIS_EXPORT.isTrue()) {
                HtmlExport.exportPlayer(plugin, uuid);
            }
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
        if (usingAnotherWebServer && !origin.equals(RequestInspectPluginsTabBukkitWebAPI.class)) {
            try {
                getWebAPI().getAPI(RequestPluginsTabWebAPI.class).sendRequest(webServerAddress, uuid);
            } catch (WebAPIFailException e) {
                Log.error("Failed send Player Plugins tab contents to BungeeCord.");
            } catch (WebAPIException e) {
                attemptConnection();
                cacheInspectPluginsTab(uuid, origin);
            }
        } else {
            HookHandler hookHandler = plugin.getHookHandler();
            List<PluginData> plugins = hookHandler.getAdditionalDataSources();
            Map<PluginData, InspectContainer> containers = new HashMap<>();
            for (PluginData pluginData : plugins) {
                InspectContainer inspectContainer = new InspectContainer();
                try {
                    InspectContainer container = pluginData.getPlayerData(uuid, inspectContainer);
                    if (container != null && !container.isEmpty()) {
                        containers.put(pluginData, container);
                    }
                } catch (Exception e) {
                    String sourcePlugin = pluginData.getSourcePlugin();
                    Log.error("PluginData caused exception: " + sourcePlugin);
                    Log.toLog(this.getClass().getName() + " " + sourcePlugin, e);
                }
            }

            cacheInspectPluginsTab(uuid, InspectPluginsTabContentCreator.createContent(containers));
        }
    }

    public void cacheInspectPluginsTab(UUID uuid, String[] contents) {
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(PostInspectPluginsTabWebAPI.class).sendPluginsTab(webServerAddress, uuid, contents);
            } catch (WebAPIFailException e) {
                Log.error("Failed send Player HTML to BungeeCord.");
            } catch (WebAPIException e) {
                attemptConnection();
                cacheInspectPluginsTab(uuid, contents);
            }
        } else {
            pluginsTabContents.put(uuid, contents);
            Response inspectResponse = PageCache.loadPage(PageId.PLAYER.of(uuid));
            if (inspectResponse != null && inspectResponse instanceof InspectPageResponse) {
                ((InspectPageResponse) inspectResponse).setInspectPagePluginsTab(contents);
            }
        }
    }

    @Override
    public String[] getPluginsTabContent(UUID uuid) {
        String[] calculating = HtmlStructure.createInspectPageTabContentCalculating();
        return pluginsTabContents.getOrDefault(uuid, calculating);
    }

    @Override
    public boolean isCached(UUID uuid) {
        if (usingAnotherWebServer) {
            try {
                return getWebAPI().getAPI(IsCachedWebAPI.class).isInspectCached(webServerAddress, uuid);
            } catch (WebAPIFailException e) {
                Log.error("Failed check Bungee Player Cache status.");
            } catch (WebAPIException e) {
                attemptConnection();
                return isCached(uuid);
            }
        }
        return super.isCached(uuid);
    }

    @Override
    public boolean isAnalysisCached(UUID serverUUID) {
        if (Plan.getServerUUID().equals(serverUUID)) {
            return analysisData != null;
        }
        if (usingAnotherWebServer) {
            try {
                return getWebAPI().getAPI(IsCachedWebAPI.class).isAnalysisCached(webServerAddress, serverUUID);
            } catch (WebAPIFailException e) {
                Log.error("Failed check Bungee Analysis Cache status.");
            } catch (WebAPIException e) {
                attemptConnection();
                return isAnalysisCached(serverUUID);
            }
        }
        return PageCache.isCached(PageId.SERVER.of(serverUUID));
    }

    private WebAPIManager getWebAPI() {
        return plugin.getWebServer().getWebAPI();
    }

    /**
     * Get the HTML for analysis page of this server.
     *
     * @return Html for Analysis page
     * @throws NullPointerException if AnalysisData has not been cached.
     */
    @Override
    public String getAnalysisHtml() {
        if (analysisData == null) {
            analysis.runAnalysis(this);
            ErrorResponse analysisRefreshPage = new ErrorResponse();
            analysisRefreshPage.setTitle("Analysis is being refreshed..");
            analysisRefreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"25\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
            analysisRefreshPage.replacePlaceholders();
            return analysisRefreshPage.getContent();
        }
        try {
            return Theme.replaceColors(new AnalysisPageParser(analysisData, plugin).parse());
        } catch (ParseException e) {
            return new InternalErrorResponse(e, this.getClass().getSimpleName()).getContent();
        }
    }

    @Override
    public String getPlayerHtml(UUID uuid) throws ParseException {
        return Theme.replaceColors(new InspectPageParser(uuid, plugin).parse());
    }

    @Override
    public DataCache getDataCache() {
        return dataCache;
    }

    public void cacheAnalysisData(AnalysisData analysisData) {
        this.analysisData = analysisData;
        refreshDate = MiscUtils.getTime();
        cacheAnalysisHtml();
        UUID serverUUID = Plan.getServerUUID();
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(AnalysisReadyWebAPI.class).sendRequest(webServerAddress, serverUUID);
                updateNetworkPageContent();
                return;
            } catch (WebAPIFailException ignored) {
                Log.error("Failed to notify Bungee of Analysis Completion.");
            } catch (WebAPIException e) {
                attemptConnection();
            }
        }
        analysisReady(serverUUID);
    }

    private void cacheAnalysisHtml() {
        cacheAnalysisHtml(getAnalysisHtml());
    }

    public void cacheAnalysisHtml(String html) {
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(PostHtmlWebAPI.class).sendAnalysisHtml(webServerAddress, html);
            } catch (WebAPIFailException e) {
                Log.error("Failed to send Analysis HTML to Bungee Server.");
            } catch (WebAPIException e) {
                attemptConnection();
                cacheAnalysisHtml(html);
            }
        } else {
            UUID serverUUID = Plan.getServerUUID();
            PageCache.cachePage(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(html));
            if (Settings.ANALYSIS_EXPORT.isTrue()) {
                HtmlExport.exportServer(plugin, serverUUID);
            }
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
        boolean webServerIsEnabled = WebServerSystem.isWebServerEnabled();
        boolean previousState = usingAnotherWebServer;

        try {
            Log.info("Attempting Bungee Connection.. (" + webServerAddress + ")");
            PingWebAPI api = getWebAPI().getAPI(PingWebAPI.class);
            try {
                api.sendRequest(webServerAddress);
                getWebAPI().getAPI(PostOriginalBukkitSettingsWebAPI.class).sendRequest(webServerAddress);
                Log.info("Bungee Connection OK");
                plugin.getServerInfoManager().resetConnectionFails();
                usingAnotherWebServer = true;
                return true;
            } catch (WebAPIConnectionFailException e) {
                plugin.getServerInfoManager().markConnectionFail();
            } catch (WebAPINotFoundException e) {
                Log.info("Bungee reported that UUID of this server is not in the MySQL-database. Try using '/plan m setup " + webServerAddress + "' again");
            } catch (WebAPIException e) {
                Log.toLog(this.getClass().getName(), e);
            }
            Log.info("Bungee Connection Failed.");
            usingAnotherWebServer = false;
            return false;
        } finally {
            boolean changedState = previousState != usingAnotherWebServer;
            if (webServerIsEnabled && changedState) {
                WebServer webServer = WebServerSystem.getInstance().getWebServer();
                webServer.stop();
                webServer.initServer();
            }
        }
    }

    @Override
    public String getWebServerAddress() {
        return webServerAddress != null ? webServerAddress : plugin.getWebServer().getAccessAddress();
    }

    @Override
    public void analysisReady(UUID serverUUID) {
        try {
            AnalyzeCommand.sendAnalysisMessage(analysisNotification.get(serverUUID), serverUUID);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        analysisNotification.getOrDefault(serverUUID, new HashSet<>()).clear();
    }

    @Override
    public void updateNetworkPageContent() {
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(PostNetworkPageContentWebAPI.class).sendNetworkContent(webServerAddress, HtmlStructure.createServerContainer(plugin));
            } catch (WebAPIFailException ignored) {
                /* Do nothing */
            } catch (WebAPIException ignored) {
                attemptConnection();
                updateNetworkPageContent();
            }
        }
    }

    @Override
    public TreeMap<String, List<String>> getErrors() throws IOException {
        return ErrorLogger.getLoggedErrors(plugin);
    }
}