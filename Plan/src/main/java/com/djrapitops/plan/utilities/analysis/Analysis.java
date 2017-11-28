package main.java.com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.ServerProfile;
import main.java.com.djrapitops.plan.data.additional.AnalysisContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.SessionCache;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.response.ErrorResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.InternalErrorResponse;

import java.util.*;

/**
 * @author Rsl1122
 */
public class Analysis {

    private final Plan plugin;
    private int taskId = -1;
    private static ServerProfile serverProfile;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Analysis(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Analyzes the data of all offline players on the server.
     *
     * @param infoManager InformationManager of the plugin.
     */
    public void runAnalysis(InformationManager infoManager) {
        if (isAnalysisBeingRun()) {
            return;
        }

        Benchmark.start("Analysis");
        log(Locale.get(Msg.ANALYSIS_START).toString());
        // Async task for Analysis
        RunnableFactory.createNew(new AbsRunnable("AnalysisTask") {
            @Override
            public void run() {
                try {
                    ErrorResponse analysisRefreshPage = new ErrorResponse();
                    analysisRefreshPage.setTitle("Analysis is being refreshed..");
                    analysisRefreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"25\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
                    analysisRefreshPage.replacePlaceholders();
                    ((BukkitInformationManager) plugin.getInfoManager()).cacheAnalysisHtml(analysisRefreshPage.getContent());
                    taskId = this.getTaskId();
                    analyze(infoManager, plugin.getDB());
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + ":" + this.getTaskName(), e);
                } finally {
                    taskId = -1;
                    this.cancel();
                }

            }
        }).runTaskAsynchronously();
    }

    /**
     * Caches analyzed data of db to the provided cache analysisCache.
     *
     * @param infoManager InformationManager of the plugin.
     *                    method.
     * @param db          Database which data will be analyzed.
     * @return Whether or not analysis was successful.
     */
    public boolean analyze(InformationManager infoManager, Database db) {
        log(Locale.get(Msg.ANALYSIS_FETCH).toString());
        Benchmark.start("Fetch Phase");
        Log.logDebug("Database", "Analysis Fetch");
        Log.logDebug("Analysis", "Analysis Fetch Phase");


        return analyzeData(infoManager, db);
    }

    /**
     * Analyze data in the db about this server.
     *
     * @param infoManager InformationManager of the plugin.
     * @return Success?
     */
    public boolean analyzeData(InformationManager infoManager, Database db) {
        try {
            Benchmark.start("Create Empty dataset");

            AnalysisData analysisData = new AnalysisData();

            Benchmark.stop("Analysis", "Create Empty dataset");
            Benchmark.start("Fetch Phase");
            ServerProfile profile = db.getServerProfile(Plan.getServerUUID());
            DataCache dataCache = plugin.getDataCache();
            profile.addActiveSessions(new HashMap<>(SessionCache.getActiveSessions()));
            serverProfile = profile;

            for (PlayerProfile player : profile.getPlayers()) {

                dataCache.updateNames(player.getUuid(), player.getName(), null);
            }

            long fetchPhaseLength = Benchmark.stop("Analysis", "Fetch Phase");

            // TODO BanData (PluginData) effects

            Benchmark.start("Analysis Phase");
            Log.logDebug("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_PHASE_START).parse(profile.getPlayerCount(), fetchPhaseLength));

            analysisData.analyze(profile);

            Benchmark.stop("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_3RD_PARTY).toString());
            Log.logDebug("Analysis", "Analyzing additional data sources (3rd party)");
            analysisData.parsePluginsSection(analyzeAdditionalPluginData(profile.getUuids()));
            ((BukkitInformationManager) infoManager).cacheAnalysisData(analysisData);

            // TODO Export
//            ExportUtility.export(analysisData, rawData);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            ((BukkitInformationManager) plugin.getInfoManager()).cacheAnalysisHtml(new InternalErrorResponse(e, "Analysis").getContent());
            Log.logDebug("Analysis", "Error: " + e);
            return false;
        } finally {
            long time = Benchmark.stop("Analysis", "Analysis");
            Log.logDebug("Analysis");
            Log.info(Locale.get(Msg.ANALYSIS_FINISHED).parse(time, ""));
            serverProfile = null;
        }
        return true;
    }

    private void log(String msg) {
        if (Settings.ANALYSIS_LOG.isTrue()) {
            Log.info(msg);
        }
    }

    private Map<PluginData, AnalysisContainer> analyzeAdditionalPluginData(Set<UUID> uuids) {
        Map<PluginData, AnalysisContainer> containers = new HashMap<>();

        Benchmark.start("Analysis", "3rd party Analysis");
        List<PluginData> sources = plugin.getHookHandler().getAdditionalDataSources();

        Log.logDebug("Analysis", "Additional Sources: " + sources.size());
        sources.parallelStream().forEach(source -> {
            StaticHolder.saveInstance(this.getClass(), plugin.getClass());
            try {
                Benchmark.start("Analysis", "Source " + source.getSourcePlugin());

                AnalysisContainer container = source.getServerData(uuids, new AnalysisContainer());
                if (container != null && !container.isEmpty()) {
                    containers.put(source, container);
                }

            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                Log.error("A PluginData-source caused an exception: " + source.getSourcePlugin());
                Log.toLog(this.getClass().getName(), e);
            } finally {
                Benchmark.stop("Analysis", "Source " + source.getSourcePlugin());
            }
        });
        Benchmark.stop("Analysis", "3rd party Analysis");
        return containers;
    }

    /**
     * Condition whether or not analysis is being run.
     *
     * @return true / false (state)
     */
    public boolean isAnalysisBeingRun() {
        return taskId != -1;
    }

    public void setTaskId(int id) {
        taskId = id;
    }

    /**
     * Only available during Analysis.
     *
     * @return ServerProfile being analyzed or null if analysis is not being run.
     */
    public static ServerProfile getServerProfile() {
        return serverProfile;
    }
}
