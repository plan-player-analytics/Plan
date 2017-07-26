package main.java.com.djrapitops.plan.ui.html.tables;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rsl1122
 */
public class CommandUseTableCreator {

    /**
     * @param commandUse
     * @return
     */
    public static String createSortedCommandUseTable(Map<String, Integer> commandUse) {
        Benchmark.start("Create CommandUse table");
        List<String[]> sorted = MapComparator.sortByValue(commandUse);
        StringBuilder html = new StringBuilder();
        if (sorted.isEmpty()) {
            html.append(Html.ERROR_TABLE_2.parse());
        } else {
            Collections.reverse(sorted);
            int i = 0;
            for (String[] values : sorted) {
                if (i >= 500) {
                    break;
                }
                try {
                    html.append(Html.TABLELINE_2.parse(values[1], values[0]));
                } catch (IllegalArgumentException e) {
                    Log.toLog("SortableCommandUseTableCreator", e);
                    Log.toLog("Cause: " + values[0] + " " + values[1], Log.getErrorsFilename());
                }
                i++;
            }
        }
        Benchmark.stop("Create CommandUse table");
        return html.toString();
    }
}
