package main.java.com.djrapitops.plan.ui.tables;

import java.util.Collections;
import java.util.List;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionDataComparator;

/**
 *
 * @author Rsl1122
 */
public class SortabeSessionTableCreator {

    public static String createSortedSessionDataTable5(List<SessionData> sessionData) {
        String html = "<table class=\"sortable table\"><thead><tr>"
                + "<th>Session Started</th>"
                + "<th>Session Ended</th>"
                + "<th>Session Length</th>"
                + "</tr></thead>"
                + "<tbody>";
        if (sessionData.isEmpty()) {
            html += "<tr><td>No Session Data available</td><td></td><td></td></tr>";
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
                long length = end-start;
                html += "<tr>"
                        + "<td sorttable_customkey=\""+start+"\">" + FormatUtils.formatTimeStamp(start+"") + "</td>"
                        + "<td sorttable_customkey=\""+end+"\">" + FormatUtils.formatTimeStamp(end+"") + "</td>"
                        + "<td sorttable_customkey=\""+length+"\">" + FormatUtils.formatTimeAmount(length+"") + "</td>"
                        + "</tr>";
            }
        }
        html += "</tbody></table>";
        return html;
    }
}
