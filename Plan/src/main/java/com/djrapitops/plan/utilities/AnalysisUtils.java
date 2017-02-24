package main.java.com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.tables.SortableCommandUseTableCreator;
import main.java.com.djrapitops.plan.ui.tables.SortablePlayersTableCreator;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    public static boolean isActive(long lastPlayed, long playTime, int loginTimes) {
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

    static String createTableOutOfHashMap(HashMap<String, Integer> commandUse) {
        return SortableCommandUseTableCreator.createSortedCommandUseTable(commandUse);
    }
    
    static String createSortablePlayersTable(Collection<UserData> data) {
        return SortablePlayersTableCreator.createSortablePlayersTable(data);
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

    static int getNewPlayers(List<Long> registered, long scale, long now) {        
        int newPlayers = 0;
        // Filters out register dates before scale
        newPlayers = registered.stream()
                .filter((reg) -> (reg > now - scale))
                .map((_item) -> 1).reduce(newPlayers, Integer::sum);
        return newPlayers;
    }
}
