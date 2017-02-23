package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collections;
import java.util.List;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionDataComparator;

/**
 *
 * @author Rsl1122
 */
public class SortabeSessionTableCreator {

    public static String createSortedSessionDataTable5(List<SessionData> sessionData) {
        String html = Html.TABLE_SESSIONS_START.parse();
        if (sessionData.isEmpty()) {
            html += Html.TABLELINE_3.parse(Html.SESSIONDATA_NONE.parse(), "", "");
        } else {
            Collections.sort(sessionData, new SessionDataComparator());
            Collections.reverse(sessionData);
            int i = 0;
            for (SessionData session : sessionData) {
                if (i > 4) {
                    break;
                }
                long start = session.getSessionStart();
                long end = session.getSessionEnd();
                long length = end - start;
                html += Html.TABLELINE_3_CUSTOMKEY.parse(
                        start+"", FormatUtils.formatTimeStamp(start+""), 
                        end+"", FormatUtils.formatTimeStamp(end+""), 
                        length+"", FormatUtils.formatTimeAmount(length+"")
                );
            }
        }
        html += Html.TABLE_END.parse();
        return html;
    }
}
