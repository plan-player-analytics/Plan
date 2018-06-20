package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;

/**
 * Server Health analysis mutator.
 *
 * @author Rsl1122
 */
public class HealthInformation {

    private final AnalysisContainer analysisContainer;
    private final List<String> notes;
    private final long now;
    private double serverHealth;
    private long fourWeeksAgo;

    public HealthInformation(AnalysisContainer analysisContainer) {
        this.analysisContainer = analysisContainer;
        this.notes = new ArrayList<>();
        calculate();

        now = analysisContainer.getUnsafe(AnalysisKeys.ANALYSIS_TIME);
        fourWeeksAgo = analysisContainer.getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO);
    }

    public String toHtml() {
        StringBuilder healthNoteBuilder = new StringBuilder();
        for (String healthNote : notes) {
            healthNoteBuilder.append(healthNote);
        }
        return healthNoteBuilder.toString();
    }

    private void calculate() {
        activityChangeNote();
        newPlayerNote();
        activePlayerPlaytimeChange();
        lowPerformance();
    }

    public double getServerHealth() {
        return serverHealth;
    }

    private void activityChangeNote() {
        TreeMap<Long, Map<String, Set<UUID>>> activityData = analysisContainer.getUnsafe(AnalysisKeys.ACTIVITY_DATA);

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
        Key<PlayersMutator> newMonth = new Key<>(PlayersMutator.class, "NEW_MONTH");
        PlayersMutator newPlayersMonth = analysisContainer.getValue(newMonth).orElse(new PlayersMutator(new ArrayList<>()));
        PlayersOnlineResolver onlineResolver = analysisContainer.getUnsafe(AnalysisKeys.PLAYERS_ONLINE_RESOLVER);

        double avgOnlineOnRegister = newPlayersMonth.registerDates().stream()
                .mapToInt(date -> onlineResolver.getOnlineOn(date).orElse(-1))
                .filter(value -> value != -1)
                .average().orElse(0);
        if (avgOnlineOnRegister >= 1) {
            notes.add("<p>" + Html.GREEN_THUMB.parse() + " New Players have players to play with when they join ("
                    + FormatUtils.cutDecimals(avgOnlineOnRegister) + " on average)</p>");
        } else {
            notes.add("<p>" + Html.YELLOW_FLAG.parse() + " New Players may not have players to play with when they join ("
                    + FormatUtils.cutDecimals(avgOnlineOnRegister) + " on average)</p>");
            serverHealth -= 5;
        }

        long playersNewMonth = analysisContainer.getValue(AnalysisKeys.PLAYERS_NEW_MONTH).orElse(0);
        long playersRetainedMonth = analysisContainer.getValue(AnalysisKeys.PLAYERS_RETAINED_MONTH).orElse(0);

        if (playersNewMonth != 0) {
            double stuckPerc = MathUtils.averageDouble(playersRetainedMonth, playersNewMonth) * 100;
            if (stuckPerc >= 25) {
                notes.add("<p>" + Html.GREEN_THUMB.parse() + " " + FormatUtils.cutDecimals(stuckPerc)
                        + "% of new players have stuck around (" + playersRetainedMonth + "/" + playersNewMonth + ")</p>");
            } else {
                notes.add("<p>" + Html.YELLOW_FLAG.parse() + " " + FormatUtils.cutDecimals(stuckPerc)
                        + "% of new players have stuck around (" + playersRetainedMonth + "/" + playersNewMonth + ")</p>");
            }
        }
    }

    private void activePlayerPlaytimeChange() {
        PlayersMutator currentlyActive = PlayersMutator.copyOf(analysisContainer.getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)).filterActive(now, 1.75);
        long twoWeeksAgo = (now - (now - fourWeeksAgo)) / 2L;

        long totalFourToTwoWeeks = 0;
        long totalLastTwoWeeks = 0;
        for (PlayerContainer activePlayer : currentlyActive.all()) {
            totalFourToTwoWeeks += SessionsMutator.forContainer(activePlayer)
                    .filterSessionsBetween(fourWeeksAgo, twoWeeksAgo).toActivePlaytime();
            totalLastTwoWeeks += SessionsMutator.forContainer(activePlayer)
                    .filterSessionsBetween(twoWeeksAgo, now).toActivePlaytime();
        }
        int activeCount = currentlyActive.count();
        if (activeCount != 0) {
            long avgFourToTwoWeeks = MathUtils.averageLong(totalFourToTwoWeeks, activeCount);
            long avgLastTwoWeeks = MathUtils.averageLong(totalLastTwoWeeks, activeCount);
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
        Key<TPSMutator> tpsMonth = new Key<>(TPSMutator.class, "TPS_MONTH");
        TPSMutator tpsMutator = analysisContainer.getUnsafe(tpsMonth);
        long serverDownTime = tpsMutator.serverDownTime();
        double aboveThreshold = tpsMutator.percentageTPSAboveLowThreshold();
        long tpsSpikeMonth = analysisContainer.getValue(AnalysisKeys.TPS_SPIKE_MONTH).orElse(0);

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