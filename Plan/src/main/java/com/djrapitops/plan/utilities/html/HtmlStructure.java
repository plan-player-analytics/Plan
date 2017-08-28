/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.html.tables.KillsTableCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for parsing layout components of the websites.
 *
 * @author Rsl1122
 */
public class HtmlStructure {

    public static String separateWithDots(String... elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            String element = elements[i];
            if (element.isEmpty()) {
                continue;
            }
            builder.append(" &#x2022; ");
            builder.append(element);
        }
        return builder.toString();
    }

    public static String createDotList(String... elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            if (element.isEmpty()) {
                continue;
            }
            builder.append("&#x2022; ");
            builder.append(element);
            builder.append("<br>");
        }
        return builder.toString();
    }

    public static String createServerOverviewColumn(Map<String, List<Session>> sessions) {
        StringBuilder builder = new StringBuilder("<div class=\"column\">");
        for (Map.Entry<String, List<Session>> entry : sessions.entrySet()) {
            String serverName = entry.getKey();
            List<Session> serverSessions = entry.getValue();

            // Header
            builder.append("<div class=\"box-header\"><h2><i class=\"fa fa-server\" aria-hidden=\"true\"></i> ").append(serverName).append("</h2> </div>");

            // White box
            builder.append("<div class=\"box\" style=\"margin-bottom: 5px;\">");

            // Content
            builder.append("<p>Sessions: ").append(serverSessions.size()).append("<br>");
            long playTime = AnalysisUtils.getTotalPlaytime(serverSessions);
            builder.append("Playtime: ").append(FormatUtils.formatTimeAmount(playTime)).append("<br>");
            builder.append("<br>");
            long longestSessionLength = AnalysisUtils.getLongestSessionLength(serverSessions);
            builder.append("Longest Session: " + FormatUtils.formatTimeAmount(longestSessionLength));

            // Content and box end
            builder.append("</p></div>");

        }
        // Column ends
        builder.append("</div>");
        return builder.toString();
    }

    public static String createSessionsTabContent(Map<String, List<Session>> sessions, List<Session> allSessions) {
        Map<Integer, String> serverNameIDRelationMap = new HashMap<>();

        for (Map.Entry<String, List<Session>> entry : sessions.entrySet()) {
            String serverName = entry.getKey();
            List<Session> serverSessions = entry.getValue();
            for (Session session : serverSessions) {
                serverNameIDRelationMap.put(session.getSessionID(), serverName);
            }
        }

        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Session session : allSessions) {
            if (i >= 50) {
                break;
            }

            String sessionStart = FormatUtils.formatTimeStampYear(session.getSessionStart());
            String sessionLength = FormatUtils.formatTimeAmount(session.getLength());
            String sessionEnd = FormatUtils.formatTimeStampYear(session.getSessionEnd());

            String dotSeparated = separateWithDots(sessionStart, sessionLength);

            // Session-column starts & header.
            builder.append("<div class=\"session column\">")
                    .append("<div class=\"session-header\">")
                    .append("<div class=\"session-col\" style=\"width: 200%;\">")
                    .append("<h3>").append(dotSeparated).append("</h3>")
                    .append("<p>Click to Expand</p>")
                    .append("</div>")
                    .append("</div>");

            String serverName = serverNameIDRelationMap.get(session.getSessionID());

            // Left side of Session box
            builder.append("<div class=\"session-content\">")
                    .append("<div class=\"row\">") //
                    .append("<div class=\"session-col\" style=\"padding: 0px;\">");

            // Left side header
            builder.append("<div class=\"box-header\" style=\"margin: 0px;\">")
                    .append("<h2><i class=\"fa fa-calendar\" aria-hidden=\"true\"></i> ")
                    .append(sessionStart)
                    .append("</h2>")
                    .append("</div>");

            // Left side content
            builder.append("<div class=\"box\" style=\"margin: 0px;\">")
                    .append("<p>Session Length: ").append(sessionLength).append("<br>")
                    .append("Session Ended: ").append(sessionEnd).append("<br>")
                    .append("Server: ").append(serverName).append("<br><br>")
                    .append("Mob Kills: ").append(session.getMobKills()).append("<br>")
                    .append("Deaths: ").append(session.getDeaths()).append("</p>");

            builder.append(KillsTableCreator.createTable(session.getPlayerKills()))
                    .append("</div>"); // Left Side content ends

            // Left side ends & Right side starts
            builder.append("</div>")
                    .append("<div class=\"session-col\">");

            // TODO WorldTimes Pie

            // Session-col, Row, Session-Content, Session-column ends.
            builder.append("</div>")
                    .append("</div>")
                    .append("</div>")
                    .append("</div>");

            i++;
        }
        return builder.toString();
    }
}