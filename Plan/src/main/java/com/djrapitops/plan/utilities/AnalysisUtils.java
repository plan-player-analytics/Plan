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
import main.java.com.djrapitops.plan.ui.tables.SortableCommandUseTableCreator;
import main.java.com.djrapitops.plan.ui.tables.SortablePlayersTableCreator;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    /**
     *
     * @param lastPlayed
     * @param playTime
     * @param loginTimes
     * @return
     */
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
        if (!registered.isEmpty()) {
           newPlayers = registered.stream()
                .filter((reg) -> (reg != null))
                .filter((reg) -> (reg > now - scale))
                .map((_item) -> 1).reduce(newPlayers, Integer::sum);
        }
        // Filters out register dates before scale
        
        return newPlayers;
    }
    
    static List<Long> transformSessionDataToLengths(Collection<SessionData> data) {
        List<Long> list = new ArrayList<>();
        data.stream().forEach((sData) -> {
            list.add(sData.getSessionEnd()-sData.getSessionStart());
        });
        return list;
    }
    
    static long average(Collection<Long> list) {
        if (list.isEmpty()) {
            return 0;
        }
        long total = 0;
        for (Long long1 : list) {
            total += long1;
        }
        return total / list.size();
    }
}
