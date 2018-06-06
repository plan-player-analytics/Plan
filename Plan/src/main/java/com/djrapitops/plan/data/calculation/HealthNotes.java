/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.calculation;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.container.StickyData;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class in charge of Server health analysis.
 *
 * @author Rsl1122
 */
public class HealthNotes {

    private final List<String> notes;
    private final AnalysisData analysisData;
    private final SortedMap<Long, Map<String, Set<UUID>>> activityData;
    private final List<TPS> tpsDataMonth;
    private final long now;
    private final long fourWeeksAgo;
    private double serverHealth;

    public HealthNotes(AnalysisData analysisData, SortedMap<Long, Map<String, Set<UUID>>> activityData, List<TPS> tpsDataMonth, long now) {
        this.notes = new ArrayList<>();
        serverHealth = 100.0;

        this.analysisData = analysisData;
        this.activityData = activityData;
        this.tpsDataMonth = tpsDataMonth;
        this.now = now;
        this.fourWeeksAgo = now - TimeAmount.WEEK.ms() * 4L;
    }

    public void analyzeHealth() {
        activityChangeNote();
        newPlayerNote();
        activePlayerPlaytimeChange();
        lowPerformance();
    }

    public String parse() {
        StringBuilder healthNoteBuilder = new StringBuilder();
        for (String healthNote : notes) {
            healthNoteBuilder.append(healthNote);
        }
        return healthNoteBuilder.toString();
    }

    public double getServerHealth() {
        return serverHealth;
    }

