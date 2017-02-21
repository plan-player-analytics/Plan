package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;

/**
 *
 * @author Rsl1122
 */
public class SortableCommandUseTableCreator {

    public static String createSortedCommandUseTable(HashMap<String, Integer> commandUse) {
        List<String[]> sorted = MapComparator.sortByValue(commandUse);
        String html = "";
        if (sorted.isEmpty()) {
            html = Html.ERROR_TABLE.parse();
            return html;
        }
        Collections.reverse(sorted);
        for (String[] values : sorted) {            
            html += Html.TABLELINE.parse(values[1], values[0]);
        }
        return html;
    }
}
