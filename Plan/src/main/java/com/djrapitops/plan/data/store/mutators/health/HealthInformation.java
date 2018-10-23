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
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.HealthInfoLang;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Server Health analysis mutator.
 *
 * @author Rsl1122
 */
public class HealthInformation extends AbstractHealthInfo {

    private final AnalysisContainer analysisContainer;

    private final int lowTPSThreshold;

    public HealthInformation(
            AnalysisContainer analysisContainer,
            Locale locale,
            int lowTPSThreshold,
            int activeMinuteThreshold,
            int activeLoginThreshold,
            Formatter<Long> timeAmountFormatter,
            Formatter<Double> decimalFormatter,
            Formatter<Double> percentageFormatter
    ) {
        super(
                analysisContainer.getUnsafe(AnalysisKeys.ANALYSIS_TIME),
                analysisContainer.getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO),
                locale,
                activeMinuteThreshold, activeLoginThreshold,
                timeAmountFormatter, decimalFormatter, percentageFormatter
        );
        this.analysisContainer = analysisContainer;
        this.lowTPSThreshold = lowTPSThreshold;
        calculate();
    }

    @Override
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
                .map(onlineResolver::getOnlineOn)
                .filter(Optional::isPresent)
                .mapToInt(Optional::get)
                .average().orElse(0);
        if (avgOnlineOnRegister >= 1) {
            addNote(Icons.GREEN_THUMB + locale.getString(HealthInfoLang.NEW_PLAYER_JOIN_PLAYERS_GOOD,
                    decimalFormatter.apply(avgOnlineOnRegister)));
        } else {
            addNote(Icons.YELLOW_FLAG + locale.getString(HealthInfoLang.NEW_PLAYER_JOIN_PLAYERS_BAD,
                    decimalFormatter.apply(avgOnlineOnRegister)));
            serverHealth -= 5;
        }

        long playersNewMonth = analysisContainer.getValue(AnalysisKeys.PLAYERS_NEW_MONTH).orElse(0);
        long playersRetainedMonth = analysisContainer.getValue(AnalysisKeys.PLAYERS_RETAINED_MONTH).orElse(0);

        if (playersNewMonth != 0) {
            double retainPercentage = playersRetainedMonth * 1.0 / playersNewMonth;
            String stickinessSentence = locale.getString(HealthInfoLang.NEW_PLAYER_STICKINESS,
                    percentageFormatter.apply(retainPercentage), playersRetainedMonth, playersNewMonth);
            if (retainPercentage >= 0.25) {
                addNote(Icons.GREEN_THUMB + stickinessSentence);
            } else {
                addNote(Icons.YELLOW_FLAG + stickinessSentence);
            }
        }
    }

    private void lowPerformance() {
        Key<TPSMutator> tpsMonth = new Key<>(TPSMutator.class, "TPS_MONTH");
        TPSMutator tpsMutator = analysisContainer.getUnsafe(tpsMonth);
        long serverDownTime = tpsMutator.serverDownTime();

        double aboveThreshold = tpsMutator.percentageTPSAboveThreshold(lowTPSThreshold);
        long tpsSpikeMonth = analysisContainer.getValue(AnalysisKeys.TPS_SPIKE_MONTH).orElse(0);

        StringBuilder avgLowThresholdString = new StringBuilder(subNote);
        if (aboveThreshold >= 0.96) {
            avgLowThresholdString.append(Icons.GREEN_THUMB);
        } else if (aboveThreshold >= 0.9) {
            avgLowThresholdString.append(Icons.YELLOW_FLAG);
            serverHealth *= 0.9;
        } else {
            avgLowThresholdString.append(Icons.RED_WARN);
            serverHealth *= 0.6;
        }
        avgLowThresholdString.append(locale.getString(HealthInfoLang.TPS_ABOVE_LOW_THERSHOLD, percentageFormatter.apply(aboveThreshold)));

        String tpsDipSentence = locale.getString(HealthInfoLang.TPS_LOW_DIPS, lowTPSThreshold, tpsSpikeMonth);
        if (tpsSpikeMonth <= 5) {
            addNote(Icons.GREEN_THUMB + tpsDipSentence + avgLowThresholdString);
        } else if (tpsSpikeMonth <= 25) {
            addNote(Icons.YELLOW_FLAG + tpsDipSentence + avgLowThresholdString);
            serverHealth *= 0.95;
        } else {
            addNote(Icons.RED_WARN + tpsDipSentence + avgLowThresholdString);
            serverHealth *= 0.8;
        }

        String downtimeSentence = locale.getString(HealthInfoLang.DOWNTIME, timeAmountFormatter.apply(serverDownTime));
        if (serverDownTime <= TimeUnit.DAYS.toMillis(1L)) {
            addNote(Icons.GREEN_THUMB + downtimeSentence);
        } else {
            long weekMs = TimeAmount.WEEK.toMillis(1L);
            if (serverDownTime <= weekMs) {
                addNote(Icons.YELLOW_FLAG + downtimeSentence);
                serverHealth *= (weekMs - serverDownTime) * 1.0 / weekMs;
            } else {
                addNote(Icons.RED_WARN + downtimeSentence);
                long monthMs = TimeAmount.MONTH.toMillis(1L);
                serverHealth *= (monthMs - serverDownTime) * 1.0 / monthMs;
            }
        }
    }

}