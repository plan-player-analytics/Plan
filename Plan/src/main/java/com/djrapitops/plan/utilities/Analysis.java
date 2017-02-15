package main.java.com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.RawAnalysisData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.ui.Html;
import static org.bukkit.Bukkit.getOfflinePlayer;
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
                final RawAnalysisData sorted = new RawAnalysisData();
                sorted.setCommandUse(plugin.getDB().getCommandUse());
                log(Phrase.ANALYSIS_BEGIN_ANALYSIS + "");
                AnalysisData analysisData = new AnalysisData();

                // Fill Dataset with userdata.
                rawData.parallelStream().forEach((uData) -> {
                    try {
                        HashMap<GameMode, Long> gmTimes = uData.getGmTimes();
                        sorted.addToGmZero(gmTimes.get(GameMode.SURVIVAL));
                        sorted.addToGmOne(gmTimes.get(GameMode.CREATIVE));
                        sorted.addToGmTwo(gmTimes.get(GameMode.ADVENTURE));
                        try {
                            Long gm = gmTimes.get(GameMode.SPECTATOR);
                            if (gm != null) {
                                sorted.addGmThree(gm);
                            }
                        } catch (NoSuchFieldError e) {
                        }
                        long playTime = uData.getPlayTime();
                        sorted.addTotalPlaytime(playTime);
                        String playerName = uData.getName();
                        String url = HtmlUtils.getInspectUrl(playerName);
                        String html = Html.BUTTON.parse(url, playerName);

                        sorted.getLatestLogins().put(html, uData.getLastPlayed());
                        sorted.addTotalLoginTimes(uData.getLoginTimes());
                        int age = uData.getDemData().getAge();
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
                        sorted.addTotalKills(uData.getPlayerKills().size());
                        sorted.addTotalMobKills(uData.getMobKills());
                        sorted.addTotalDeaths(uData.getDeaths());
                        sorted.getSessiondata().addAll(uData.getSessions());
                        sorted.getRegistered().add(uData.getRegistered());
                    } catch (NullPointerException e) {
                        plugin.logError(Phrase.DATA_CORRUPTION_WARN.parse(uData.getUuid() + ""));
                    }
                });

                // Analyze & Save RawAnalysisData to AnalysisData
                createPlayerActivityGraphs(analysisData, sorted.getSessiondata(), sorted.getRegistered());

                analysisData.setTop20ActivePlayers(AnalysisUtils.createActivePlayersTable(sorted.getPlaytimes(), 20));
                analysisData.setRecentPlayers(AnalysisUtils.createListStringOutOfHashMapLong(sorted.getLatestLogins(), 20));

                long totalPlaytime = sorted.getTotalPlaytime();
                analysisData.setTotalPlayTime(totalPlaytime);
                analysisData.setAveragePlayTime(totalPlaytime / rawData.size());
                analysisData.setTotalLoginTimes(sorted.getTotalLoginTimes());

                createActivityVisalization(sorted.getTotalBanned(), sorted.getActive(), sorted.getInactive(), sorted.getJoinleaver(), analysisData);

                analysisData.setOps(sorted.getOps());

                analyzeAverageAge(sorted.getAges(), analysisData);
                createGamemodeUsageVisualization(sorted.getGmZero(), sorted.getGmOne(), sorted.getGmTwo(), sorted.getGmThree(), analysisData);
                createCommandUseTable(sorted, analysisData);

                analysisData.setTotaldeaths(sorted.getTotalDeaths());
                analysisData.setTotalkills(sorted.getTotalKills());
                analysisData.setTotalmobkills(sorted.getTotalMobKills());

                analysisData.setRefreshDate(new Date().getTime());
                analysisCache.cache(analysisData);
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
