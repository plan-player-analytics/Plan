package main.java.com.djrapitops.plan.data;

import com.djrapitops.plugin.api.TimeAmount;
import com.google.common.base.Objects;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.AnalysisContainer;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.tables.Actions;
import main.java.com.djrapitops.plan.systems.webserver.theme.Colors;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.ActivityStackGraphCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.PunchCardGraphCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.WorldMapCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.line.*;
import main.java.com.djrapitops.plan.utilities.html.graphs.pie.ActivityPieCreator;
import main.java.com.djrapitops.plan.utilities.html.graphs.pie.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.html.structure.AnalysisPluginsTabContentCreator;
import main.java.com.djrapitops.plan.utilities.html.structure.SessionTabStructureCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.CommandUseTableCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.SessionsTableCreator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Big container object for Data.
 * <p>
 * Contains parts that can be analysed. Each part has their own purpose.
 * <p>
 * Parts contain variables that can be added to. These variables are then
 * analysed using the analysis method.
 * <p>
 * After being analysed the ReplaceMap can be retrieved for replacing
 * placeholders on the server.html file.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisData extends RawData {

    private long refreshDate;

    private Map<String, Long> analyzedValues;
    private Set<StickyData> stickyMonthData;

    public AnalysisData() {
        analyzedValues = new HashMap<>();
        stickyMonthData = new HashSet<>();
    }

    public void parsePluginsSection(Map<PluginData, AnalysisContainer> containers) {
        String[] navAndTabs = AnalysisPluginsTabContentCreator.createContent(containers);
        addValue("navPluginsTabs", navAndTabs[0]);
        addValue("tabsPlugins", navAndTabs[1]);
    }

    private void addConstants() {
        addValue("version", MiscUtils.getIPlan().getVersion());
        addValue("worldPieColors", Settings.THEME_GRAPH_WORLD_PIE.toString());
        addValue("gmPieColors", Settings.THEME_GRAPH_GM_PIE.toString());
        addValue("serverName", Settings.SERVER_NAME.toString());
        addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());
        addValue("refresh", FormatUtils.formatTimeStamp(refreshDate));

        addValue("activityPieColors", Settings.THEME_GRAPH_ACTIVITY_PIE.toString());
        addValue("playersGraphColor", Colors.PLAYERS_ONLINE.getColor());
        addValue("tpsHighColor", Colors.TPS_HIGH.getColor());
        addValue("tpsMediumColor", Colors.TPS_MED.getColor());
        addValue("tpsLowColor", Colors.TPS_LOW.getColor());
        addValue("tpsMedium", Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber());
        addValue("tpsHigh", Settings.THEME_GRAPH_TPS_THRESHOLD_HIGH.getNumber());

        addValue("playersMax", ServerProfile.getPlayersMax());
        addValue("playersOnline", ServerProfile.getPlayersOnline());
    }

    public long getRefreshDate() {
        return refreshDate;
    }

    public void analyze(ServerProfile profile) {
        addConstants();
        long now = MiscUtils.getTime();
        refreshDate = now;
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        got("now", now);
        got("dayAgo", dayAgo);
        got("weekAgo", weekAgo);
        got("monthAgo", monthAgo);

        Map<UUID, List<Session>> sessions = profile.getSessions();
        List<Session> allSessions = profile.getAllSessions();
        allSessions.sort(new SessionStartComparator());

        List<PlayerProfile> players = profile.getPlayers();
        List<PlayerProfile> ops = profile.getOps().collect(Collectors.toList());
        long playersTotal = got("playersTotal", players.size());

        List<TPS> tpsData = profile.getTPSData(0, now).collect(Collectors.toList());
        List<TPS> tpsDataDay = profile.getTPSData(dayAgo, now).collect(Collectors.toList());
        List<TPS> tpsDataWeek = profile.getTPSData(weekAgo, now).collect(Collectors.toList());
        List<TPS> tpsDataMonth = profile.getTPSData(monthAgo, now).collect(Collectors.toList());

        List<String> geoLocations = profile.getGeoLocations();
        Map<String, Integer> commandUsage = profile.getCommandUsage();

        directProfileVariables(profile);
        performanceTab(tpsData, tpsDataDay, tpsDataWeek, tpsDataMonth);
        sessionData(monthAgo, sessions, allSessions);
        onlineActivityNumbers(profile, sessions, players);
        geolocationsTab(geoLocations);
        commandUsage(commandUsage);

        addValue("ops", ops.size());
        addValue("playersTotal", playersTotal);

        healthTab(now, monthAgo, players, tpsDataMonth);

        long totalPlaytime = profile.getTotalPlaytime();
        addValue("playtimeTotal", playersTotal != 0 ? FormatUtils.formatTimeAmount(totalPlaytime) : "No Players");
        addValue("playtimeAverage", playersTotal != 0 ? FormatUtils.formatTimeAmount(MathUtils.averageLong(totalPlaytime, playersTotal)) : "-");
    }

    private void healthTab(long now, long monthAgo, List<PlayerProfile> players, List<TPS> tpsDataMonth) {
        double serverHealth = 100.0;
        List<String> healthNotes = new ArrayList<>();

        TreeMap<Long, Map<String, Set<UUID>>> activityData = new TreeMap<>();
        long fourWeeksAgo = now - TimeAmount.WEEK.ms() * 4L;

        if (!players.isEmpty()) {
            for (PlayerProfile player : players) {
                for (long date = now; date >= now - TimeAmount.MONTH.ms() * 2L; date -= TimeAmount.WEEK.ms()) {
                    double activityIndex = player.getActivityIndex(date);
                    String index = FormatUtils.readableActivityIndex(activityIndex)[1];

                    Map<String, Set<UUID>> map = activityData.getOrDefault(date, new HashMap<>());
                    Set<UUID> uuids = map.getOrDefault(index, new HashSet<>());
                    uuids.add(player.getUuid());
                    map.put(index, uuids);
                    activityData.put(date, map);
                }
            }
        }

        Map<String, Set<UUID>> activityNow = activityData.getOrDefault(now, new HashMap<>());
        Map<String, Set<UUID>> activityFourWAgo = activityData.getOrDefault(fourWeeksAgo, new HashMap<>());

        String[] activityStackSeries = ActivityStackGraphCreator.createSeries(activityData);
        String activityPieSeries = ActivityPieCreator.createSeriesData(activityNow);

        addValue("activityStackCategories", activityStackSeries[0]);
        addValue("activityStackSeries", activityStackSeries[1]);
        addValue("activityPieSeries", activityPieSeries);

        Set<UUID> veryActiveNow = activityNow.getOrDefault("Very Active", new HashSet<>());
        Set<UUID> activeNow = activityNow.getOrDefault("Active", new HashSet<>());
        Set<UUID> regularNow = activityNow.getOrDefault("Regular", new HashSet<>());
        Set<UUID> veryActiveFWAG = activityFourWAgo.getOrDefault("Very Active", new HashSet<>());
        Set<UUID> activeFWAG = activityFourWAgo.getOrDefault("Active", new HashSet<>());
        Set<UUID> regularFWAG = activityFourWAgo.getOrDefault("Regular", new HashSet<>());

        addValue("playersRegular", (veryActiveNow.size() + activeNow.size() + regularNow.size()));

        Set<UUID> regularRemainCompareSet = new HashSet<>(regularFWAG);
        regularRemainCompareSet.addAll(activeFWAG);
        regularRemainCompareSet.addAll(veryActiveFWAG);

        int activeFWAGNum = regularRemainCompareSet.size();
        regularRemainCompareSet.removeAll(regularNow);
        regularRemainCompareSet.removeAll(activeNow);
        regularRemainCompareSet.removeAll(veryActiveNow);
        int notRegularAnymore = regularRemainCompareSet.size();
        int remain = activeFWAGNum - notRegularAnymore;
        double percRemain = remain * 100.0 / activeFWAGNum;

        Set<UUID> regularNewCompareSet = new HashSet<>(regularNow);
        regularNewCompareSet.addAll(activeNow);
        regularNewCompareSet.addAll(veryActiveNow);
        regularNewCompareSet.removeAll(regularFWAG);
        regularNewCompareSet.removeAll(activeFWAG);
        regularNewCompareSet.removeAll(veryActiveFWAG);
        int newActive = regularNewCompareSet.size();

        int change = newActive - notRegularAnymore;

        String remainNote = "";
        if (activeFWAGNum != 0) {
            remainNote = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            if (percRemain > 50) {
                remainNote += Html.GREEN_THUMB.parse();
            } else if (percRemain > 20) {
                remainNote += Html.YELLOW_FLAG.parse();
            } else {
                remainNote += Html.RED_WARN.parse();
                serverHealth -= 2.5;
            }

            remainNote += " " + FormatUtils.cutDecimals(percRemain) + "% of regular players have remained active (" + remain + "/" + activeFWAGNum + ")";
        }
        if (change > 0) {
            healthNotes.add(
                    "<p>" + Html.GREEN_THUMB.parse() + " Number of regular players has increased (+" + change + ")<br>" +
                            remainNote + "</p>");
        } else if (change == 0) {
            healthNotes.add(
                    "<p>" + Html.GREEN_THUMB.parse() + " Number of regular players has stayed the same (+" + change + ")<br>" +
                            remainNote + "</p>");
        } else if (change > -20) {
            healthNotes.add(
                    "<p>" + Html.YELLOW_FLAG.parse() + " Number of regular players has decreased (" + change + ")<br>" +
                            remainNote + "</p>");
            serverHealth -= 5;
        } else {
            healthNotes.add(
                    "<p>" + Html.RED_WARN.parse() + " Number of regular players has decreased (" + change + ")<br>" +
                            remainNote + "</p>");
            serverHealth -= 10;
        }

        double avgOnlineOnRegister = MathUtils.averageInt(stickyMonthData.stream().map(StickyData::getOnlineOnJoin));
        if (avgOnlineOnRegister >= 1) {
            healthNotes.add("<p>" + Html.GREEN_THUMB.parse() + " New Players have players to play with when they join ("
                    + FormatUtils.cutDecimals(avgOnlineOnRegister) + " on average)</p>");
        } else {
            healthNotes.add("<p>" + Html.YELLOW_FLAG.parse() + " New Players may not have players to play with when they join ("
                    + FormatUtils.cutDecimals(avgOnlineOnRegister) + " on average)</p>");
            serverHealth -= 5;
        }

        long newM = value("newM");
        long stuckPerM = value("stuckPerM");

        if (newM != 0) {
            double stuckPerc = MathUtils.averageDouble(stuckPerM, newM) * 100;
            if (stuckPerc >= 25) {
                healthNotes.add("<p>" + Html.GREEN_THUMB.parse() + " " + FormatUtils.cutDecimals(stuckPerc)
                        + "% of new players have stuck around (" + stuckPerM + "/" + newM + ")</p>");
            } else {
                healthNotes.add("<p>" + Html.YELLOW_FLAG.parse() + " " + FormatUtils.cutDecimals(stuckPerc)
                        + "% of new players have stuck around (" + stuckPerM + "/" + newM + ")</p>");
            }
        }

        List<PlayerProfile> currentActivePlayers = players.stream()
                .filter(player -> player.getActivityIndex(now) >= 1.75)
                .collect(Collectors.toList());

        long twoWeeksAgo = now - TimeAmount.WEEK.ms() * 2L;

        long totalFourToTwoWeeks = 0;
        long totalLastTwoWeeks = 0;
        for (PlayerProfile activePlayer : currentActivePlayers) {
            totalFourToTwoWeeks += activePlayer.getPlaytime(monthAgo, twoWeeksAgo);
            totalLastTwoWeeks += activePlayer.getPlaytime(twoWeeksAgo, now);
        }
        int currentlyActive = currentActivePlayers.size();
        if (currentlyActive != 0) {
            long avgFourToTwoWeeks = MathUtils.averageLong(totalFourToTwoWeeks, currentlyActive);
            long avgLastTwoWeeks = MathUtils.averageLong(totalLastTwoWeeks, currentlyActive);
            String avgLastTwoWeeksString = FormatUtils.formatTimeAmount(avgLastTwoWeeks);
            String avgFourToTwoWeeksString = FormatUtils.formatTimeAmount(avgFourToTwoWeeks);
            if (avgFourToTwoWeeks >= avgLastTwoWeeks) {
                healthNotes.add("<p>" + Html.GREEN_THUMB.parse() + " Active players to have things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)</p>");
            } else if (avgFourToTwoWeeks - avgLastTwoWeeks > TimeAmount.HOUR.ms() * 2L) {
                healthNotes.add("<p>" + Html.RED_WARN.parse() + " Active players might to be running out of things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)</p>");
                serverHealth -= 5;
            } else {
                healthNotes.add("<p>" + Html.YELLOW_FLAG.parse() + " Active players might to be running out of things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)</p>");
            }
        }

        long serverDownTime = ServerProfile.serverDownTime(tpsDataMonth);
//        long serverIdleTime = ServerProfile.serverIdleTime(tpsDataMonth);
        double aboveThreshold = ServerProfile.aboveLowThreshold(tpsDataMonth);
        long tpsSpikeMonth = value("tpsSpikeMonth");

        String avgLowThresholdString = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        if (aboveThreshold >= 0.96) {
            avgLowThresholdString += Html.GREEN_THUMB.parse();
        } else if (aboveThreshold >= 0.9) {
            avgLowThresholdString += Html.YELLOW_FLAG.parse();
            serverHealth *= 0.9;
        } else {
            avgLowThresholdString += Html.RED_WARN.parse();
            serverHealth *= 0.6;
        }
        avgLowThresholdString += " Average TPS was above Low Threshold "
                + FormatUtils.cutDecimals(aboveThreshold * 100.0) + "% of the time";

        if (tpsSpikeMonth <= 5) {
            healthNotes.add("<p>" + Html.GREEN_THUMB.parse()
                    + " Average TPS dropped below Low Threshold (" + Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber() + ")" +
                    " " + tpsSpikeMonth + " times<br>" +
                    avgLowThresholdString + "</p>");
        } else if (tpsSpikeMonth <= 25) {
            healthNotes.add("<p>" + Html.YELLOW_FLAG.parse()
                    + " Average TPS dropped below Low Threshold (" + Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber() + ")" +
                    " " + tpsSpikeMonth + " times<br>" +
                    avgLowThresholdString + "</p>");
            serverHealth *= 0.95;
        } else {
            healthNotes.add("<p>" + Html.RED_WARN.parse()
                    + " Average TPS dropped below Low Threshold (" + Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber() + ")" +
                    " " + tpsSpikeMonth + " times<br>" +
                    avgLowThresholdString + "</p>");
            serverHealth *= 0.8;
        }

        if (serverDownTime <= TimeAmount.DAY.ms()) {
            healthNotes.add("<p>" + Html.GREEN_THUMB.parse() + " Total Server downtime (No Data) was "
                    + FormatUtils.formatTimeAmount(serverDownTime) + "</p>");
        } else if (serverDownTime <= TimeAmount.WEEK.ms()) {
            healthNotes.add("<p>" + Html.YELLOW_FLAG.parse() + " Total Server downtime (No Data) was "
                    + FormatUtils.formatTimeAmount(serverDownTime) + "</p>");
            serverHealth *= 0.6;
        } else {
            healthNotes.add("<p>" + Html.RED_WARN.parse() + " Total Server downtime (No Data) was "
                    + FormatUtils.formatTimeAmount(serverDownTime) + "</p>");
            serverHealth *= 0.3;
        }
//        healthNotes.add("<p>" + Html.FA_COLORED_ICON.parse("red", "life-ring") + " Server was idle (No Players) "
//                + FormatUtils.formatTimeAmount(serverIdleTime) + " in total</p>");

        StringBuilder healthNoteBuilder = new StringBuilder();
        for (String healthNote : healthNotes) {
            healthNoteBuilder.append(healthNote);
        }
        addValue("healthNotes", healthNoteBuilder.toString());
        addValue("healthIndex", serverHealth);
    }

    private void commandUsage(Map<String, Integer> commandUsage) {
        addValue("commandUniqueCount", String.valueOf(commandUsage.size()));
        addValue("commandCount", MathUtils.sumInt(commandUsage.values().stream().map(i -> (int) i)));
        addValue("tableBodyCommands", HtmlUtils.removeXSS(CommandUseTableCreator.createTable(commandUsage)));
    }

    private void geolocationsTab(List<String> geoLocations) {
        addValue("geoMapSeries", WorldMapCreator.createDataSeries(geoLocations));
    }

    private void onlineActivityNumbers(ServerProfile profile, Map<UUID, List<Session>> sessions, List<PlayerProfile> players) {
        long now = value("now");
        long dayAgo = value("dayAgo");
        long weekAgo = value("weekAgo");
        long monthAgo = value("monthAgo");

        List<PlayerProfile> newDay = profile.getPlayersWhoRegistered(dayAgo, now).collect(Collectors.toList());
        List<PlayerProfile> newWeek = profile.getPlayersWhoRegistered(weekAgo, now).collect(Collectors.toList());
        List<PlayerProfile> newMonth = profile.getPlayersWhoRegistered(monthAgo, now).collect(Collectors.toList());
        List<PlayerProfile> uniqueDay = profile.getPlayersWhoPlayedBetween(dayAgo, now).collect(Collectors.toList());
        List<PlayerProfile> uniqueWeek = profile.getPlayersWhoPlayedBetween(weekAgo, now).collect(Collectors.toList());
        List<PlayerProfile> uniqueMonth = profile.getPlayersWhoPlayedBetween(monthAgo, now).collect(Collectors.toList());

        int uniqD = uniqueDay.size();
        int uniqW = uniqueWeek.size();
        int uniqM = uniqueMonth.size();
        long newD = got("newD", newDay.size());
        long newW = got("newW", newWeek.size());
        long newM = got("newM", newMonth.size());
        long playersTotal = value("playersTotal");

        addValue("playersDay", uniqD);
        addValue("playersWeek", uniqW);
        addValue("playersMonth", uniqM);
        addValue("playersNewDay", newD);
        addValue("playersNewWeek", newW);
        addValue("playersNewMonth", newM);

        addValue("playersAverage", AnalysisUtils.getUniqueJoinsPerDay(sessions, -1));
        addValue("playersAverageDay", AnalysisUtils.getUniqueJoinsPerDay(sessions, dayAgo));
        addValue("playersAverageWeek", AnalysisUtils.getUniqueJoinsPerDay(sessions, weekAgo));
        addValue("playersAverageMonth", AnalysisUtils.getUniqueJoinsPerDay(sessions, monthAgo));
        addValue("playersNewAverage", AnalysisUtils.getNewUsersPerDay(toRegistered(players), -1, playersTotal));
        addValue("playersNewAverageDay", AnalysisUtils.getNewUsersPerDay(toRegistered(newDay), -1, newD));
        addValue("playersNewAverageWeek", AnalysisUtils.getNewUsersPerDay(toRegistered(newWeek), -1, newW));
        addValue("playersNewAverageMonth", AnalysisUtils.getNewUsersPerDay(toRegistered(newMonth), -1, newM));

        stickiness(now, weekAgo, monthAgo, newDay, newWeek, newMonth);
    }

    private void stickiness(long now, long weekAgo, long monthAgo,
                            List<PlayerProfile> newDay, List<PlayerProfile> newWeek, List<PlayerProfile> newMonth) {
        long newD = value("newD");
        long newW = value("newW");
        long newM = value("newM");

        long fourDaysAgo = now - TimeAmount.DAY.ms() * 4L;
        long twoWeeksAgo = now - TimeAmount.WEEK.ms() * 2L;

        List<PlayerProfile> playersStuckPerMonth = newMonth.stream()
                .filter(p -> p.playedBetween(monthAgo, twoWeeksAgo) && p.playedBetween(twoWeeksAgo, now))
                .collect(Collectors.toList());
        List<PlayerProfile> playersStuckPerWeek = newWeek.stream()
                .filter(p -> p.playedBetween(weekAgo, fourDaysAgo) && p.playedBetween(fourDaysAgo, now))
                .collect(Collectors.toList());

        int stuckPerM = playersStuckPerMonth.size();
        int stuckPerW = playersStuckPerWeek.size();
        got("stuckPerM", stuckPerM);
        got("stuckPerW", stuckPerW);

        addValue("playersStuckMonth", stuckPerM);
        addValue("playersStuckWeek", stuckPerW);
        addValue("playersStuckPercMonth", newM != 0 ? FormatUtils.cutDecimals(MathUtils.averageDouble(stuckPerM, newM) * 100.0) + "%" : "-");
        addValue("playersStuckPercWeek", newW != 0 ? FormatUtils.cutDecimals(MathUtils.averageDouble(stuckPerW, newW) * 100.0) + "%" : "-");

        if (newD != 0) {
            // New Players
            stickyMonthData = newMonth.stream().map(StickyData::new).distinct().collect(Collectors.toSet());
            Set<StickyData> stickyW = playersStuckPerMonth.stream().map(StickyData::new).distinct().collect(Collectors.toSet());
            // New Players who stayed
            Set<StickyData> stickyStuckM = newMonth.stream().map(StickyData::new).distinct().collect(Collectors.toSet());
            Set<StickyData> stickyStuckW = playersStuckPerWeek.stream().map(StickyData::new).distinct().collect(Collectors.toSet());

            int stuckPerD = 0;
            for (PlayerProfile playerProfile : newDay) {
                StickyData data = new StickyData(playerProfile);

                Set<StickyData> similarM = new HashSet<>();
                Set<StickyData> similarW = new HashSet<>();
                for (StickyData stickyData : stickyMonthData) {
                    if (stickyData.distance(data) < 2.5) {
                        similarM.add(stickyData);
                    }
                }
                for (StickyData stickyData : stickyW) {
                    if (stickyData.distance(data) < 2.5) {
                        similarW.add(stickyData);
                    }
                }

                double probability = 1.0;

                int stickM = 0;
                for (StickyData stickyData : stickyStuckM) {
                    if (similarM.contains(stickyData)) {
                        stickM++;
                    }
                }

                probability *= (stickM / similarM.size());

                int stickW = 0;
                for (StickyData stickyData : stickyStuckW) {
                    if (similarW.contains(stickyData)) {
                        stickW++;
                    }
                }

                probability *= (stickW / similarW.size());

                if (probability >= 0.5) {
                    stuckPerD++;
                }
            }
            addValue("playersStuckDay", stuckPerD);
            addValue("playersStuckPercDay", FormatUtils.cutDecimals(MathUtils.averageDouble(stuckPerD, newD) * 100.0) + "%");
        } else {
            addValue("playersStuckDay", 0);
            addValue("playersStuckPercDay", "-");
        }
    }

    private List<Long> toRegistered(List<PlayerProfile> players) {
        return players.stream().map(PlayerProfile::getRegistered).collect(Collectors.toList());
    }

    private void sessionData(long monthAgo, Map<UUID, List<Session>> sessions, List<Session> allSessions) {
        List<Session> sessionsMonth = allSessions.stream()
                .filter(s -> s.getSessionStart() >= monthAgo)
                .collect(Collectors.toList());
        String[] tables = SessionsTableCreator.createTable(sessions, allSessions);
        String[] sessionContent = SessionTabStructureCreator.createStructure(sessions, allSessions);

        addValue("sessionCount", allSessions.size());
        addValue("accordionSessions", sessionContent[0]);
        addValue("sessionTabGraphViewFunctions", sessionContent[1]);
        addValue("tableBodySessions", tables[0]);
        addValue("listRecentLogins", tables[1]);
        addValue("sessionAverage", FormatUtils.formatTimeAmount(MathUtils.averageLong(allSessions.stream().map(Session::getLength))));
        addValue("punchCardSeries", PunchCardGraphCreator.createDataSeries(sessionsMonth));

        addValue("deaths", ServerProfile.getDeathCount(allSessions));
        addValue("mobKillCount", ServerProfile.getMobKillCount(allSessions));
        addValue("killCount", ServerProfile.getPlayerKills(allSessions).size());
    }

    private void directProfileVariables(ServerProfile profile) {
        WorldTimes worldTimes = profile.getServerWorldtimes();
        long allTimePeak = profile.getAllTimePeak();
        long lastPeak = profile.getLastPeakDate();

        addValue("tablePlayerlist", Html.TABLE_PLAYERS.parse(profile.createPlayersTableBody()));
        addValue("worldTotal", FormatUtils.formatTimeAmount(worldTimes.getTotal()));
        String[] seriesData = WorldPieCreator.createSeriesData(worldTimes);
        addValue("worldSeries", seriesData[0]);
        addValue("gmSeries", seriesData[1]);
        addValue("lastPeakTime", lastPeak != -1 ? FormatUtils.formatTimeStampYear(lastPeak) : "No Data");
        addValue("playersLastPeak", lastPeak != -1 ? profile.getLastPeakPlayers() : "-");
        addValue("bestPeakTime", allTimePeak != -1 ? FormatUtils.formatTimeStampYear(allTimePeak) : "No Data");
        addValue("playersBestPeak", allTimePeak != -1 ? profile.getAllTimePeakPlayers() : "-");
    }

    private void performanceTab(List<TPS> tpsData, List<TPS> tpsDataDay, List<TPS> tpsDataWeek, List<TPS> tpsDataMonth) {
        got("tpsSpikeMonth", ServerProfile.getLowSpikeCount(tpsDataMonth));
        got("tpsSpikeWeek", ServerProfile.getLowSpikeCount(tpsDataWeek));
        got("tpsSpikeDay", ServerProfile.getLowSpikeCount(tpsDataDay));
        addValue("tpsSpikeMonth", value("tpsSpikeMonth"));
        addValue("tpsSpikeWeek", value("tpsSpikeWeek"));
        addValue("tpsSpikeDay", value("tpsSpikeDay"));

        addValue("playersOnlineSeries", PlayerActivityGraphCreator.buildSeriesDataString(tpsData));
        addValue("tpsSeries", TPSGraphCreator.buildSeriesDataString(tpsData));
        addValue("cpuSeries", CPUGraphCreator.buildSeriesDataString(tpsData));
        addValue("ramSeries", RamGraphCreator.buildSeriesDataString(tpsData));
        addValue("entitySeries", WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsData));
        addValue("chunkSeries", WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsData));

        double averageCPUMonth = MathUtils.averageDouble(tpsDataMonth.stream().map(TPS::getCPUUsage).filter(i -> i != 0));
        double averageCPUWeek = MathUtils.averageDouble(tpsDataWeek.stream().map(TPS::getCPUUsage).filter(i -> i != 0));
        double averageCPUDay = MathUtils.averageDouble(tpsDataDay.stream().map(TPS::getCPUUsage).filter(i -> i != 0));

        addValue("tpsAverageMonth", FormatUtils.cutDecimals(MathUtils.averageDouble(tpsDataMonth.stream().map(TPS::getTicksPerSecond))));
        addValue("tpsAverageWeek", FormatUtils.cutDecimals(MathUtils.averageDouble(tpsDataWeek.stream().map(TPS::getTicksPerSecond))));
        addValue("tpsAverageDay", FormatUtils.cutDecimals(MathUtils.averageDouble(tpsDataDay.stream().map(TPS::getTicksPerSecond))));

        addValue("cpuAverageMonth", averageCPUMonth >= 0 ? FormatUtils.cutDecimals(averageCPUMonth) + "%" : "Unavailable");
        addValue("cpuAverageWeek", averageCPUWeek >= 0 ? FormatUtils.cutDecimals(averageCPUWeek) + "%" : "Unavailable");
        addValue("cpuAverageDay", averageCPUDay >= 0 ? FormatUtils.cutDecimals(averageCPUDay) + "%" : "Unavailable");

        addValue("ramAverageMonth", FormatUtils.cutDecimals(MathUtils.averageLong(tpsDataMonth.stream().map(TPS::getUsedMemory).filter(i -> i != 0))));
        addValue("ramAverageWeek", FormatUtils.cutDecimals(MathUtils.averageLong(tpsDataWeek.stream().map(TPS::getUsedMemory).filter(i -> i != 0))));
        addValue("ramAverageDay", FormatUtils.cutDecimals(MathUtils.averageLong(tpsDataDay.stream().map(TPS::getUsedMemory).filter(i -> i != 0))));

        addValue("entityAverageMonth", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataMonth.stream().map(TPS::getEntityCount).filter(i -> i != 0))));
        addValue("entityAverageWeek", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataWeek.stream().map(TPS::getEntityCount).filter(i -> i != 0))));
        addValue("entityAverageDay", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataDay.stream().map(TPS::getEntityCount).filter(i -> i != 0))));

        addValue("chunkAverageMonth", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataMonth.stream().map(TPS::getChunksLoaded).filter(i -> i != 0))));
        addValue("chunkAverageWeek", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataWeek.stream().map(TPS::getChunksLoaded).filter(i -> i != 0))));
        addValue("chunkAverageDay", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataDay.stream().map(TPS::getChunksLoaded).filter(i -> i != 0))));
    }

    private long got(String key, long v) {
        analyzedValues.put(key, v);
        return v;
    }

    private long value(String key) {
        return analyzedValues.getOrDefault(key, 0L);
    }
}

