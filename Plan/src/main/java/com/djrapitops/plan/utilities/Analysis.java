package main.java.com.djrapitops.plan.utilities;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.RawAnalysisData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.RecentPlayersButtonsCreator;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.ui.graphs.SessionLengthDistributionGraphCreator;
import main.java.com.djrapitops.plan.ui.tables.SortableCommandUseTableCreator;
import main.java.com.djrapitops.plan.ui.tables.SortablePlayersTableCreator;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Rsl1122
 */
public class Analysis {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Analysis(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
    }

    /**
     * Analyzes the data of all offlineplayers on the server.
     *
     * First retrieves all Offlineplayers and checks those that are in the
     * database. Then Runs a new Analysis Task Asyncronously. Saves AnalysisData
     * to the provided Cache. Saves all UserData to InspectCache for 15 minutes.
     *
     * @param analysisCache Cache that the data is saved to.
     */
    public void runAnalysis(AnalysisCacheHandler analysisCache) {
        Benchmark.start("Analysis");
        log(Phrase.ANALYSIS_START + "");
        // Async task for Analysis
        BukkitTask asyncAnalysisTask = (new BukkitRunnable() {
            @Override
            public void run() {
                analyze(analysisCache, plugin.getDB());
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
    }

    /**
     * Caches analyzed data of db to the provided cache analysisCache.
     *
     * @param analysisCache Cache that will contain AnalysisData result of this
     * method.
     * @param db Database which data will be analyzed.
     * @return Whether or not analysis was successful.
     */
    public boolean analyze(AnalysisCacheHandler analysisCache, Database db) {
        log(Phrase.ANALYSIS_FETCH_DATA + "");
        Benchmark.start("Analysis Fetch Phase");
        try {
            inspectCache.cacheAllUserData(db);
        } catch (Exception ex) {
            Log.toLog(this.getClass().getName(), ex);
            Log.error(Phrase.ERROR_ANALYSIS_FETCH_FAIL + "");
        }
        List<UserData> rawData = inspectCache.getCachedUserData();
        if (rawData.isEmpty()) {
            Log.info(Phrase.ANALYSIS_FAIL_NO_DATA + "");
            return false;
        }
        Benchmark.stop("Analysis Fetch Phase");
        return analyzeData(rawData, analysisCache);
    }

    /**
     *
     * @param rawData
     * @param analysisCache
     * @return
     */
    public boolean analyzeData(List<UserData> rawData, AnalysisCacheHandler analysisCache) {
        Benchmark.start("Analysis UUID transform");
        Benchmark.start("Analysis Phase");
        List<UUID> uuids = rawData.stream().map(d -> d.getUuid()).collect(Collectors.toList());
        Benchmark.stop("Analysis UUID transform");
        Benchmark.start("Analysis Create Empty dataset");
        long now = MiscUtils.getTime();
        final RawAnalysisData sorted = new RawAnalysisData();
        sorted.setCommandUse(plugin.getHandler().getCommandUse());
        AnalysisData analysisData = new AnalysisData();
        Benchmark.stop("Analysis Create Empty dataset");
        log(Phrase.ANALYSIS_BEGIN_ANALYSIS + "");
        String playersTable = SortablePlayersTableCreator.createSortablePlayersTable(rawData);
        analysisData.setSortablePlayersTable(playersTable);
        sorted.fillGeolocations();
        Benchmark.start("Analysis Fill Dataset");
        rawData.stream().forEach((uData) -> {
            Map<GameMode, Long> gmTimes = uData.getGmTimes();
            if (gmTimes != null) {
                Long survival = gmTimes.get(GameMode.SURVIVAL);
                if (survival != null) {
                    sorted.addToGmZero(survival);
                }
                Long creative = gmTimes.get(GameMode.CREATIVE);
                if (creative != null) {
                    sorted.addToGmOne(creative);
                }
                Long adventure = gmTimes.get(GameMode.ADVENTURE);
                if (adventure != null) {
                    sorted.addToGmTwo(adventure);
                }
                try {
                    Long gm = gmTimes.get(GameMode.SPECTATOR);
                    if (gm != null) {
                        sorted.addGmThree(gm);
                    }
                } catch (NoSuchFieldError e) {
                }
            }
            long playTime = uData.getPlayTime();
            sorted.addTotalPlaytime(playTime);
            String playerName = uData.getName();
            String url = HtmlUtils.getInspectUrl(playerName);
            String html = Html.BUTTON.parse(url, playerName);

            sorted.getLatestLogins().put(html, uData.getLastPlayed());
            sorted.addTotalLoginTimes(uData.getLoginTimes());
            DemographicsData demData = uData.getDemData();
            if (demData == null) {
                demData = new DemographicsData();
            }
            int age = demData.getAge();
            if (age != -1) {
                sorted.getAges().add(age);
            }
            if (uData.isOp()) {
                sorted.addOps(1);
            }
            if (uData.isBanned()) {
                sorted.addTotalBanned(1);
            } else if (uData.getLoginTimes() == 1) {
                sorted.addJoinleaver(1);
            } else if (AnalysisUtils.isActive(now, uData.getLastPlayed(), playTime, uData.getLoginTimes())) {
                sorted.addActive(1);
                sorted.getPlaytimes().put(html, playTime);
            } else {
                sorted.addInactive(1);
            }
            List<KillData> playerKills = uData.getPlayerKills();
            if (playerKills != null) {
                sorted.addTotalKills(playerKills.size());
            }
            sorted.addTotalMobKills(uData.getMobKills());
            sorted.addTotalDeaths(uData.getDeaths());
            List<SessionData> sessions = uData.getSessions();
            if (!sessions.isEmpty()) {
                sorted.getSessiondata().addAll(sessions);
            }
            sorted.getRegistered().add(uData.getRegistered());
            sorted.addGeoloc(demData.getGeoLocation());
            uData.stopAccessing();
            Gender gender = demData.getGender();
            if (null != gender) {
                switch (gender) {
                    case MALE:
                        sorted.addToGender(0, 1);
                        break;
                    case FEMALE:
                        sorted.addToGender(1, 1);
                        break;
                    default:
                        sorted.addToGender(2, 1);
                        break;
                }
            }
        });
        Benchmark.stop("Analysis Fill Dataset");
        createCloroplethMap(analysisData, sorted.getGeolocations(), sorted.getGeocodes());
        // Analyze & Save RawAnalysisData to AnalysisData
        createPlayerActivityGraphs(analysisData, sorted.getSessiondata(), sorted.getRegistered());
        analysisData.setRecentPlayers(RecentPlayersButtonsCreator.createRecentLoginsButtons(sorted.getLatestLogins(), 20));
        long totalPlaytime = sorted.getTotalPlaytime();
        analysisData.setTotalPlayTime(totalPlaytime);
        analysisData.setAveragePlayTime(totalPlaytime / rawData.size());
        analysisData.setSessionAverage(MathUtils.averageLong(AnalysisUtils.transformSessionDataToLengths(sorted.getSessiondata())));
        analysisData.setTotalLoginTimes(sorted.getTotalLoginTimes());
        createActivityVisalization(uuids.size(), sorted.getTotalBanned(), sorted.getActive(), sorted.getInactive(), sorted.getJoinleaver(), analysisData);
        analysisData.setOps(sorted.getOps());
        analyzeAverageAge(sorted.getAges(), analysisData);
        createGamemodeUsageVisualization(sorted.getGmZero(), sorted.getGmOne(), sorted.getGmTwo(), sorted.getGmThree(), analysisData);
        createCommandUseTable(sorted, analysisData);
        analysisData.setTotaldeaths(sorted.getTotalDeaths());
        analysisData.setTotalkills(sorted.getTotalKills());
        analysisData.setTotalmobkills(sorted.getTotalMobKills());
        analysisData.setRefreshDate(now);
        analysisData.setGenderData(sorted.getGenders());
        analysisData.setPunchCardData(PunchCardGraphCreator.generateDataArray(sorted.getSessiondata()));
        analysisData.setSessionDistributionData(SessionLengthDistributionGraphCreator.generateDataArraySessions(sorted.getSessiondata()));
        analysisData.setPlaytimeDistributionData(SessionLengthDistributionGraphCreator.generateDataArray(sorted.getPlaytimes().values()));
        analysisData.setAdditionalDataReplaceMap(analyzeAdditionalPluginData(uuids));
        analysisCache.cache(analysisData);
        Benchmark.stop("Analysis");
        if (Settings.ANALYSIS_LOG_FINISHED.isTrue()) {
            Log.info(Phrase.ANALYSIS_COMPLETE + "");
        }
        return true;
    }

    private void createCommandUseTable(final RawAnalysisData raw, AnalysisData data) {

        Map<String, Integer> commandUse = raw.getCommandUse();
        if (!commandUse.isEmpty()) {
            String tableHtml = SortableCommandUseTableCreator.createSortedCommandUseTable(commandUse);
            data.setCommandUseTableHtml(tableHtml);
            data.setTotalCommands(commandUse.size());
        } else {
            data.setCommandUseTableHtml(Html.ERROR_TABLE_2.parse());
            data.setTotalCommands(0);
        }
    }

    private void createActivityVisalization(int total, int totalBanned, int active, int inactive, int joinleaver, AnalysisData data) {
        Benchmark.start("Analysis Activity Visualization");
        data.setActive(active);
        data.setInactive(inactive);
        data.setBanned(totalBanned);
        data.setJoinleaver(joinleaver);
        data.setTotal(total);
        Benchmark.stop("Analysis Activity Visualization");
    }

    // TODO Refactor
    private void analyzeAverageAge(List<Integer> ages, AnalysisData data) {
        int totalAge = 0;
        for (int age : ages) {
            totalAge += age;
        }
        double averageAge;
        if (!ages.isEmpty()) {
            averageAge = totalAge * 1.0 / ages.size();
        } else {
            averageAge = -1;
        }
        data.setAverageAge(averageAge);
    }

    private void createGamemodeUsageVisualization(long gmZero, long gmOne, long gmTwo, long gmThree, AnalysisData data) {
        Benchmark.start("Analysis GMVisualization");
        long gmTotal = gmZero + gmOne + gmTwo + gmThree;
        HashMap<GameMode, Long> totalGmTimes = new HashMap<>();
        totalGmTimes.put(GameMode.SURVIVAL, gmZero);
        totalGmTimes.put(GameMode.CREATIVE, gmOne);
        totalGmTimes.put(GameMode.ADVENTURE, gmTwo);
        try {
            totalGmTimes.put(GameMode.SPECTATOR, gmThree);
        } catch (NoSuchFieldError e) {
        }
        data.setGm0Perc((gmZero * 1.0 / gmTotal));
        data.setGm1Perc((gmOne * 1.0 / gmTotal));
        data.setGm2Perc((gmTwo * 1.0 / gmTotal));
        data.setGm3Perc((gmThree * 1.0 / gmTotal));
        Benchmark.stop("Analysis GMVisualization");
    }

    private void createPlayerActivityGraphs(AnalysisData data, List<SessionData> sData, List<Long> registered) {
        long now = new Date().toInstant().getEpochSecond() * (long) 1000;

        long scaleDay = 86400 * 1000;
        long scaleWeek = 604800 * 1000;
        long scaleMonth = (long) 2592000 * (long) 1000;
        int maxPlayers = plugin.getHandler().getMaxPlayers();

        data.setNewPlayersDay(AnalysisUtils.getNewPlayers(registered, scaleDay, now));
        data.setNewPlayersWeek(AnalysisUtils.getNewPlayers(registered, scaleWeek, now));
        data.setNewPlayersMonth(AnalysisUtils.getNewPlayers(registered, scaleMonth, now));

        List<SessionData> sessions = sData.stream()
                .filter(session -> (session != null))
                .filter(session -> session.isValid())
                .filter((session) -> (session.getSessionStart() >= now-scaleMonth || session.getSessionEnd() >= now-scaleMonth))
                .collect(Collectors.toList());
        
        String[] dayArray = PlayerActivityGraphCreator.generateDataArray(sessions, scaleDay, maxPlayers);
        String[] weekArray = PlayerActivityGraphCreator.generateDataArray(sessions, scaleWeek, maxPlayers);
        String[] monthArray = PlayerActivityGraphCreator.generateDataArray(sessions, scaleMonth, maxPlayers);

        data.setPlayersDataArray(new String[]{dayArray[0], dayArray[1], weekArray[0], weekArray[1], monthArray[0], monthArray[1]});
    }

    private void log(String msg) {
        if (Settings.ANALYSIS_LOG_TO_CONSOLE.isTrue()) {
            Log.info(msg);
        }
    }

    private void createCloroplethMap(AnalysisData aData, Map<String, Integer> geolocations, Map<String, String> geocodes) {
        Benchmark.start("Analysis Chloropleth map");
        String locations = "[";
        String z = "[";
        String text = "[";
        for (String c : geolocations.keySet()) {
            locations += "\"" + c + "\"" + ",";
            z += geolocations.get(c) + ",";
            String code = geocodes.get(c);
            if (code != null) {
                text += "\"" + code + "\"" + ",";
            } else {
                text += "\"UNK\",";
            }
        }
        locations += "]";
        z += "]";
        text += "]";
        aData.setGeomapCountries(locations.replace(",]", "]"));
        aData.setGeomapZ(z.replace(",]", "]"));
        aData.setGeomapCodes(text.replace(",]", "]"));
        Benchmark.stop("Analysis Chloropleth map");
    }

    private Map<String, String> analyzeAdditionalPluginData(List<UUID> uuids) {
        final Map<String, String> replaceMap = new HashMap<>();
        final HookHandler hookHandler = plugin.getHookHandler();
        final List<PluginData> sources = hookHandler.getAdditionalDataSources();
        final AnalysisType[] totalTypes = new AnalysisType[]{
            AnalysisType.INT_TOTAL, AnalysisType.LONG_TOTAL, AnalysisType.LONG_TIME_MS_TOTAL, AnalysisType.DOUBLE_TOTAL
        };
        final AnalysisType[] avgTypes = new AnalysisType[]{
            AnalysisType.INT_AVG, AnalysisType.LONG_AVG, AnalysisType.LONG_TIME_MS_AVG, AnalysisType.LONG_EPOCH_MS_MINUS_NOW_AVG, AnalysisType.DOUBLE_AVG
        };
        final AnalysisType bool = AnalysisType.BOOLEAN_PERCENTAGE;
        final AnalysisType boolTot = AnalysisType.BOOLEAN_TOTAL;
        Log.debug("Analyzing additional sources: " + sources.size());
        sources.parallelStream().forEach(source -> {
            Benchmark.start("Source " + source.getPlaceholder("").replace("%", ""));
            try {
                final List<AnalysisType> analysisTypes = source.getAnalysisTypes();
                if (analysisTypes.isEmpty()) {
                    return;
                }
                if (analysisTypes.contains(AnalysisType.HTML)) {
                    replaceMap.put(source.getPlaceholder(AnalysisType.HTML.getPlaceholderModifier()), source.getHtmlReplaceValue(AnalysisType.HTML.getModifier(), uuids.get(0)));
                    return;
                }
                for (AnalysisType type : totalTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholder(type.getPlaceholderModifier()), AnalysisUtils.getTotal(type, source, uuids));
                    }
                }
                for (AnalysisType type : avgTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholder(type.getPlaceholderModifier()), AnalysisUtils.getAverage(type, source, uuids));
                    }
                }
                if (analysisTypes.contains(bool)) {
                    replaceMap.put(source.getPlaceholder(bool.getPlaceholderModifier()), AnalysisUtils.getBooleanPercentage(bool, source, uuids));
                }
                if (analysisTypes.contains(boolTot)) {
                    replaceMap.put(source.getPlaceholder(boolTot.getPlaceholderModifier()), AnalysisUtils.getBooleanTotal(boolTot, source, uuids));
                }
            } catch (Throwable e) {
                Log.error("A PluginData-source caused an exception: " + source.getPlaceholder("").replace("%", ""));
                Log.toLog(this.getClass().getName(), e);
            } finally {
                Benchmark.stop("Source " + source.getPlaceholder("").replace("%", ""));
            }
        });
        return replaceMap;
    }
}
