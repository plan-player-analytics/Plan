/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.data.store.mutators.health;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.PlayersOnlineResolver;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;

/**
 * Server Health analysis mutator.
 *
 * @author Rsl1122
 */
public class HealthInformation extends AbstractHealthInfo {

    private final AnalysisContainer analysisContainer;

    public HealthInformation(AnalysisContainer analysisContainer) {
        super(
                analysisContainer.getUnsafe(AnalysisKeys.ANALYSIS_TIME),
                analysisContainer.getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO)
        );
        this.analysisContainer = analysisContainer;
        calculate();
    }

    public String toHtml() {
        StringBuilder healthNoteBuilder = new StringBuilder();
        for (String healthNote : notes) {
            healthNoteBuilder.append(healthNote);
        }
        return healthNoteBuilder.toString();
    }

    @Override
    protected void calculate() {
        activityChangeNote(analysisContainer.getUnsafe(AnalysisKeys.ACTIVITY_DATA));
        newPlayerNote();
        activePlayerPlaytimeChange(analysisContainer.getUnsafe(AnalysisKeys.PLAYERS_MUTATOR));
        lowPerformance();
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
            double retainPercentage = playersRetainedMonth / playersNewMonth;
            if (retainPercentage >= 0.25) {
                notes.add("<p>" + Html.GREEN_THUMB.parse() + " " + Formatters.percentage().apply(retainPercentage)
                        + " of new players have stuck around (" + playersRetainedMonth + "/" + playersNewMonth + ")</p>");
            } else {
                notes.add("<p>" + Html.YELLOW_FLAG.parse() + " " + Formatters.percentage().apply(retainPercentage)
                        + "% of new players have stuck around (" + playersRetainedMonth + "/" + playersNewMonth + ")</p>");
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

}