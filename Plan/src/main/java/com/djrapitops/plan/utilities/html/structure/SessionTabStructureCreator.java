/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.analysis.JoinInfoPart;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import main.java.com.djrapitops.plan.utilities.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.KillsTableCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.SessionsTableCreator;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SessionTabStructureCreator {

    public static String[] creteStructure(Map<UUID, Map<String, List<Session>>> sessions, List<Session> allSessions, boolean appendName) {

        Map<Integer, UUID> uuidsByID = generateIDtoUUIDMap(sessions);

        if (Settings.DISPLAY_SESSIONS_AS_TABLE.isTrue()) {
            return new String[]{Html.TABLE_SESSIONS.parse(SessionsTableCreator.createTable(uuidsByID, allSessions)[0]), ""};
        }

        if (Verify.isEmpty(allSessions)) {
            return new String[]{"<div class=\"session column\">" +
                    "<div class=\"session-header\">" +
                    "<div class=\"session-col\" style=\"width: 200%;\">" +
                    "<h3>No Sessions</h3>" +
                    "</div></div></div>", ""};
        }

        Map<Integer, String> serverNameIDMap = generateIDtoServerNameMap(sessions);

        StringBuilder html = new StringBuilder();
        StringBuilder viewScript = new StringBuilder();
        int i = 0;
        for (Session session : allSessions) {
            if (i >= 50) {
                break;
            }

            int sessionID = session.getSessionID();
            UUID uuid = uuidsByID.get(sessionID);
            String serverName = serverNameIDMap.get(sessionID);

            String sessionStart = FormatUtils.formatTimeStampYear(session.getSessionStart());
            String sessionLength = FormatUtils.formatTimeAmount(session.getLength());
            String sessionEnd = FormatUtils.formatTimeStampYear(session.getSessionEnd());

            String name = Plan.getInstance().getDataCache().getName(uuid);
            String link = Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(name), name);
            String dotSeparated = appendName ?
                    HtmlStructure.separateWithDots(link, sessionStart, sessionLength) :
                    HtmlStructure.separateWithDots(sessionStart, sessionLength);

            // Session-column starts & header.
            html.append("<div class=\"session column\">")
                    .append("<div title=\"Session ID: ").append(sessionID).append("\" class=\"session-header\">")
                    .append("<div class=\"session-col\" style=\"width: 200%;\">")
                    .append("<h3><i style=\"color:#777\" class=\"fa fa-chevron-down\" aria-hidden=\"true\"></i> ").append(dotSeparated).append("</h3>")
                    .append("</div>")
                    .append("</div>");


            // Left side of Session box
            html.append("<div class=\"session-content\">")
                    .append("<div class=\"row\">") //
                    .append("<div class=\"session-col\" style=\"padding: 0px;\">");

            // Left side header
            html.append("<div class=\"box-header\" style=\"margin: 0px;\">")
                    .append("<h2><i class=\"fa fa-calendar\" aria-hidden=\"true\"></i> ")
                    .append(sessionStart)
                    .append("</h2>")
                    .append("</div>");

            // Left side content
            html.append("<div class=\"box\" style=\"margin: 0px;\">")
                    .append("<p>Session Length: ").append(sessionLength).append("<br>")
                    .append("Session Ended: ").append(sessionEnd).append("<br>")
                    .append("Server: ").append(serverName).append("<br><br>")
                    .append("Mob Kills: ").append(session.getMobKills()).append("<br>")
                    .append("Deaths: ").append(session.getDeaths()).append("</p>");

            html.append(KillsTableCreator.createTable(session.getPlayerKills()))
                    .append("</div>"); // Left Side content ends

            // Left side ends & Right side starts
            html.append("</div>")
                    .append("<div class=\"session-col\">");

            String id = "worldPie" + session.getSessionStart() + i;

            html.append("<div id=\"").append(id).append("\" style=\"width: 100%; height: 400px;\"></div>");

            WorldTimes worldTimes = session.getWorldTimes();

            try {
                // Add 0 time for worlds not present.
                Set<String> nonZeroWorlds = worldTimes.getWorldTimes().keySet();
                for (String world : MiscUtils.getIPlan().getDB().getWorldTable().getWorlds()) {
                    if (nonZeroWorlds.contains(world)) {
                        continue;
                    }
                    worldTimes.setGMTimesForWorld(world, new GMTimes());
                }
            } catch (SQLException e) {
                Log.toLog("SessionTabStructureCreator", e);
            }

            String[] worldData = WorldPieCreator.createSeriesData(worldTimes);

            html.append("<script>")
                    .append("var ").append(id).append("series = {name:'World Playtime',colors: worldPieColors,colorByPoint:true,data:").append(worldData[0]).append("};")
                    .append("var ").append(id).append("gmseries = ").append(worldData[1]).append(";")
                    .append("</script>");

            viewScript.append("worldPie(")
                    .append(id).append(", ")
                    .append(id).append("series, ")
                    .append(id).append("gmseries")
                    .append(");");

            // Session-col, Row, Session-Content, Session-column ends.
            html.append("</div>")
                    .append("</div>")
                    .append("</div>")
                    .append("</div>");

            i++;
        }
        return new String[]{html.toString(), viewScript.toString()};
    }

    private static Map<Integer, String> generateIDtoServerNameMap(Map<UUID, Map<String, List<Session>>> sessions) {
        Map<Integer, String> serverNameIDRelationMap = new HashMap<>();
        for (Map<String, List<Session>> map : sessions.values()) {
            for (Map.Entry<String, List<Session>> entry : map.entrySet()) {
                String serverName = entry.getKey();
                List<Session> serverSessions = entry.getValue();
                for (Session session : serverSessions) {
                    serverNameIDRelationMap.put(session.getSessionID(), serverName);
                }
            }
        }
        return serverNameIDRelationMap;
    }

    private static Map<Integer, UUID> generateIDtoUUIDMap(Map<UUID, Map<String, List<Session>>> sessions) {
        Map<Integer, UUID> uuidsByID = new HashMap<>();
        for (Map.Entry<UUID, Map<String, List<Session>>> entry : sessions.entrySet()) {
            UUID uuid = entry.getKey();
            for (List<Session> sessionList : entry.getValue().values()) {
                for (Session session : sessionList) {
                    uuidsByID.put(session.getSessionID(), uuid);
                }
            }
        }
        return uuidsByID;
    }

    public static String[] creteStructure(JoinInfoPart joinInfoPart) {
        Map<UUID, Map<String, List<Session>>> map = new HashMap<>();
        Map<UUID, List<Session>> sessions = joinInfoPart.getSessions();
        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            Map<String, List<Session>> serverSpecificMap = new HashMap<>();
            serverSpecificMap.put("This server", entry.getValue());
            map.put(entry.getKey(), serverSpecificMap);
        }

        List<Session> allSessions = sessions.values().stream()
                .flatMap(Collection::stream)
                .sorted(new SessionStartComparator())
                .collect(Collectors.toList());

        return creteStructure(map, allSessions, true);
    }
}