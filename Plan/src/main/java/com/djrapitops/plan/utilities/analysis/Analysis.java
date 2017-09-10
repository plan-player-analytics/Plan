package main.java.com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.*;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.data.analysis.*;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.TPSTable;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.response.AnalysisPageResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.PlayersPageResponse;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.comparators.UserInfoLastPlayedComparator;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.tables.PlayersTableCreator;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class Analysis {

    private final Plan plugin;
    private int taskId = -1;

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
        plugin.getRunnableFactory().createNew(new AbsRunnable("AnalysisTask") {
            @Override
            public void run() {
                taskId = this.getTaskId();
                analyze(infoManager, plugin.getDB());
                taskId = -1;
                this.cancel();
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
        Log.debug("Database", "Analysis Fetch");
        Log.debug("Analysis", "Analysis Fetch Phase");


        return analyzeData(infoManager, db);
    }

    /**
     * @param infoManager InformationManager of the plugin.
     * @return
     */
    public boolean analyzeData(InformationManager infoManager, Database db) {
        try {
            Benchmark.start("Create Empty dataset");

            AnalysisData analysisData = new AnalysisData();
            List<PluginData> thirdPartyPlugins = plugin.getHookHandler().getAdditionalDataSources();
            analysisData.setPluginsTabLayout(HtmlStructure.createAnalysisPluginsTabLayout(thirdPartyPlugins));
            analysisData.setPlanVersion(plugin.getVersion());

            Benchmark.stop("Analysis", "Create Empty dataset");
            fillDataset(analysisData, db);
            long fetchPhaseLength = Benchmark.stop("Analysis", "Fetch Phase");

            Benchmark.start("Analysis Phase");
            Log.debug("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_PHASE_START).parse(analysisData.getPlayerCountPart().getPlayerCount(), fetchPhaseLength));
            analysisData.analyseData();
            Benchmark.stop("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_3RD_PARTY).toString());
            Log.debug("Analysis", "Analyzing additional data sources (3rd party)");
            analysisData.setAdditionalDataReplaceMap(analyzeAdditionalPluginData(analysisData.getPlayerCountPart().getUuids()));
            ((BukkitInformationManager) infoManager).cacheAnalysisdata(analysisData);
            long time = Benchmark.stop("Analysis", "Analysis");

            Log.logDebug("Analysis", time);

            Log.info(Locale.get(Msg.ANALYSIS_FINISHED).parse(String.valueOf(time), HtmlUtils.getServerAnalysisUrlWithProtocol()));

            PageCache.removeIf(identifier -> identifier.startsWith("inspectPage: ") || identifier.startsWith("inspectionJson: "));
            PageCache.cachePage("analysisPage", () -> new AnalysisPageResponse(plugin.getInfoManager()));
            PageCache.cachePage("players", PlayersPageResponse::new);

            // TODO Export
//            ExportUtility.export(analysisData, rawData);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.debug("Analysis", "Error: " + e);
            Log.logDebug("Analysis");
            return false;
        }
        return true;
    }

    private void log(String msg) {
        Log.info(msg);
    }

    private Map<String, Serializable> analyzeAdditionalPluginData(Set<UUID> uuids) {
        Benchmark.start("3rd party");
        final Map<String, Serializable> replaceMap = new HashMap<>();
        final HookHandler hookHandler = plugin.getHookHandler();
        final List<PluginData> sources = hookHandler.getAdditionalDataSources().stream()
                .filter(p -> !p.isBanData())
                .filter(p -> !p.getAnalysisTypes().isEmpty())
                .collect(Collectors.toList());
        final AnalysisType[] totalTypes = new AnalysisType[]{
                AnalysisType.INT_TOTAL, AnalysisType.LONG_TOTAL, AnalysisType.LONG_TIME_MS_TOTAL, AnalysisType.DOUBLE_TOTAL
        };
        final AnalysisType[] avgTypes = new AnalysisType[]{
                AnalysisType.INT_AVG, AnalysisType.LONG_AVG, AnalysisType.LONG_TIME_MS_AVG, AnalysisType.LONG_EPOCH_MS_MINUS_NOW_AVG, AnalysisType.DOUBLE_AVG
        };
        final AnalysisType bool = AnalysisType.BOOLEAN_PERCENTAGE;
        final AnalysisType boolTot = AnalysisType.BOOLEAN_TOTAL;
        Log.debug("Analysis", "Additional Sources: " + sources.size());
        sources.parallelStream().filter(Verify::notNull).forEach(source -> {
            try {
                Benchmark.start("Source " + StringUtils.remove(source.getPlaceholder(), '%'));
                final List<AnalysisType> analysisTypes = source.getAnalysisTypes();
                if (analysisTypes.isEmpty()) {
                    return;
                }
                if (analysisTypes.contains(AnalysisType.HTML)) {
                    String html = source.getHtmlReplaceValue(AnalysisType.HTML.getModifier(), UUID.randomUUID());
                    String placeholderName = source.getPlaceholderName(AnalysisType.HTML.getPlaceholderModifier());
                    int length = html.length();
                    if (length < 20000) {
                        replaceMap.put(placeholderName, html);
                    } else {
                        replaceMap.put(placeholderName, "<p>Html was removed because it contained too many characters to be responsive (" + length + "/20000)</p>");
                    }
                    return;
                }
                for (AnalysisType type : totalTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholderName(type.getPlaceholderModifier()), AnalysisUtils.getTotal(type, source, uuids));
                    }
                }
                for (AnalysisType type : avgTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholderName(type.getPlaceholderModifier()), AnalysisUtils.getAverage(type, source, uuids));
                    }
                }
                if (analysisTypes.contains(bool)) {
                    replaceMap.put(source.getPlaceholderName(bool.getPlaceholderModifier()), AnalysisUtils.getBooleanPercentage(bool, source, uuids));
                }
                if (analysisTypes.contains(boolTot)) {
                    replaceMap.put(source.getPlaceholderName(boolTot.getPlaceholderModifier()), AnalysisUtils.getBooleanTotal(boolTot, source, uuids));
                }
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                Log.error("A PluginData-source caused an exception: " + StringUtils.remove(source.getPlaceholder(), '%'));

                Log.toLog(this.getClass().getName(), e);
            } finally {
                Benchmark.stop("Analysis", "Source " + StringUtils.remove(source.getPlaceholder(), '%'));
            }
        });
        Benchmark.stop("Analysis", "3rd party");
        return replaceMap;
    }

    /**
     * @return
     */
    public boolean isAnalysisBeingRun() {
        return taskId != -1;
    }

    public void setTaskId(int id) {
        taskId = id;
    }

    private void fillDataset(AnalysisData analysisData, Database db) {
        ActivityPart activity = analysisData.getActivityPart();
        CommandUsagePart commandUsagePart = analysisData.getCommandUsagePart();
        GeolocationPart geolocPart = analysisData.getGeolocationPart();
        JoinInfoPart joinInfo = analysisData.getJoinInfoPart();
        KillPart killPart = analysisData.getKillPart();
        PlayerCountPart playerCount = analysisData.getPlayerCountPart();
        PlaytimePart playtime = analysisData.getPlaytimePart();
        TPSPart tpsPart = analysisData.getTpsPart();
        WorldPart worldPart = analysisData.getWorldPart();

        long now = MiscUtils.getTime();

        Benchmark.start("Fetch Phase");
        try {
            Map<String, Integer> commandUse = plugin.getDB().getCommandUse();
            commandUsagePart.setCommandUsage(commandUse);

            TPSTable tpsTable = db.getTpsTable();
            List<TPS> tpsData = tpsTable.getTPSData();
            tpsTable.getAllTimePeak().ifPresent(tpsPart::setAllTimePeak);
            tpsTable.getPeakPlayerCount(now - (TimeAmount.DAY.ms() * 2)).ifPresent(tpsPart::setLastPeak);

            tpsPart.addTpsData(tpsData);
            Log.debug("Analysis", "TPS Data Size: " + tpsData.size());

            List<UserInfo> userInfo = db.getUserInfoTable().getServerUserInfo();

            for (UserInfo user : userInfo) {
                if (user.isBanned()) {
                    activity.addBan(user.getUuid());
                }
            }

            Map<UUID, UserInfo> mappedUserInfo = userInfo.stream().collect(Collectors.toMap(UserInfo::getUuid, Function.identity()));
            Map<UUID, Long> lastSeen = db.getSessionsTable().getLastSeenForAllPlayers();
            for (Map.Entry<UUID, Long> entry : lastSeen.entrySet()) {
                UserInfo user = mappedUserInfo.get(entry.getKey());
                if (user == null) {
                    continue;
                }
                user.setLastSeen(entry.getValue());
            }
            userInfo.sort(new UserInfoLastPlayedComparator());

            activity.setRecentPlayersUUIDs(userInfo.stream().map(UserInfo::getUuid).collect(Collectors.toList()));
            activity.setRecentPlayers(userInfo.stream().map(UserInfo::getName).collect(Collectors.toList()));

            playerCount.addPlayers(userInfo.stream().map(UserInfo::getUuid).collect(Collectors.toSet()));

            Map<UUID, Long> registered = userInfo.stream().collect(Collectors.toMap(UserInfo::getUuid, UserInfo::getRegistered));
            joinInfo.addRegistered(registered);
            activity.addBans(userInfo.stream().filter(UserInfo::isBanned).map(UserInfo::getUuid).collect(Collectors.toSet()));

            playerCount.addOPs(userInfo.stream().filter(UserInfo::isOpped).map(UserInfo::getUuid).collect(Collectors.toSet()));

            Map<UUID, Session> activeSessions = plugin.getDataCache().getActiveSessions();
            Map<UUID, List<Session>> sessions = db.getSessionsTable().getAllSessions(true).get(Plan.getServerUUID());
            joinInfo.addActiveSessions(activeSessions);
            if (sessions != null) {
                joinInfo.addSessions(sessions);
            }

            analysisData.setPlayersTable(PlayersTableCreator.createTable(userInfo, joinInfo, geolocPart));

            Map<UUID, List<PlayerKill>> playerKills = db.getKillsTable().getPlayerKills();
            killPart.addKills(playerKills);

            Map<UUID, List<String>> geolocations = db.getIpsTable().getAllGeolocations();
            geolocPart.addGeoLocations(geolocations);

            WorldTimes worldTimes = db.getWorldTimesTable().getWorldTimesOfServer();
            worldPart.setWorldTimes(worldTimes);

            playtime.setTotalPlaytime(db.getSessionsTable().getPlaytimeOfServer());
            playtime.setPlaytime30d(db.getSessionsTable().getPlaytimeOfServer(now - TimeAmount.MONTH.ms()));
            playtime.setPlaytime7d(db.getSessionsTable().getPlaytimeOfServer(now - TimeAmount.WEEK.ms()));
            playtime.setPlaytime24h(db.getSessionsTable().getPlaytimeOfServer(now - TimeAmount.DAY.ms()));

            List<PluginData> banSources = plugin.getHookHandler().getAdditionalDataSources()
                    .stream().filter(PluginData::isBanData).collect(Collectors.toList());

            for (UUID uuid : playerCount.getUuids()) {
                boolean banned = banSources.stream().anyMatch(pluginData -> {
                    try {
                        Serializable value = pluginData.getValue(uuid);
                        return value instanceof Boolean
                                && (boolean) value;
                    } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
                        Log.toLog(pluginData.getSourcePlugin() + pluginData.getPlaceholder() + " (Cause) ", e);
                        return false;
                    }
                });
                if (banned) {
                    activity.addBan(uuid);
                }
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
        Benchmark.stop("Analysis", "Fetch Phase");
    }
}
