package com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.calculation.AnalysisData;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.plugin.BanData;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.tasks.BukkitTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class Analysis {

    private static Long refreshDate;
    private final UUID serverUUID;
    private final Database database;

    private static ServerProfile serverProfile;
    private final DataCache dataCache;
    private boolean analysingThisServer;

    private Analysis(UUID serverUUID, Database database, DataCache dataCache) {
        this.serverUUID = serverUUID;
        analysingThisServer = ServerInfo.getServerUUID().equals(serverUUID);
        this.database = database;
        this.dataCache = dataCache;
    }

    public static Optional<Long> getRefreshDate() {
        return Optional.ofNullable(refreshDate);
    }

    public static AnalysisData runAnalysisFor(UUID serverUUID, Database database, DataCache dataCache) throws Exception {
        return new Analysis(serverUUID, database, dataCache).runAnalysis();
    }

    /**
     * Only available during Analysis.
     *
     * @return ServerProfile being analyzed or null if analysis is not being run.
     */
    public static ServerProfile getServerProfile() {
        return serverProfile;
    }

    private AnalysisData runAnalysis() throws Exception {
        ((BukkitTaskSystem) TaskSystem.getInstance()).cancelBootAnalysis();

        Benchmark.start("Analysis");
        log(Locale.get(Msg.ANALYSIS_START).toString());
        return analyze();
    }

    private AnalysisData analyze() throws Exception {
        log(Locale.get(Msg.ANALYSIS_FETCH).toString());
        Benchmark.start("Fetch Phase");
        Log.logDebug("Database", "Analysis Fetch");
        Log.logDebug("Analysis", "Analysis Fetch Phase");
        try {
            Benchmark.start("Create Empty dataset");

            AnalysisData analysisData = new AnalysisData();

            Benchmark.stop("Analysis", "Create Empty dataset");
            Benchmark.start("Fetch Phase");
            ServerProfile profile = database.fetch().getServerProfile(serverUUID);
            if (analysingThisServer) {
                profile.addActiveSessions(new HashMap<>(SessionCache.getActiveSessions()));
            }
            serverProfile = profile;

            updatePlayerNameCache(profile);

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
            return analysisData;
        } finally {
            refreshDate = MiscUtils.getTime();
            long time = Benchmark.stop("Analysis", "Analysis");
            Log.logDebug("Analysis");
            Log.info(Locale.get(Msg.ANALYSIS_FINISHED).parse(time, ""));
            serverProfile = null;
        }
    }

    private void updatePlayerNameCache(ServerProfile profile) {
        for (PlayerProfile player : profile.getPlayers()) {
            dataCache.updateNames(player.getUuid(), player.getName(), null);
        }
    }

    private void setBannedByPlugins(ServerProfile profile) {
        if (!analysingThisServer) {
            return;
        }
        List<BanData> banPlugins = HookHandler.getInstance().getAdditionalDataSources().stream()
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
        if (!analysingThisServer) {
            return new HashMap<>();
        }
        Map<PluginData, AnalysisContainer> containers = new HashMap<>();

        Benchmark.start("Analysis", "3rd party Analysis");
        List<PluginData> sources = HookHandler.getInstance().getAdditionalDataSources();

        Log.logDebug("Analysis", "Additional Sources: " + sources.size());
        sources.parallelStream().forEach(source -> {
            PlanPlugin plugin = PlanPlugin.getInstance();
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

    public static boolean isAnalysisBeingRun() {
        return serverProfile != null;
    }
}
