package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class Analysis {

    private Plan plugin;
    private InspectCacheHandler inspectCache;
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
     * to the provided Cache.
     *
     * @param analysisCache Cache that the data is saved to.
     */
    public void analyze(AnalysisCacheHandler analysisCache) {
        rawData.clear();
        added.clear();
        plugin.log("Analysis | Beginning analysis of user data..");
        OfflinePlayer[] offlinePlayers;
        try {
            offlinePlayers = plugin.getServer().getOfflinePlayers();
        } catch (IndexOutOfBoundsException e) {
            plugin.log("Analysis | Analysis failed, no known players.");
            return;
        }
        final List<UUID> uuids = new ArrayList<>();
        for (OfflinePlayer p : offlinePlayers) {
            UUID uuid = p.getUniqueId();
            if (plugin.getDB().wasSeenBefore(uuid)) {
                uuids.add(uuid);
            }
        }
        if (uuids.isEmpty()) {
            plugin.log("Analysis | Analysis failed, no data in the database.");
            return;
        }
        (new BukkitRunnable() {
            @Override
            public void run() {
                uuids.stream().forEach((uuid) -> {
                    inspectCache.cache(uuid);
                });
                plugin.log("Analysis | Fetching Data..");
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
                plugin.log("Analysis | Data Fetched, beginning Analysis of data..");
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

                int ops = 0;
                List<Integer> ages = new ArrayList<>();
                
                boolean planLiteEnabled;
                PlanLiteHook planLiteHook = plugin.getPlanLiteHook();
                if (planLiteHook != null) {
                    planLiteEnabled = planLiteHook.isEnabled();
                } else {
                    planLiteEnabled = false;
                }

                PlanLiteAnalyzedData plData = new PlanLiteAnalyzedData();
                HashMap<String, Integer> townMap = new HashMap<>();
                HashMap<String, Integer> factionMap = new HashMap<>();
                int totalVotes = 0;
                int totalMoney = 0;

                // Fill Dataset with userdata.
                for (UserData uData : rawData) {
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
                    gmThree += gmTimes.get(GameMode.SPECTATOR);
                    totalPlaytime += uData.getPlayTime();
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
                    } else if (AnalysisUtils.isActive(uData.getLastPlayed(), uData.getPlayTime(), uData.getLoginTimes())) {
                        active++;
                    } else {
                        inactive++;
                    }
                }

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

                data.setTotalLoginTimes(totalLoginTimes);

                String activityPieChartHtml = AnalysisUtils.createActivityPieChart(totalBanned, active, inactive, joinleaver);
                data.setActivityChartImgHtml(activityPieChartHtml);
                data.setActive(active);
                data.setInactive(inactive);
                data.setBanned(totalBanned);
                data.setJoinleaver(joinleaver);

                data.setTotal(offlinePlayers.length);
                data.setOps(ops);

                data.setTotalPlayTime(totalPlaytime);
                data.setAveragePlayTime(totalPlaytime / rawData.size());
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

                long gmTotal = gmZero + gmOne + gmTwo + gmThree;
                HashMap<GameMode, Long> totalGmTimes = new HashMap<>();
                totalGmTimes.put(GameMode.SURVIVAL, gmZero);
                totalGmTimes.put(GameMode.CREATIVE, gmOne);
                totalGmTimes.put(GameMode.ADVENTURE, gmTwo);
                totalGmTimes.put(GameMode.SPECTATOR, gmThree);
                String serverGMChartHtml = AnalysisUtils.createGMPieChart(totalGmTimes, gmTotal);
                data.setGmTimesChartImgHtml(serverGMChartHtml);
                data.setGm0Perc((gmZero * 1.0 / gmTotal));
                data.setGm1Perc((gmOne * 1.0 / gmTotal));
                data.setGm2Perc((gmTwo * 1.0 / gmTotal));
                data.setGm3Perc((gmThree * 1.0 / gmTotal));

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
                    data.setTop50CommandsListHtml("<p>Error Calcuclating Command usages (No usage data)</p>");
                }

                data.setRefreshDate(new Date().getTime());
                analysisCache.cache(data);
                plugin.log("Analysis | Analysis Complete.");
                this.cancel();
            }

            private void createPlayerActivityGraphs(AnalysisData data) {
                long scaleMonth = (long) 2592000 * (long) 1000;
                String playerActivityHtmlMonth = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleMonth);
                data.setPlayersChartImgHtmlMonth(playerActivityHtmlMonth);
                long scaleWeek = 604800 * 1000;
                String playerActivityHtmlWeek = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleWeek);
                data.setPlayersChartImgHtmlWeek(playerActivityHtmlWeek);
                long scaleDay = 86400 * 1000;
                String playerActivityHtmlDay = AnalysisUtils.createPlayerActivityGraph(rawServerData, scaleDay);
                data.setPlayersChartImgHtmlDay(playerActivityHtmlDay);
            }
        }).runTaskAsynchronously(plugin);
    }
}
