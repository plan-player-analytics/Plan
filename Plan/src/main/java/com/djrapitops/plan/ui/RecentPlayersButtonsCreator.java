package main.java.com.djrapitops.plan.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;

/**
 *
 * @author Rsl1122
 */
public class RecentPlayersButtonsCreator {

    /**
     * Creates recent players buttons inside a p-tag.
     *
     * @param map Map of Playername, Logintime in ms
     * @param limit How many playes will be shown
     * @return html p-tag list of recent logins.
     */
    public static String createRecentLoginsButtons(HashMap<String, Long> map, int limit) {
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
}
