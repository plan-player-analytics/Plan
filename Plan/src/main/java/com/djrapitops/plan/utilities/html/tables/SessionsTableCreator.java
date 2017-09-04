/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.tables;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.analysis.JoinInfoPart;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SessionsTableCreator {

    public static String createTable(JoinInfoPart joinInfoPart) {
        Map<Integer, UUID> uuidByID = new HashMap<>();
        for (Map.Entry<UUID, List<Session>> entry : joinInfoPart.getSessions().entrySet()) {
            List<Session> sessions = entry.getValue();
            for (Session session : sessions) {
                uuidByID.put(session.getSessionID(), entry.getKey());
            }
        }

        List<Session> allSessions = joinInfoPart.getAllSessions();
        if (allSessions.isEmpty()) {
            return Html.TABLELINE_4.parse("<b>No Sessions</b>", "", "", "");
        }

        allSessions.sort(new SessionStartComparator());

        StringBuilder html = new StringBuilder();

        int i = 0;
        for (Session session : allSessions) {
            if (i >= 50) {
                break;
            }

            UUID uuid = uuidByID.get(session.getSessionID());
            // TODO Name cache
            String name = "TODO";
            String start = FormatUtils.formatTimeStamp(session.getSessionStart());
            String length = session.getSessionEnd() != -1 ? FormatUtils.formatTimeAmount(session.getLength()) : "Online";
//            getLongestWorldPlayed()

            html.append(Html.TABLELINE_4.parse(
                    HtmlUtils.getRelativeInspectUrl(name)

                    ));

            i++;
        }


        return html.toString();
    }
}