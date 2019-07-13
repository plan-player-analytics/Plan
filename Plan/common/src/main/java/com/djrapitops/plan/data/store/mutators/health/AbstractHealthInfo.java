/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.data.store.mutators.health;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.HealthInfoLang;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Icons;

import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHealthInfo {

    protected static final String SUB_NOTE = "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    protected final List<String> notes;
    protected final long now;
    protected final long monthAgo;

    protected double serverHealth;

    protected final Locale locale;
    protected final long activeMsThreshold;
    protected final int activeLoginThreshold;
    protected final Formatter<Long> timeAmountFormatter;
    protected final Formatter<Double> decimalFormatter;
    protected final Formatter<Double> percentageFormatter;

    public AbstractHealthInfo(
            long now, long monthAgo,
            Locale locale,
            long activeMsThreshold,
            int activeLoginThreshold,
            Formatter<Long> timeAmountFormatter,
            Formatter<Double> decimalFormatter,
            Formatter<Double> percentageFormatter
    ) {
        this.now = now;
        this.monthAgo = monthAgo;
        this.locale = locale;
        this.activeMsThreshold = activeMsThreshold;
        this.activeLoginThreshold = activeLoginThreshold;
        this.timeAmountFormatter = timeAmountFormatter;
        this.decimalFormatter = decimalFormatter;
        this.percentageFormatter = percentageFormatter;
        serverHealth = 100.0;

        this.notes = new ArrayList<>();
    }

    protected abstract void calculate();

    public double getServerHealth() {
        return serverHealth;
    }

    public String toHtml() {
        StringBuilder healthNoteBuilder = new StringBuilder();
        for (String healthNote : notes) {
            healthNoteBuilder.append(healthNote);
        }
        return healthNoteBuilder.toString();
    }

    protected void activityChangeNote(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        Map<String, Set<UUID>> activityNow = activityData.getOrDefault(now, new HashMap<>());
        Set<UUID> veryActiveNow = activityNow.getOrDefault("Very Active", new HashSet<>());
        Set<UUID> activeNow = activityNow.getOrDefault("Active", new HashSet<>());
        Set<UUID> regularNow = activityNow.getOrDefault("Regular", new HashSet<>());

        Map<String, Set<UUID>> activityFourWAgo = activityData.getOrDefault(monthAgo, new HashMap<>());
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
        double percRemain = activeFWAGNum != 0 ? remain * 1.0 / activeFWAGNum : 1.0;

        int newActive = getNewActive(veryActiveNow, activeNow, regularNow, veryActiveFWAG, activeFWAG, regularFWAG);

        int change = newActive - notRegularAnymore;

        StringBuilder remainNote = new StringBuilder();
        if (activeFWAGNum != 0) {
            remainNote.append(SUB_NOTE);
            if (percRemain > 0.5) {
                remainNote.append(Icons.GREEN_THUMB);
            } else if (percRemain > 0.2) {
                remainNote.append(Icons.YELLOW_FLAG);
            } else {
                remainNote.append(Icons.RED_WARN);
                serverHealth -= 2.5;
            }

            remainNote.append(locale.getString(HealthInfoLang.REGULAR_ACTIVITY_REMAIN,
                    percentageFormatter.apply(percRemain),
                    remain, activeFWAGNum
            ));
        }

        String sentenceStart = locale.getString(HealthInfoLang.REGULAR_CHANGE);
        if (change > 0) {
            addNote(Icons.GREEN_THUMB + sentenceStart + locale.getString(HealthInfoLang.REGULAR_CHANGE_INCREASE, change) + remainNote);
        } else if (change == 0) {
            addNote(Icons.GREEN_THUMB + sentenceStart + locale.getString(HealthInfoLang.REGULAR_CHANGE_ZERO, change) + remainNote);
        } else if (change > -20) {
            addNote(Icons.YELLOW_FLAG + sentenceStart + locale.getString(HealthInfoLang.REGULAR_CHANGE_DECREASE, change) + remainNote);
            serverHealth -= 5;
        } else {
            addNote(Icons.RED_WARN + sentenceStart + locale.getString(HealthInfoLang.REGULAR_CHANGE_DECREASE, change) + remainNote);
            serverHealth -= 10;
        }
    }

    protected void activePlayerPlaytimeChange(PlayersMutator playersMutator) {
        PlayersMutator currentlyActive = playersMutator.filterActive(now, activeMsThreshold, 1.75);
        long twoWeeksAgo = now - ((now - monthAgo) / 2L);

        long totalFourToTwoWeeks = 0;
        long totalLastTwoWeeks = 0;
        for (PlayerContainer activePlayer : currentlyActive.all()) {
            totalFourToTwoWeeks += SessionsMutator.forContainer(activePlayer)
                    .filterSessionsBetween(monthAgo, twoWeeksAgo).toActivePlaytime();
            totalLastTwoWeeks += SessionsMutator.forContainer(activePlayer)
                    .filterSessionsBetween(twoWeeksAgo, now).toActivePlaytime();
        }
        int activeCount = currentlyActive.count();
        if (activeCount != 0) {
            long avgFourToTwoWeeks = totalFourToTwoWeeks / (long) activeCount;
            long avgLastTwoWeeks = totalLastTwoWeeks / (long) activeCount;
            String avgLastTwoWeeksString = timeAmountFormatter.apply(avgLastTwoWeeks);
            String avgFourToTwoWeeksString = timeAmountFormatter.apply(avgFourToTwoWeeks);

            // Played more or equal amount than 2 weeks ago
            if (avgLastTwoWeeks >= avgFourToTwoWeeks) {
                addNote(Icons.GREEN_THUMB + locale.getString(HealthInfoLang.ACTIVE_PLAY_COMPARISON_INCREASE,
                        avgLastTwoWeeksString, avgFourToTwoWeeksString));
                // Played more than 2 hours less, than 2 weeks ago
            } else if (avgFourToTwoWeeks - avgLastTwoWeeks > TimeUnit.HOURS.toMillis(2L)) {
                addNote(Icons.RED_WARN + locale.getString(HealthInfoLang.ACTIVE_PLAY_COMPARISON_DECREASE,
                        avgLastTwoWeeksString, avgFourToTwoWeeksString));
                serverHealth -= 5;
                // Played less than two weeks ago
            } else {
                addNote(Icons.YELLOW_FLAG + locale.getString(HealthInfoLang.ACTIVE_PLAY_COMPARISON_DECREASE,
                        avgLastTwoWeeksString, avgFourToTwoWeeksString));
            }
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

    protected void addNote(String note) {
        notes.add("<p>" + note + "</p>");
    }
}
