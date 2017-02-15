package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;

/**
 *
 * @author Rsl1122
 */
public class SortedTableCreator {
    
    public static String createCommandUseTable(HashMap<String, Integer> commandUse) {
        return "Not Implemented yet";
    }

    public static String createTableOutOfHashMap(HashMap<String, Integer> commandUse) {
        return createTableOutOfHashMap(commandUse, 50);
    }

    public static String createTableOutOfHashMap(HashMap<String, Integer> map, int limit) {
        List<String[]> sorted = MapComparator.sortByValue(map);
        String html = Html.TABLE_START.parse();
        if (sorted.isEmpty()) {
            html = Html.ERROR_TABLE.parse();
            return html;
        }
        Collections.reverse(sorted);
        int i = 1;
        for (String[] values : sorted) {
            if (i >= limit) {
                break;
            }
            html += Html.TABLELINE.parse(values[1], values[0]);
            i++;
        }
        html += Html.TABLE_END.parse();
        return html;
    }

    public static String createActivePlayersTable(HashMap<String, Long> map, int limit) {
        List<String[]> sorted = MapComparator.sortByValueLong(map);
        String html = Html.TABLE_START.parse();
        if (sorted.isEmpty()) {
            html = Html.ERROR_TABLE.parse() + Html.TABLE_END.parse();
            return html;
        }
        Collections.reverse(sorted);
        int i = 1;
        for (String[] values : sorted) {
            if (i >= limit) {
                break;
            }
            html += Html.TABLELINE.parse(values[1].replaceAll(Html.BUTTON_CLASS.parse(), Html.LINK_CLASS.parse()), FormatUtils.formatTimeAmount(values[0]));
            i++;
        }
        html += Html.TABLE_END.parse();
        return html;
    }

    public static String createTableOutOfHashMapLong(HashMap<String, Long> players) {
        return createActivePlayersTable(players, 20);
    }
}
