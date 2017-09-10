/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.command.commands.AnalyzeCommand;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.info.parsing.AnalysisPageParser;
import main.java.com.djrapitops.plan.systems.info.parsing.InspectPageParser;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.InspectPageResponse;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;

import java.util.Optional;
import java.util.UUID;

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


    public BukkitInformationManager(Plan plugin) {
        this.plugin = plugin;
        dataCache = new DataCache(plugin);
        analysis = new Analysis(plugin);

        Optional<String> bungeeConnectionAddress = plugin.getServerInfoManager().getBungeeConnectionAddress();
        if (bungeeConnectionAddress.isPresent()) {
            webServerAddress = bungeeConnectionAddress.get();
            attemptConnection();
        } else {

        }

    }

    @Override
    public void refreshAnalysis() {
        plugin.getDataCache().cacheSavedNames();
        analysis.runAnalysis(this);
    }

    @Override
    public void cachePlayer(UUID uuid) {
        PageCache.loadPage("inspectPage: " + uuid, () -> new InspectPageResponse(this, uuid));
        // TODO Player page plugin tab request
    }

    @Override
    public boolean isCached(UUID uuid) {
        if (usingBungeeWebServer) {
            // TODO Check if cached on bungee
        }
        return super.isCached(uuid);
    }

    @Override
    public boolean isAnalysisCached() {
        if (usingBungeeWebServer) {
            // TODO Check if cached on bungee
        }
        return PageCache.isCached("analysisPage");
    }

    @Override
    public String getAnalysisHtml() {
        // TODO Bungee part.
        try {
            return new AnalysisPageParser(analysisData, plugin).parse();
        } catch (ParseException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return "";
    }

    @Override
    public String getPlayerHtml(UUID uuid) {
        if (usingBungeeWebServer) {
            // TODO Bungee request
        }
        try {
            return new InspectPageParser(uuid, plugin).parse();
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
        // TODO Web Caching (Move from Analysis)
        AnalyzeCommand.sendAnalysisMessage(analysisNotification);
        analysisNotification.clear();
    }

    public AnalysisData getAnalysisData() {
        return analysisData;
    }

    public Optional<Long> getAnalysisRefreshDate() {
        return refreshDate != null ? Optional.of(refreshDate) : Optional.empty();
    }

    @Override
    public void attemptConnection() {
        usingBungeeWebServer = true;
        // TODO Check the connection
    }
}