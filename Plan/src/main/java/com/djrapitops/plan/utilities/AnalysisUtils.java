package com.djrapitops.plan.utilities;

import com.djrapitops.plan.ui.graphs.GMTimesPieChartCreator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.graphs.ActivityPieChartCreator;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.tables.SortedTableCreator;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    /**
     * Creates a GMTimesPieChart image HTML.
     *
     * @param gmTimes HashMap of gamemodes and time in ms how long has been
     * played in them.
     * @return Html img tag with url.
     */
    public static String createGMPieChart(HashMap<GameMode, Long> gmTimes) {
        String url = GMTimesPieChartCreator.createChart(gmTimes);
        return Html.IMG.parse(url);
    }

    /**
     * Creates a GMTimesPieChart image HTML.
     *
     * @param gmTimes HashMap of gamemodes and time in ms how long has been
     * played in them.
     * @param total Total time played in all gamemodes
     * @return Html img tag with url.
     */
    public static String createGMPieChart(HashMap<GameMode, Long> gmTimes, long total) {
        String url = GMTimesPieChartCreator.createChart(gmTimes, total);
        return Html.IMG.parse(url);
    }

    static String createPlayerActivityGraph(List<SessionData> sessionData, long scale) {
        String url = PlayerActivityGraphCreator.createChart(sessionData, scale);
        return Html.IMG.parse(url);
    }

    static boolean isActive(long lastPlayed, long playTime, int loginTimes) {
        int timeToActive = Settings.ANALYSIS_MINUTES_FOR_ACTIVE.getNumber();
        if (timeToActive < 0) {
            timeToActive = 0;
        }
        long twoWeeks = 1209600000;
        if (new Date().getTime() - lastPlayed < twoWeeks) {
            if (loginTimes > 3) {
                if (playTime > 60 * timeToActive) {
                    return true;
                }
            }
        }
        return false;
    }

    static String createActivityPieChart(int totalBanned, int active, int inactive, int joinleaver) {
        String url = ActivityPieChartCreator.createChart(totalBanned, active, inactive, joinleaver);
        return Html.IMG.parse(url);
    }

    static String createTableOutOfHashMap(HashMap<String, Integer> commandUse) {
        return SortedTableCreator.createTableOutOfHashMap(commandUse);
    }

    static String createTableOutOfHashMapLong(HashMap<String, Long> players) {
        return SortedTableCreator.createTableOutOfHashMapLong(players);
    }

    static String createTableOutOfHashMap(HashMap<String, Integer> map, int limit) {
        return SortedTableCreator.createTableOutOfHashMap(map, limit);
    }

    static String createActivePlayersTable(HashMap<String, Long> map, int limit) {
        return SortedTableCreator.createActivePlayersTable(map, limit);
    }

    static String createListStringOutOfHashMapLong(HashMap<String, Long> map, int limit) {
        List<String[]> sorted = MapComparator.sortByValueLong(map);
        String html = "<p>";
        if (sorted.isEmpty()) {
            html = Html.ERROR_LIST.parse();
            return html;
        }
        Collections.reverse(sorted);
        int i = 1;
        for (String[] values : sorted) {
            if (i >= limit) {
                break;
            }
            html += values[1] + " ";
            i++;
        }
        html += "</p>";
        return html;
    }

    static String[] analyzeSessionData(List<SessionData> sessionData, List<Long> registered, long scale, long now) {
        String[] returnA = new String[2];
        List<SessionData> inScale = new ArrayList<>();
        sessionData.stream()
                .filter((s) -> (s.getSessionStart() > now - scale))
                .forEach((s) -> {
                    inScale.add(s);
                });
        returnA[0] = createPlayerActivityGraph(inScale, scale);

        int newPlayers = 0;
        // Filters out register dates before scale
        newPlayers = registered.stream()
                .filter((reg) -> (reg > now - scale))
                .map((_item) -> 1).reduce(newPlayers, Integer::sum);
        returnA[1] = "" + newPlayers;
        return returnA;
    }
}
