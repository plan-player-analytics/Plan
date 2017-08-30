/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info;

import com.djrapitops.plugin.command.ISender;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.bungee.PlanBungee;
import main.java.com.djrapitops.plan.command.commands.AnalyzeCommand;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.systems.info.parsing.AnalysisPageParser;
import main.java.com.djrapitops.plan.systems.info.parsing.InspectPageParser;
import main.java.com.djrapitops.plan.systems.info.parsing.UrlParser;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class InformationManager {
    // TODO Class that manages ALL information for API, WebAPI requests, Command Caching etc.
    private Plan plugin;
    private Database db;

    private DataCache dataCache;

    private boolean usingBungeeWebServer;
    private String webServerAddress;

    private Set<ISender> analysisNotification;
    private Analysis analysis;
    private AnalysisData analysisData;
    private String analysisPluginsTab;
    private Long refreshDate;

    public InformationManager(Plan plugin) {
        this.plugin = plugin;
        db = plugin.getDB();

        plugin.getServerInfoManager().getBungeeConnectionAddress()
                .ifPresent(address -> webServerAddress = address);

        dataCache = new DataCache(plugin);
        analysis = new Analysis(plugin);
        analysisNotification = new HashSet<>();

        if (webServerAddress != null) {
            attemptBungeeConnection();
        }
    }

    public InformationManager(PlanBungee plugin) {
        // TODO Init info manager.
    }

    public void attemptBungeeConnection() {
        // TODO WebAPI bungee connection check
    }

    public void cachePlayer(UUID uuid) {
        plugin.addToProcessQueue(); // TODO Player page information parser
        // TODO Player page plugin tab request
    }

    public UrlParser getLinkTo(String target) {
        if (webServerAddress != null) {
            return new UrlParser(webServerAddress).target(target);
        } else {
            return new UrlParser("");
        }
    }

    public void refreshAnalysis() {
        analysis.runAnalysis(this);
    }

    public DataCache getDataCache() {
        return dataCache;
    }

    public SessionCache getSessionCache() {
        return dataCache;
    }

    public boolean isCached(UUID uuid) {
        if (usingBungeeWebServer) {
            // TODO bungee part
        }
        return PageCache.isCached("inspectPage: " + uuid);
    }

    public String getPlayerHtml(UUID uuid) {
        // TODO Bungee part.
        try {
            return new InspectPageParser(uuid, plugin).parse();
        } catch (ParseException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return "";
    }

    public boolean isAnalysisCached() {
        // TODO Bungee part
        return PageCache.isCached("analysisPage");
    }

    public String getAnalysisHtml() {
        // TODO Bungee part.
        try {
            return new AnalysisPageParser(analysisData, plugin).parse();
        } catch (ParseException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        return "";
    }

    public void cacheAnalysisdata(AnalysisData analysisData) {
        this.analysisData = analysisData;
        refreshDate = MiscUtils.getTime();
        // TODO Web Caching (Move from Analysis)
        AnalyzeCommand.sendAnalysisMessage(analysisNotification);
        analysisNotification.clear();
    }

    public void addAnalysisNotification(ISender sender) {
        analysisNotification.add(sender);
    }

    public AnalysisData getAnalysisData() {
        return analysisData;
    }

    public Optional<Long> getAnalysisRefreshDate() {
        return refreshDate != null ? Optional.of(refreshDate) : Optional.empty();
    }
}