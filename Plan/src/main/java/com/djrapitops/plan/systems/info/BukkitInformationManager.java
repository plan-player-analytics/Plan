/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.connection.WebFailException;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plan.system.webserver.pages.parsing.AnalysisPage;
import com.djrapitops.plan.system.webserver.response.cache.PageId;
import com.djrapitops.plan.system.webserver.response.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.response.pages.AnalysisPageResponse;
import com.djrapitops.plan.system.webserver.webapi.WebAPIManager;
import com.djrapitops.plan.system.webserver.webapi.bukkit.AnalysisReadyWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bukkit.AnalyzeWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bungee.PostHtmlWebAPI;
import com.djrapitops.plan.system.webserver.webapi.bungee.PostOriginalBukkitSettingsWebAPI;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the Information going to the ResponseCache.
 * <p>
 * This means Inspect and Analysis pages as well as managing what is sent to Bungee WebServer when one is in use.
 *
 * @author Rsl1122
 */
@Deprecated
public class BukkitInformationManager extends InformationManager {

    private final Plan plugin;
    private final Analysis analysis;
    private AnalysisData analysisData;

    public BukkitInformationManager(Plan plugin) {
        this.plugin = plugin;
        analysis = new Analysis(plugin);
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
            analysis.runAnalysis();
        } else if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(AnalyzeWebAPI.class).sendRequest(webServerAddress, serverUUID);
            } catch (WebFailException e) {
                Log.error("Failed to request Analysis refresh from Bungee.");
            } catch (WebException e) {
                attemptConnection();
                refreshAnalysis(serverUUID);
            }
        }

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
            analysis.runAnalysis();
            ErrorResponse analysisRefreshPage = new ErrorResponse();
            analysisRefreshPage.setTitle("Analysis is being refreshed..");
            analysisRefreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"25\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
            analysisRefreshPage.replacePlaceholders();
            return analysisRefreshPage.getContent();
        }
        try {
            return Theme.replaceColors(new AnalysisPage(analysisData, plugin).toHtml());
        } catch (ParseException e) {
            return new InternalErrorResponse(e, this.getClass().getSimpleName()).getContent();
        }
    }

    public void cacheAnalysisData(AnalysisData analysisData) {
        this.analysisData = analysisData;
        refreshDate = MiscUtils.getTime();
        cacheAnalysisHtml();
        UUID serverUUID = Plan.getServerUUID();
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(AnalysisReadyWebAPI.class).sendRequest(webServerAddress, serverUUID);
//                updateNetworkPageContent();
                return;
            } catch (WebFailException ignored) {
                Log.error("Failed to notify Bungee of Analysis Completion.");
            } catch (WebException e) {
                attemptConnection();
            }
        }
        analysisReady(serverUUID);
    }

    private void cacheAnalysisHtml() {
        cacheAnalysisHtml(getAnalysisHtml());
    }

    @Deprecated
    public void cacheAnalysisHtml(String html) {
        if (usingAnotherWebServer) {
            try {
                getWebAPI().getAPI(PostHtmlWebAPI.class).sendAnalysisHtml(webServerAddress, html);
            } catch (WebFailException e) {
                Log.error("Failed to send Analysis HTML to Bungee Server.");
            } catch (WebException e) {
                attemptConnection();
                cacheAnalysisHtml(html);
            }
        } else {
            UUID serverUUID = Plan.getServerUUID();
            ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), () -> new AnalysisPageResponse(html));
            if (Settings.ANALYSIS_EXPORT.isTrue()) {
                HtmlExport.exportServer(serverUUID);
            }
        }
    }

    public AnalysisData getAnalysisData() {
        return analysisData;
    }

    @Override
    public boolean attemptConnection() {
        boolean webServerIsEnabled = WebServerSystem.isWebServerEnabled();
        boolean previousState = usingAnotherWebServer;

        try {
            Log.info("Attempting Bungee Connection.. (" + webServerAddress + ")");
//            PingWebAPI api = getWebAPI().getAPI(PingWebAPI.class);
            try {
//                api.sendRequest(webServerAddress);
                getWebAPI().getAPI(PostOriginalBukkitSettingsWebAPI.class).sendRequest(webServerAddress);
                Log.info("Bungee Connection OK");
                plugin.getServerInfoManager().resetConnectionFails();
                usingAnotherWebServer = true;
                return true;
            } catch (ConnectionFailException e) {
                plugin.getServerInfoManager().markConnectionFail();
            } catch (NotFoundException e) {
                Log.info("Bungee reported that UUID of this server is not in the MySQL-database. Try using '/plan m setup " + webServerAddress + "' again");
            } catch (WebException e) {
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
        analysisNotification.getOrDefault(serverUUID, new HashSet<>()).clear();
    }

}