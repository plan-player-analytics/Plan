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

import java.util.*;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SessionsTableCreator {

    public static String[] createTables(JoinInfoPart joinInfoPart) {
        Map<Integer, UUID> uuidByID = new HashMap<>();
        for (Map.Entry<UUID, List<Session>> entry : joinInfoPart.getSessions().entrySet()) {
            List<Session> sessions = entry.getValue();
            for (Session session : sessions) {
                uuidByID.put(session.getSessionID(), entry.getKey());
            }
        }

        List<Session> allSessions = joinInfoPart.getAllSessions();
        if (allSessions.isEmpty()) {
            return new String[]{Html.TABLELINE_4.parse("<b>No Sessions</b>", "", "", ""),
                    Html.TABLELINE_2.parse("<b>No Sessions</b>", "")};
        }

        allSessions.sort(new SessionStartComparator());

        StringBuilder sessionTableBuilder = new StringBuilder();
        StringBuilder recentLoginsBuilder = new StringBuilder();

        int i = 0;
        Set<String> recentLoginsNames = new HashSet<>();
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

            String inspectUrl = HtmlUtils.getRelativeInspectUrl(name);
            sessionTableBuilder.append(Html.TABLELINE_4.parse(
                    inspectUrl

            ));

            if (recentLoginsNames.size() < 20 && !recentLoginsNames.contains(name)) {
                recentLoginsBuilder.append(Html.TABLELINE_2.parse(inspectUrl, start));
                recentLoginsNames.add(name);
            }

            i++;
        }
        return new String[]{sessionTableBuilder.toString(), recentLoginsBuilder.toString()};
    }
}