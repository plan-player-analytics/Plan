package com.djrapitops.plan.data.store.mutators.health;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;

public abstract class AbstractHealthInfo {

    protected final String subNote = "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    protected final List<String> notes;
    protected final long now;
    protected final long monthAgo;

    protected double serverHealth;

    public AbstractHealthInfo(long now, long monthAgo) {
        this.now = now;
        this.monthAgo = monthAgo;
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
        double percRemain = remain * 100.0 / activeFWAGNum;

        int newActive = getNewActive(veryActiveNow, activeNow, regularNow, veryActiveFWAG, activeFWAG, regularFWAG);

        int change = newActive - notRegularAnymore;

        String remainNote = "";
        if (activeFWAGNum != 0) {
            remainNote = "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            if (percRemain > 50) {
                remainNote += Icons.GREEN_THUMB;
            } else if (percRemain > 20) {
                remainNote += Icons.YELLOW_FLAG;
            } else {
                remainNote += Icons.RED_WARN;
                serverHealth -= 2.5;
            }

            remainNote += " " + FormatUtils.cutDecimals(percRemain) + "% of regular players have remained active ("
                    + remain + "/" + activeFWAGNum + ")";
        }
        if (change > 0) {
            addNote(Icons.GREEN_THUMB + " Number of regular players has increased (+" + change + ")" + remainNote);
        } else if (change == 0) {
            addNote(Icons.GREEN_THUMB + " Number of regular players has stayed the same (+" + change + ")" + remainNote);
        } else if (change > -20) {
            addNote(Icons.YELLOW_FLAG + " Number of regular players has decreased (" + change + ")" + remainNote);
            serverHealth -= 5;
        } else {
            addNote(Icons.RED_WARN + " Number of regular players has decreased (" + change + ")" + remainNote);
            serverHealth -= 10;
        }
    }

    protected void activePlayerPlaytimeChange(PlayersMutator playersMutator) {
        PlayersMutator currentlyActive = playersMutator.filterActive(now, 1.75);
        long twoWeeksAgo = (now - (now - monthAgo)) / 2L;

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
            String avgLastTwoWeeksString = Formatters.timeAmount().apply(avgLastTwoWeeks);
            String avgFourToTwoWeeksString = Formatters.timeAmount().apply(avgFourToTwoWeeks);
            if (avgFourToTwoWeeks >= avgLastTwoWeeks) {
                addNote(Icons.GREEN_THUMB + " Active players seem to have things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)");
            } else if (avgFourToTwoWeeks - avgLastTwoWeeks > TimeAmount.HOUR.ms() * 2L) {
                addNote(Icons.RED_WARN + " Active players might be running out of things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)");
                serverHealth -= 5;
            } else {
                addNote(Icons.YELLOW_FLAG + " Active players might be running out of things to do (Played "
                        + avgLastTwoWeeksString + " vs " + avgFourToTwoWeeksString
                        + ", last two weeks vs weeks 2-4)");
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