class StickyData {
    private final double activityIndex;
    private Integer messagesSent;
    private Integer onlineOnJoin;

    public StickyData(PlayerProfile player) {
        activityIndex = player.getActivityIndex(player.getRegistered() + TimeAmount.DAY.ms());
        for (Action action : player.getActions()) {
            if (messagesSent == null && action.getDoneAction() == Actions.FIRST_LOGOUT) {
                String additionalInfo = action.getAdditionalInfo();
                String[] split = additionalInfo.split(": ");
                if (split.length == 2) {
                    try {
                        messagesSent = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (onlineOnJoin == null && action.getDoneAction() == Actions.FIRST_SESSION) {
                String additionalInfo = action.getAdditionalInfo();
                String[] split = additionalInfo.split(" ");
                if (split.length == 3) {
                    try {
                        onlineOnJoin = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (messagesSent == null) {
            messagesSent = 0;
        }
        if (onlineOnJoin == null) {
            onlineOnJoin = 0;
        }
    }

    public double distance(StickyData data) {
        double num = 0;
        num += Math.abs(data.activityIndex - activityIndex) * 2.0;
        num += Math.abs(data.onlineOnJoin - onlineOnJoin) / 10.0;
        num += Math.abs(data.messagesSent - messagesSent) / 10.0;

        return num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StickyData that = (StickyData) o;
        return Double.compare(that.activityIndex, activityIndex) == 0 &&
                Objects.equal(messagesSent, that.messagesSent) &&
                Objects.equal(onlineOnJoin, that.onlineOnJoin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityIndex, messagesSent, onlineOnJoin);
    }

    public int getOnlineOnJoin() {
        return onlineOnJoin;
    }
}
