package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;
import main.java.com.djrapitops.plan.data.RawAnalysisData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
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
    private final List<UserData> rawData;
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
        BukkitTask asyncAnalysisTask = (new BukkitRunnable() {
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
                // Create empty Dataset
                final RawAnalysisData raw = new RawAnalysisData();
                raw.setCommandUse(plugin.getDB().getCommandUse());
                log(Phrase.ANALYSIS_BEGIN_ANALYSIS + "");
                AnalysisData data = new AnalysisData();

                // DEPRECATED - WILL BE REMOVED
                boolean planLiteEnabled = isPlanLiteEnabled();
                PlanLiteAnalyzedData plData = new PlanLiteAnalyzedData();
                HashMap<String, Integer> townMap = new HashMap<>();
                HashMap<String, Integer> factionMap = new HashMap<>();
                int totalVotes = 0;
                int totalMoney = 0;
                // Fill Dataset with userdata.
                rawData.parallelStream().forEach((uData) -> {
                    try {
                        HashMap<GameMode, Long> gmTimes = uData.getGmTimes();
                        raw.addToGmZero(gmTimes.get(GameMode.SURVIVAL));
                        raw.addToGmOne(gmTimes.get(GameMode.CREATIVE));
                        raw.addToGmTwo(gmTimes.get(GameMode.ADVENTURE));
                        try {
                            Long gm = gmTimes.get(GameMode.SPECTATOR);
                            if (gm != null) {
                                raw.addGmThree(gm);
                            }
                        } catch (NoSuchFieldError e) {
                        }
                        long playTime = uData.getPlayTime();
                        raw.addTotalPlaytime(playTime);
                        String playerName = uData.getName();
                        String url = HtmlUtils.getInspectUrl(playerName);
                        String html = Html.BUTTON.parse(url, playerName);

                        raw.getLatestLogins().put(html, uData.getLastPlayed());
                        raw.addTotalLoginTimes(uData.getLoginTimes());
                        int age = uData.getDemData().getAge();
                        if (age != -1) {
                            raw.getAges().add(age);
                        }
                        if (uData.isOp()) {
                            raw.addOps(1);
                        }
                        if (uData.isBanned()) {
                            raw.addTotalBanned(1);
                        } else if (uData.getLoginTimes() == 1) {
                            raw.addJoinleaver(1);
                        } else if (AnalysisUtils.isActive(uData.getLastPlayed(), playTime, uData.getLoginTimes())) {
                            raw.addActive(1);
                            raw.getPlaytimes().put(html, playTime);
                        } else {
                            raw.addInactive(1);
                        }
                        raw.addTotalKills(uData.getPlayerKills().size());
                        raw.addTotalMobKills(uData.getMobKills());
                        raw.addTotalDeaths(uData.getDeaths());
                        raw.getSessiondata().addAll(uData.getSessions());
                        raw.getRegistered().add(uData.getRegistered());
                    } catch (NullPointerException e) {
                        plugin.logError(Phrase.DATA_CORRUPTION_WARN.parse(uData.getUuid() + ""));
                    }
                });

                // Analyze & Save RawAnalysisData to AnalysisData
                createPlayerActivityGraphs(data, raw.getSessiondata(), raw.getRegistered());

                data.setTop20ActivePlayers(AnalysisUtils.createActivePlayersTable(raw.getPlaytimes(), 20));
                data.setRecentPlayers(AnalysisUtils.createListStringOutOfHashMapLong(raw.getLatestLogins(), 20));

                addPlanLiteToData(planLiteEnabled, plData, factionMap, townMap, totalVotes, totalMoney, data);

                long totalPlaytime = raw.getTotalPlaytime();
                data.setTotalPlayTime(totalPlaytime);
                data.setAveragePlayTime(totalPlaytime / rawData.size());
                data.setTotalLoginTimes(raw.getTotalLoginTimes());

                createActivityVisalization(raw.getTotalBanned(), raw.getActive(), raw.getInactive(), raw.getJoinleaver(), data);

                data.setOps(raw.getOps());

                analyzeAverageAge(raw.getAges(), data);
                createGamemodeUsageVisualization(raw.getGmZero(), raw.getGmOne(), raw.getGmTwo(), raw.getGmThree(), data);
                createCommandUseTable(raw, data);

                data.setTotaldeaths(raw.getTotalDeaths());
                data.setTotalkills(raw.getTotalKills());
                data.setTotalmobkills(raw.getTotalMobKills());

                data.setRefreshDate(new Date().getTime());
                analysisCache.cache(data);
                plugin.log(Phrase.ANALYSIS_COMPLETE + "");
                this.cancel();
            }

            private void createCommandUseTable(final RawAnalysisData raw, AnalysisData data) {
                if (!raw.getCommandUse().isEmpty()) {
                    data.setTop50CommandsListHtml(AnalysisUtils.createTableOutOfHashMap(raw.getCommandUse()));
                } else {
                    data.setTop50CommandsListHtml(Html.ERROR_TABLE.parse());
                }
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

            private void createPlayerActivityGraphs(AnalysisData data, List<SessionData> sData, List<Long> registered) {
                long now = new Date().toInstant().getEpochSecond() * (long) 1000;
                long scaleMonth = (long) 2592000 * (long) 1000;
                String[] urlAndNumber = AnalysisUtils.analyzeSessionData(sData, registered, scaleMonth, now);
                data.setPlayersChartImgHtmlMonth(urlAndNumber[0]);
                data.setNewPlayersMonth(Integer.parseInt(urlAndNumber[1]));
                long scaleWeek = 604800 * 1000;
                urlAndNumber = AnalysisUtils.analyzeSessionData(sData, registered, scaleWeek, now);
                data.setPlayersChartImgHtmlWeek(urlAndNumber[0]);
                data.setNewPlayersWeek(Integer.parseInt(urlAndNumber[1]));
                long scaleDay = 86400 * 1000;
                urlAndNumber = AnalysisUtils.analyzeSessionData(sData, registered, scaleDay, now);
                data.setPlayersChartImgHtmlDay(urlAndNumber[0]);
                data.setNewPlayersDay(Integer.parseInt(urlAndNumber[1]));
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
