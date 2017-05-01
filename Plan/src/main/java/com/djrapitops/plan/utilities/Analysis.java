package main.java.com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.RecentPlayersButtonsCreator;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import static org.bukkit.Bukkit.getOfflinePlayer;

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
     * @param analysisCache
     * @deprecated Does nothing anymore, use analyze(AnalysisCacheHandler,
     * Database)
     */
    @Deprecated
    public void analyze(AnalysisCacheHandler analysisCache) {
    }

    private List<UUID> fetchPlayersInDB(Database db) {
        try {
            log(Phrase.ANALYSIS_FETCH_PLAYERS + "");
            Set<UUID> savedUUIDs = db.getSavedUUIDs();
            List<UUID> uuids = savedUUIDs.parallelStream()
                    .filter(uuid -> uuid != null)
                    .filter((uuid) -> (getOfflinePlayer(uuid).hasPlayedBefore()))
                    .collect(Collectors.toList());
            return uuids;
        } catch (Exception e) {
            plugin.toLog(this.getClass().getName(), e);
        }
        return new ArrayList<>();
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
        List<UserData> rawData = new ArrayList<>();
        List<UUID> added = new ArrayList<>();
        List<UUID> uuids = fetchPlayersInDB(db);
        if (uuids.isEmpty()) {
            Log.info(Phrase.ANALYSIS_FAIL_NO_DATA + "");
            return false;
        }
        uuids.stream().forEach((uuid) -> {
            inspectCache.cache(uuid);
        });
        log(Phrase.ANALYSIS_FETCH_DATA + "");
        while (rawData.size() != uuids.size()) {
            uuids.stream()
                    .filter((uuid) -> (!added.contains(uuid)))
                    .forEach((uuid) -> {
                        if (inspectCache.isCached(uuid)) {
                            UserData userData = inspectCache.getFromCache(uuid);
                            if (userData != null) {
                                rawData.add(userData);
                                userData.access();
                                added.add(uuid);
                            }
                        }
                    });
        }
        if (added.isEmpty()) {
            Log.info(Phrase.ANALYSIS_FAIL_NO_DATA + "");
            return false;
        }
        return analyzeData(rawData, uuids, analysisCache);
    }

    /**
     *
     * @param rawData
     * @param uuids
     * @param analysisCache
     * @return
     */
    public boolean analyzeData(List<UserData> rawData, List<UUID> uuids, AnalysisCacheHandler analysisCache) {
        // Create empty Dataset
        final RawAnalysisData sorted = new RawAnalysisData();
        sorted.setCommandUse(plugin.getHandler().getCommandUse());
        log(Phrase.ANALYSIS_BEGIN_ANALYSIS + "");
        AnalysisData analysisData = new AnalysisData();
        analysisData.setSortablePlayersTable(AnalysisUtils.createSortablePlayersTable(rawData));
        sorted.fillGeolocations();
        // Fill Dataset with userdata.
        rawData.stream().forEach((uData) -> {
//            try {
            HashMap<GameMode, Long> gmTimes = uData.getGmTimes();
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
            } else if (AnalysisUtils.isActive(uData.getLastPlayed(), playTime, uData.getLoginTimes())) {
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
            //} catch (NullPointerException e) {
//                plugin.logError(Phrase.DATA_CORRUPTION_WARN.parse(uData.getUuid() + ""));
//                plugin.toLog(this.getClass().getName(), e);
//            }
        });
        createCloroplethMap(analysisData, sorted.getGeolocations(), sorted.getGeocodes());
        // Analyze & Save RawAnalysisData to AnalysisData
        createPlayerActivityGraphs(analysisData, sorted.getSessiondata(), sorted.getRegistered());
        analysisData.setRecentPlayers(RecentPlayersButtonsCreator.createRecentLoginsButtons(sorted.getLatestLogins(), 20));
        long totalPlaytime = sorted.getTotalPlaytime();
        analysisData.setTotalPlayTime(totalPlaytime);
        analysisData.setAveragePlayTime(totalPlaytime / rawData.size());
        analysisData.setSessionAverage(AnalysisUtils.average(AnalysisUtils.transformSessionDataToLengths(sorted.getSessiondata())));
        analysisData.setTotalLoginTimes(sorted.getTotalLoginTimes());
        createActivityVisalization(uuids.size(), sorted.getTotalBanned(), sorted.getActive(), sorted.getInactive(), sorted.getJoinleaver(), analysisData);
        analysisData.setOps(sorted.getOps());
        analyzeAverageAge(sorted.getAges(), analysisData);
        createGamemodeUsageVisualization(sorted.getGmZero(), sorted.getGmOne(), sorted.getGmTwo(), sorted.getGmThree(), analysisData);
        createCommandUseTable(sorted, analysisData);
        analysisData.setTotaldeaths(sorted.getTotalDeaths());
        analysisData.setTotalkills(sorted.getTotalKills());
        analysisData.setTotalmobkills(sorted.getTotalMobKills());
        analysisData.setRefreshDate(new Date().getTime());
        analysisData.setGenderData(sorted.getGenders());
        analysisCache.cache(analysisData);
        if (Settings.ANALYSIS_LOG_FINISHED.isTrue()) {
            Log.info(Phrase.ANALYSIS_COMPLETE + "");
        }
        return true;
    }

    private void createCommandUseTable(final RawAnalysisData raw, AnalysisData data) {
        Map<String, Integer> commandUse = raw.getCommandUse();
        if (!commandUse.isEmpty()) {
            data.setCommandUseTableHtml(AnalysisUtils.createTableOutOfMap(commandUse));
            data.setTotalCommands(commandUse.size());
        } else {
            data.setCommandUseTableHtml(Html.ERROR_TABLE_2.parse());
            data.setTotalCommands(0);
        }
    }

    private void createActivityVisalization(int total, int totalBanned, int active, int inactive, int joinleaver, AnalysisData data) {
        data.setActive(active);
        data.setInactive(inactive);
        data.setBanned(totalBanned);
        data.setJoinleaver(joinleaver);
        data.setTotal(total);
    }

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

        String[] dayArray = PlayerActivityGraphCreator.generateDataArray(sData, scaleDay, maxPlayers);
        String[] weekArray = PlayerActivityGraphCreator.generateDataArray(sData, scaleWeek, maxPlayers);
        String[] monthArray = PlayerActivityGraphCreator.generateDataArray(sData, scaleMonth, maxPlayers);

        data.setPlayersDataArray(new String[]{dayArray[0], dayArray[1], weekArray[0], weekArray[1], monthArray[0], monthArray[1]});
    }

    private void log(String msg) {
        if (Settings.ANALYSIS_LOG_TO_CONSOLE.isTrue()) {
            Log.info(msg);
        }
    }

    private void createCloroplethMap(AnalysisData aData, Map<String, Integer> geolocations, Map<String, String> geocodes) {
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
    }
}