    private void activityChangeNote() {
        Map<String, Set<UUID>> activityNow = activityData.getOrDefault(now, new HashMap<>());
        Set<UUID> veryActiveNow = activityNow.getOrDefault("Very Active", new HashSet<>());
        Set<UUID> activeNow = activityNow.getOrDefault("Active", new HashSet<>());
        Set<UUID> regularNow = activityNow.getOrDefault("Regular", new HashSet<>());

        Map<String, Set<UUID>> activityFourWAgo = activityData.getOrDefault(fourWeeksAgo, new HashMap<>());
        Set<UUID> veryActiveFWAG = activityFourWAgo.getOrDefault("Very Active", new HashSet<>());
        Set<UUID> activeFWAG = activityFourWAgo.getOrDefault("Active", new HashSet<>());
        Set<UUID> regularFWAG = activityFourWAgo.getOrDefault("Regular", new HashSet<>());

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

        int newActive = getNewActive(veryActiveNow, activeNow, regularNow, veryActiveFWAG, activeFWAG, regularFWAG);

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

            remainNote += " " + FormatUtils.cutDecimals(percRemain) + "% of regular players have remained active ("
                    + remain + "/" + activeFWAGNum + ")";
        }
        if (change > 0) {
            notes.add(
                    "<p>" + Html.GREEN_THUMB.parse() + " Number of regular players has increased (+" + change + ")<br>" +
                            remainNote + "</p>");
        } else if (change == 0) {
            notes.add(
                    "<p>" + Html.GREEN_THUMB.parse() + " Number of regular players has stayed the same (+" + change + ")<br>" +
                            remainNote + "</p>");
        } else if (change > -20) {
            notes.add(
                    "<p>" + Html.YELLOW_FLAG.parse() + " Number of regular players has decreased (" + change + ")<br>" +
                            remainNote + "</p>");
            serverHealth -= 5;
        } else {
            notes.add(
                    "<p>" + Html.RED_WARN.parse() + " Number of regular players has decreased (" + change + ")<br>" +
                            remainNote + "</p>");
            serverHealth -= 10;
        }
    }

    private void newPlayerNote() {
        double avgOnlineOnRegister = MathUtils.averageDouble(analysisData.getStickyMonthData().stream().map(StickyData::getOnlineOnJoin));
        if (avgOnlineOnRegister >= 1) {
            notes.add("<p>" + Html.GREEN_THUMB.parse() + " New Players have players to play with when they join ("
                    + FormatUtils.cutDecimals(avgOnlineOnRegister) + " on average)</p>");
        } else {
            notes.add("<p>" + Html.YELLOW_FLAG.parse() + " New Players may not have players to play with when they join ("
                    + FormatUtils.cutDecimals(avgOnlineOnRegister) + " on average)</p>");
            serverHealth -= 5;
        }

        long newM = analysisData.value("newM");
        long stuckPerM = analysisData.value("stuckPerM");

        if (newM != 0) {
            double stuckPerc = MathUtils.averageDouble(stuckPerM, newM) * 100;
            if (stuckPerc >= 25) {
                notes.add("<p>" + Html.GREEN_THUMB.parse() + " " + FormatUtils.cutDecimals(stuckPerc)
                        + "% of new players have stuck around (" + stuckPerM + "/" + newM + ")</p>");
            } else {
                notes.add("<p>" + Html.YELLOW_FLAG.parse() + " " + FormatUtils.cutDecimals(stuckPerc)
                        + "% of new players have stuck around (" + stuckPerM + "/" + newM + ")</p>");
            }
        }
    }

    private void activePlayerPlaytimeChange() {
        List<PlayerProfile> currentActivePlayers = analysisData.getPlayers().stream()
                .filter(player -> player.getActivityIndex(now).getValue() >= 1.75)
                .collect(Collectors.toList());

        long twoWeeksAgo = now - TimeAmount.WEEK.ms() * 2L;

        long totalFourToTwoWeeks = 0;
        long totalLastTwoWeeks = 0;
        for (PlayerProfile activePlayer : currentActivePlayers) {
            totalFourToTwoWeeks += activePlayer.getPlaytime(analysisData.value("monthAgo"), twoWeeksAgo);
            totalLastTwoWeeks += activePlayer.getPlaytime(twoWeeksAgo, now);
        }
        int currentlyActive = currentActivePlayers.size();
        if (currentlyActive != 0) {
            long avgFourToTwoWeeks = MathUtils.averageLong(totalFourToTwoWeeks, currentlyActive);
            long avgLastTwoWeeks = MathUtils.averageLong(totalLastTwoWeeks, currentlyActive);
            String avgLastTwoWeeksString = Formatters.timeAmount().apply(avgLastTwoWeeks);
            String avgFourToTwoWeeksString = Formatters.timeAmount().apply(avgFourToTwoWeeks);
            if (avgFourToTwoWeeks >= avgLastTwoWeeks) {
                notes.add("<p>" + Html.GREEN_THUMB.parse() + " Active players seem to have things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)</p>");
            } else if (avgFourToTwoWeeks - avgLastTwoWeeks > TimeAmount.HOUR.ms() * 2L) {
                notes.add("<p>" + Html.RED_WARN.parse() + " Active players might be running out of things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)</p>");
                serverHealth -= 5;
            } else {
                notes.add("<p>" + Html.YELLOW_FLAG.parse() + " Active players might be running out of things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)</p>");
            }
        }
    }

    private void lowPerformance() {
        long serverDownTime = ServerProfile.serverDownTime(tpsDataMonth);
        double aboveThreshold = ServerProfile.aboveLowThreshold(tpsDataMonth);
        long tpsSpikeMonth = analysisData.value("tpsSpikeMonth");

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
            notes.add("<p>" + Html.GREEN_THUMB.parse()
                    + " Average TPS dropped below Low Threshold (" + Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber() + ")" +
                    " " + tpsSpikeMonth + " times<br>" +
                    avgLowThresholdString + "</p>");
        } else if (tpsSpikeMonth <= 25) {
            notes.add("<p>" + Html.YELLOW_FLAG.parse()
                    + " Average TPS dropped below Low Threshold (" + Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber() + ")" +
                    " " + tpsSpikeMonth + " times<br>" +
                    avgLowThresholdString + "</p>");
            serverHealth *= 0.95;
        } else {
            notes.add("<p>" + Html.RED_WARN.parse()
                    + " Average TPS dropped below Low Threshold (" + Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber() + ")" +
                    " " + tpsSpikeMonth + " times<br>" +
                    avgLowThresholdString + "</p>");
            serverHealth *= 0.8;
        }

        Formatter<Long> formatter = Formatters.timeAmount();
        if (serverDownTime <= TimeAmount.DAY.ms()) {
            notes.add("<p>" + Html.GREEN_THUMB.parse() + " Total Server downtime (No Data) was "
                    + formatter.apply(serverDownTime) + "</p>");
        } else if (serverDownTime <= TimeAmount.WEEK.ms()) {
            notes.add("<p>" + Html.YELLOW_FLAG.parse() + " Total Server downtime (No Data) was "
                    + formatter.apply(serverDownTime) + "</p>");
            serverHealth *= (TimeAmount.WEEK.ms() - serverDownTime) * 1.0 / TimeAmount.WEEK.ms();
        } else {
            notes.add("<p>" + Html.RED_WARN.parse() + " Total Server downtime (No Data) was "
                    + formatter.apply(serverDownTime) + "</p>");
            serverHealth *= (TimeAmount.MONTH.ms() - serverDownTime) * 1.0 / TimeAmount.MONTH.ms();
        }
    }

    private int getNewActive(Set<UUID> veryActiveNow, Set<UUID> activeNow, Set<UUID> regularNow, Set<UUID> veryActiveFWAG, Set<UUID> activeFWAG, Set<UUID> regularFWAG) {
        Set<UUID> regularNewCompareSet = new HashSet<>(regularNow);
        regularNewCompareSet.addAll(activeNow);
        regularNewCompareSet.addAll(veryActiveNow);
        regularNewCompareSet.removeAll(regularFWAG);
        regularNewCompareSet.removeAll(activeFWAG);
        regularNewCompareSet.removeAll(veryActiveFWAG);
        return regularNewCompareSet.size();
    }
}