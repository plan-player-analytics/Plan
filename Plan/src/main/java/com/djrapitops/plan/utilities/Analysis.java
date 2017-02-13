package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class Analysis {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;
    private final List<UserData> rawData;
    private HashMap<Long, ServerData> rawServerData;
    private final List<UUID> added;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Analysis(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
        rawData = new ArrayList<>();
        added = new ArrayList<>();
    }

    /**
     * Analyzes the data of all offlineplayers on the server.
     *
     * First retrieves all Offlineplayers and checks those that are in the
     * database. Then Runs a new Analysis Task Asyncronously. Saves AnalysisData
     * to the provided Cache. Saves all UserData to InspectCache for 8 minutes.
     *
     * @param analysisCache Cache that the data is saved to.
     */
    public void analyze(AnalysisCacheHandler analysisCache) {
        rawData.clear();
        added.clear();
        log(Phrase.ANALYSIS_START + "");

        List<UUID> uuids = fetchPlayersInDB();
        if (uuids.isEmpty()) {
            plugin.log(Phrase.ANALYSIS_FAIL_NO_DATA + "");
            return;
        }
        // Async task for Analysis
        (new BukkitRunnable() {
            @Override
            public void run() {
                uuids.stream().forEach((uuid) -> {
                    inspectCache.cache(uuid, 8);
                });
                log(Phrase.ANALYSIS_FETCH_DATA + "");
                while (rawData.size() != uuids.size()) {
                    uuids.stream()
                            .filter((uuid) -> (!added.contains(uuid)))
                            .forEach((uuid) -> {
                                UserData userData = inspectCache.getFromCache(uuid);
                                if (userData != null) {
                                    rawData.add(userData);
                                    added.add(uuid);
                                }
                            });
                }
                rawServerData = plugin.getDB().getServerDataHashMap();
                log(Phrase.ANALYSIS_BEGIN_ANALYSIS + "");
                AnalysisData data = new AnalysisData();

                createPlayerActivityGraphs(data);

                // Create empty Dataset
                long gmZero = 0;
                long gmOne = 0;
                long gmTwo = 0;
                long gmThree = 0;

                long totalLoginTimes = 0;
                long totalPlaytime = 0;

                int totalBanned = 0;
                int active = 0;
                int joinleaver = 0;
                int inactive = 0;

                long totalKills = 0;
                long totalMobKills = 0;
                long totalDeaths = 0;

                int ops = 0;
                List<Integer> ages = new ArrayList<>();

                boolean planLiteEnabled = isPlanLiteEnabled();
                PlanLiteAnalyzedData plData = new PlanLiteAnalyzedData();
                HashMap<String, Integer> townMap = new HashMap<>();
                HashMap<String, Integer> factionMap = new HashMap<>();
                int totalVotes = 0;
                int totalMoney = 0;

                HashMap<String, Long> latestLogins = new HashMap<>();
                HashMap<String, Long> playtimes = new HashMap<>();
                // Fill Dataset with userdata.
                for (UserData uData : rawData) {
                    try {
                        if (planLiteEnabled) {
                            PlanLitePlayerData litePlayerData = uData.getPlanLiteData();
                            String town = litePlayerData.getTown();
                            if (!townMap.containsKey(town)) {
                                townMap.put(town, 0);
                            }
                            townMap.replace(town, townMap.get(town) + 1);
                            String faction = litePlayerData.getFaction();
                            if (!factionMap.containsKey(faction)) {
                                factionMap.put(faction, 0);
                            }
                            factionMap.replace(faction, factionMap.get(faction) + 1);
                            totalVotes += litePlayerData.getVotes();
                            totalMoney += litePlayerData.getMoney();
                        }
                        HashMap<GameMode, Long> gmTimes = uData.getGmTimes();
                        gmZero += gmTimes.get(GameMode.SURVIVAL);
                        gmOne += gmTimes.get(GameMode.CREATIVE);
                        gmTwo += gmTimes.get(GameMode.ADVENTURE);
                        try {
                            Long gm = gmTimes.get(GameMode.SPECTATOR);
                            if (gm != null) {
                                gmThree += gm;
                            }
                        } catch (NoSuchFieldError e) {
                        }
                        long playTime = uData.getPlayTime();
                        totalPlaytime += playTime;
                        String playerName = uData.getName();
                        String url = HtmlUtils.getInspectUrl(playerName);
                        String html = Html.BUTTON.parse(url, playerName);

                        latestLogins.put(html, uData.getLastPlayed());
                        totalLoginTimes += uData.getLoginTimes();
                        int age = uData.getDemData().getAge();
                        if (age != -1) {
                            ages.add(age);
                        }
                        if (uData.isOp()) {
                            ops++;
                        }
                        if (uData.isBanned()) {
                            totalBanned++;
                        } else if (uData.getLoginTimes() == 1) {
                            joinleaver++;
                        } else if (AnalysisUtils.isActive(uData.getLastPlayed(), playTime, uData.getLoginTimes())) {
                            active++;
                            playtimes.put(html, playTime);
                        } else {
                            inactive++;
                        }
                        totalKills += uData.getPlayerKills();
                        totalMobKills += uData.getMobKills();
                        totalDeaths += uData.getDeaths();
                    } catch (NullPointerException e) {
                        plugin.logError(Phrase.DATA_CORRUPTION_WARN.parse(uData.getUuid() + ""));
                    }
                }

                // Save Dataset to AnalysisData
                data.setTop20ActivePlayers(AnalysisUtils.createActivePlayersTable(playtimes, 20));
                data.setRecentPlayers(AnalysisUtils.createListStringOutOfHashMapLong(latestLogins, 20));

                addPlanLiteToData(planLiteEnabled, plData, factionMap, townMap, totalVotes, totalMoney, data);

                data.setTotalPlayTime(totalPlaytime);
                data.setAveragePlayTime(totalPlaytime / rawData.size());
                data.setTotalLoginTimes(totalLoginTimes);

                createActivityVisalization(totalBanned, active, inactive, joinleaver, data);

                data.setOps(ops);

                analyzeAverageAge(ages, data);
                createGamemodeUsageVisualization(gmZero, gmOne, gmTwo, gmThree, data);
                createCommandUseTable(data);

                data.setTotaldeaths(totalDeaths);
                data.setTotalkills(totalKills);
                data.setTotalmobkills(totalMobKills);

                data.setRefreshDate(new Date().getTime());
                analysisCache.cache(data);
                plugin.log(Phrase.ANALYSIS_COMPLETE + "");
                this.cancel();
            }

            private boolean isPlanLiteEnabled() {
                boolean planLiteEnabled;
                PlanLiteHook planLiteHook = plugin.getPlanLiteHook();
                if (planLiteHook != null) {
                    planLiteEnabled = planLiteHook.isEnabled();
                } else {
                    planLiteEnabled = false;
                }
                return planLiteEnabled;
            }

            private void addPlanLiteToData(boolean planLiteEnabled, PlanLiteAnalyzedData plData, HashMap<String, Integer> factionMap, HashMap<String, Integer> townMap, int totalVotes, int totalMoney, AnalysisData data) {
                if (planLiteEnabled) {
                    plData.setFactionMap(factionMap);
                    plData.setTownMap(townMap);
                    plData.setTotalVotes(totalVotes);
                    plData.setTotalMoney(totalMoney);
                    data.setPlanLiteEnabled(true);
                    data.setPlanLiteData(plData);
                } else {
                    data.setPlanLiteEnabled(false);
                }
            }

            private void createActivityVisalization(int totalBanned, int active, int inactive, int joinleaver, AnalysisData data) {
                String activityPieChartHtml = AnalysisUtils.createActivityPieChart(totalBanned, active, inactive, joinleaver);
                data.setActivityChartImgHtml(activityPieChartHtml);
                data.setActive(active);
                data.setInactive(inactive);
                data.setBanned(totalBanned);
                data.setJoinleaver(joinleaver);
                data.setTotal(uuids.size());
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

            private void createCommandUseTable(AnalysisData data) {
                if (rawServerData.keySet().size() > 0) {
                    ServerData sData = null;
                    for (long sDataKey : rawServerData.keySet()) {
                        sData = rawServerData.get(sDataKey);
                        break;
                    }
                    if (sData != null) {
                        data.setTop50CommandsListHtml(AnalysisUtils.createTableOutOfHashMap(sData.getCommandUsage()));
                    }
                } else {
                    data.setTop50CommandsListHtml(Html.ERROR_TABLE.parse());
                }
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
                String serverGMChartHtml = AnalysisUtils.createGMPieChart(totalGmTimes, gmTotal);
                data.setGmTimesChartImgHtml(serverGMChartHtml);
                data.setGm0Perc((gmZero * 1.0 / gmTotal));
                data.setGm1Perc((gmOne * 1.0 / gmTotal));
                data.setGm2Perc((gmTwo * 1.0 / gmTotal));
                data.setGm3Perc((gmThree * 1.0 / gmTotal));
            }

            private void createPlayerActivityGraphs(AnalysisData data) {
                long scaleMonth = (long) 2592000 * (long) 1000;
                String playerActivityHtmlMonth = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleMonth);
                data.setPlayersChartImgHtmlMonth(playerActivityHtmlMonth);
                data.setNewPlayersMonth(getHighestNPValueForScale(scaleMonth));
                long scaleWeek = 604800 * 1000;
                String playerActivityHtmlWeek = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleWeek);
                data.setPlayersChartImgHtmlWeek(playerActivityHtmlWeek);
                data.setNewPlayersWeek(getHighestNPValueForScale(scaleWeek));
                long scaleDay = 86400 * 1000;
                String playerActivityHtmlDay = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleDay);
                data.setPlayersChartImgHtmlDay(playerActivityHtmlDay);
                data.setNewPlayersDay(getHighestNPValueForScale(scaleDay));
            }

            private int getHighestNPValueForScale(long scale) {
                List<List<ServerData>> sDataForEachDay = sortServerDatasByDay(scale);
                int NPTotalInsideScaleTimeFrame = 0;
                NPTotalInsideScaleTimeFrame = sDataForEachDay.parallelStream()
                        .map((serverDataList) -> {
                            int highestNPValue = 0;
                            for (ServerData serverData : serverDataList) {
                                int newPlayers = serverData.getNewPlayers();
                                if (newPlayers > highestNPValue) {
                                    highestNPValue = newPlayers;
                                }
                            }
                            return highestNPValue;
                        }).map((highestNPValue) -> highestNPValue)
                        .reduce(NPTotalInsideScaleTimeFrame, Integer::sum);
                return NPTotalInsideScaleTimeFrame;
            }

            private List<List<ServerData>> sortServerDatasByDay(long scale) {
                List<List<ServerData>> sDataForEachDay = new ArrayList<>();
                Date lastStartOfDay = null;
                List<Long> keys = new ArrayList<>();
                keys.addAll(rawServerData.keySet());
                Collections.sort(keys);
                for (long date : keys) {
                    Date startOfDate = MiscUtils.getStartOfDate(new Date(date));
                    if (lastStartOfDay == null) {
                        sDataForEachDay.add(new ArrayList<>());
                        lastStartOfDay = startOfDate;
                    }
                    // If data is older than one month, ignore
                    if (new Date().getTime() - startOfDate.getTime() > scale) {
                        continue;
                    }
                    if (startOfDate.getTime() != lastStartOfDay.getTime()) {
                        sDataForEachDay.add(new ArrayList<>());
                    }
                    int lastIndex = sDataForEachDay.size() - 1;
                    ServerData serverData = rawServerData.get(date);
                    sDataForEachDay.get(lastIndex).add(serverData);
                    lastStartOfDay = startOfDate;
                }
                return sDataForEachDay;
            }
        }).runTaskAsynchronously(plugin);
    }

    private List<UUID> fetchPlayersInDB() {
        final List<UUID> uuids = new ArrayList<>();
        log(Phrase.ANALYSIS_FETCH_PLAYERS + "");
        Set<UUID> savedUUIDs = plugin.getDB().getSavedUUIDs();
        savedUUIDs.parallelStream()
                .filter((uuid) -> (getOfflinePlayer(uuid).hasPlayedBefore()))
                .forEach((uuid) -> {
                    uuids.add(uuid);
                });
        return uuids;
    }

    private void log(String msg) {
        if (Settings.ANALYSIS_LOG_TO_CONSOLE.isTrue()) {
            plugin.log(msg);
        }
    }
}
