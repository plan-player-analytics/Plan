package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;

/**
 *
 * @author Rsl1122
 */
public class SortableCommandUseTableCreator {

    /**
     *
     * @param commandUse
     * @return
     */
    public static String createSortedCommandUseTable(Map<String, Integer> commandUse) {
        Benchmark.start("Create commanduse table");
        List<String[]> sorted = MapComparator.sortByValue(commandUse);
        StringBuilder html = new StringBuilder();
        if (sorted.isEmpty()) {
            html.append(Html.ERROR_TABLE_2.parse());
        } else {
            Collections.reverse(sorted);
            for (String[] values : sorted) {
                try {
                    html.append(Html.TABLELINE_2.parse(values[1], values[0]));
                } catch (IllegalArgumentException e) {
                    Log.toLog("SortableCommandUseTableCreator", e);
                    Log.toLog("Cause: " + values[0] + " " + values[1]);
                }
            }
        }
        Benchmark.stop("Create commanduse table");
        return html.toString();
    }
}
