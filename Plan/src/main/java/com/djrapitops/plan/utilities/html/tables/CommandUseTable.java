package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.utilities.comparators.MapComparator;
import com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rsl1122
 */
public class CommandUseTable extends TableContainer {

    public CommandUseTable(Map<String, Integer> commandUse) {
        super("Command", "Times Used");

        if (commandUse.isEmpty()) {
            addRow("No Commands");
        } else {
            addValues(commandUse);
        }
    }

    private void addValues(Map<String, Integer> commandUse) {
        List<String[]> sorted = MapComparator.sortByValue(commandUse);
        Collections.reverse(sorted);

        int i = 0;
        for (String[] values : sorted) {
            if (i >= 500) {
                break;
            }
            String command = HtmlUtils.removeXSS(values[1]);
            addRow(command, values[0]);

            i++;
        }
    }
}
