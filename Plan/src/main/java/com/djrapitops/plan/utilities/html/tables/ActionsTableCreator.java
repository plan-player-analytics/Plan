/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.tables;

import main.java.com.djrapitops.plan.data.Action;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.util.List;

/**
 * Utility Class for creating Actions Table for inspect page.
 *
 * @author Rsl1122
 */
public class ActionsTableCreator {


    public ActionsTableCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String createTable(List<Action> actions) {
        StringBuilder html = new StringBuilder();
        if (actions.isEmpty()) {
            html.append(Html.TABLELINE_3.parse("No Actions", "-", "-"));
        } else {
            int i = 0;
            for (Action action : actions) {
                if (i >= 100) {
                    break;
                }

                long date = action.getDate();

                html.append(Html.TABLELINE_3_CUSTOMKEY_1.parse(
                        String.valueOf(date), FormatUtils.formatTimeStampYear(date),
                        action.getDoneAction().toString(),
                        action.getAdditionalInfo()
                ));

                i++;
            }
        }
        return html.toString();
    }
}