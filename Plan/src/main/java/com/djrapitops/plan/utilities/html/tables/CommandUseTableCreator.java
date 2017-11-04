package main.java.com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.utilities.comparators.MapComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rsl1122
 */
public class CommandUseTableCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private CommandUseTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param commandUse The commands and the amount of times casted
     * @return The created command use table
     */
    public static String createTable(Map<String, Integer> commandUse) {
        List<String[]> sorted = MapComparator.sortByValue(commandUse);

        StringBuilder html = new StringBuilder();
        if (sorted.isEmpty()) {
            html.append(Html.TABLELINE_2.parse("No Commands", ""));
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
                    Log.toLog("CommandUseTable - Cause: " + values[0] + " " + values[1], e);
                }

                i++;
            }
        }

        return html.toString();
    }
}
