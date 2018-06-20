/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.container.Action;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.List;

/**
 * Utility Class for creating Actions Table for inspect page.
 *
 * @author Rsl1122
 */
@Deprecated
public class ActionsTable extends TableContainer {

    public ActionsTable(List<Action> actions) {
        super("Date", "Action", "Info");

        if (actions.isEmpty()) {
            addRow("No Actions");
        } else {
            addValues(actions);
        }
    }

    private void addValues(Iterable<Action> actions) {
        int i = 0;
        Formatter<DateHolder> formatter = Formatters.year();
        for (Action action : actions) {
            if (i > 50) {
                break;
            }
            addRow(
                    formatter.apply(action),
                    action.getDoneAction().toString(),
                    HtmlUtils.swapColorsToSpan(action.getAdditionalInfo())
            );
            i++;
        }
    }
}