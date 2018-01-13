package com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.plugin.BanData;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plan.systems.cache.SessionCache;
import com.djrapitops.plan.systems.info.BukkitInformationManager;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plan.systems.tasks.PlanTaskSystem;
import com.djrapitops.plan.systems.tasks.TaskSystem;
import com.djrapitops.plan.systems.webserver.response.ErrorResponse;
import com.djrapitops.plan.systems.webserver.response.InternalErrorResponse;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.*;
import java.util.stream.Collectors;

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

        ((PlanTaskSystem) TaskSystem.getInstance()).cancelBootAnalysis();

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

            updatePlayerNameCache(profile, dataCache);

            long fetchPhaseLength = Benchmark.stop("Analysis", "Fetch Phase");
            setBannedByPlugins(profile);

            Benchmark.start("Analysis Phase");
            Log.logDebug("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_PHASE_START).parse(profile.getPlayerCount(), fetchPhaseLength));

            analysisData.analyze(profile);

            Benchmark.stop("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_3RD_PARTY).toString());
            Log.logDebug("Analysis", "Analyzing additional data sources (3rd party)");
            analysisData.parsePluginsSection(analyzeAdditionalPluginData(profile.getUuids()));
            ((BukkitInformationManager) infoManager).cacheAnalysisData(analysisData);
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

    private void updatePlayerNameCache(ServerProfile profile, DataCache dataCache) {
        for (PlayerProfile player : profile.getPlayers()) {
            dataCache.updateNames(player.getUuid(), player.getName(), null);
        }
    }

    private void setBannedByPlugins(ServerProfile profile) {
        UUID serverUUID = Plan.getServerUUID();
        List<BanData> banPlugins = plugin.getHookHandler().getAdditionalDataSources().stream()
                .filter(p -> p instanceof BanData)
                .map(p -> (BanData) p)
                .collect(Collectors.toList());

        Set<UUID> banned = new HashSet<>();
        for (BanData banPlugin : banPlugins) {
            Set<UUID> uuids = profile.getUuids();
            try {
                banned.addAll(banPlugin.filterBanned(uuids));
            } catch (Exception | NoSuchMethodError | NoClassDefFoundError | NoSuchFieldError e) {
                Log.toLog("PluginData caused exception: " + banPlugin.getClass().getName(), e);
            }
        }

        profile.getPlayers().stream().filter(player -> banned.contains(player.getUuid()))
                .forEach(player -> player.bannedOnServer(serverUUID));
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

    /**
     * Only available during Analysis.
     *
     * @return ServerProfile being analyzed or null if analysis is not being run.
     */
    public static ServerProfile getServerProfile() {
        return serverProfile;
    }
}
