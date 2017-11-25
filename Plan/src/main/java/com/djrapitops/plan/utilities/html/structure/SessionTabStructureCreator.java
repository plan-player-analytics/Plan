/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import main.java.com.djrapitops.plan.utilities.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.KillsTableCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.SessionsTableCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class SessionTabStructureCreator {

    public static String[] creteStructure(Map<UUID, Map<String, List<Session>>> sessions, List<Session> allSessions, boolean appendName) {

        Map<Integer, UUID> uuidsByID = generateIDtoUUIDMap(sessions);

        if (Verify.isEmpty(allSessions)) {
            return new String[]{"<div class=\"body\">" +
                    "<p>No Sessions</p>" +
                    "</div>", ""};
        }

        Map<Integer, String> serverNameIDMap = generateIDtoServerNameMap(sessions);

        StringBuilder html = new StringBuilder("<div class=\"panel-group scrollbar\" id=\"session_accordion\" role=\"tablist\" aria-multiselectable=\"true\">");
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
            String sessionEnd = session.getSessionEnd() == -1 ? "Online" : FormatUtils.formatTimeStampYear(session.getSessionEnd());

            int playerKillCount = session.getPlayerKills().size();

            String name = Plan.getInstance().getDataCache().getName(uuid);
            String link = Plan.getPlanAPI().getPlayerInspectPageLink(name);
            String dotSeparated = appendName ?
                    HtmlStructure.separateWithDots(name, sessionStart) :
                    HtmlStructure.separateWithDots(serverName, sessionStart);

            String htmlID = "" + session.getSessionStart() + sessionID + i;

            String worldId = "worldPie" + session.getSessionStart() + i;
            WorldTimes worldTimes = session.getWorldTimes();
            AnalysisUtils.addMissingWorlds(worldTimes);

            String[] worldData = WorldPieCreator.createSeriesData(worldTimes);

            String killTable = KillsTableCreator.createTable(session.getPlayerKills());

            // Accordion panel header
            html.append("<div title=\"Session ID: ").append(sessionID).append("\"class=\"panel panel-col-teal\">")
                    .append("<div class=\"panel-heading\" role=\"tab\" id=\"heading_").append(htmlID).append("\">")
                    .append("<h4 class=\"panel-title\">")
                    .append("<a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#session_accordion\" ")
                    .append("href=\"#session_").append(htmlID).append("\" aria-expanded=\"false\" ")
                    .append("aria-controls=\"session_").append(htmlID).append("\">")
                    .append(dotSeparated).append("<span class=\"pull-right\">").append(sessionLength).append("</span>") // Title (header)
                    .append("</a></h4>") // Closes collapsed, panel title
                    .append("</div>"); // Closes panel heading

            // Content
            html.append("<div id=\"session_").append(htmlID).append("\" class=\"panel-collapse collapse\" role=\"tabpanel\"")
                    .append(" aria-labelledby=\"heading_").append(htmlID).append("\">")
                    .append("<div class=\"panel-body\"><div class=\"row clearfix\">")
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Left col-6
                    //
                    .append("<div class=\"font-bold m-b--35\"><i class=\"col-teal fa fa-clock-o\"></i> ")
                    .append(sessionStart).append(" -> ").append(sessionEnd).append(" </div>")
                    //
                    .append("<ul class=\"dashboard-stat-list\">")
                    // End
                    .append("<li><i class=\"col-teal fa fa-clock-o\"></i> Session Ended<span class=\"pull-right\"><b>").append(sessionEnd).append("</b></span></li>")
                    // Length
                    .append("<li><i class=\"col-teal fa fa-clock-o\"></i> Session Length<span class=\"pull-right\"><b>").append(sessionLength).append("</b></span></li>")
                    // Server
                    .append("<li><i class=\"col-light-green fa fa-server\"></i> Server<span class=\"pull-right\"><b>").append(serverName).append("</b></span></li>")
                    .append("<li></li>")
                    // Player Kills
                    .append("<li><i class=\"col-red fa fa-crosshairs\"></i> Player Kills<span class=\"pull-right\"><b>").append(playerKillCount).append("</b></span></li>")
                    // Mob Kills
                    .append("<li><i class=\"col-green fa fa-crosshairs\"></i> Mob Kills<span class=\"pull-right\"><b>").append(session.getMobKills()).append("</b></span></li>")
                    // Deaths
                    .append("<li><i class=\"col-red fa fa-frown-o\"></i> Deaths<span class=\"pull-right\"><b>").append(session.getDeaths()).append("</b></span></li>")
                    .append("</ul></div>") // Closes stat-list, Left col-6
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Right col-6
                    .append("<div id=\"").append(worldId).append("\" class=\"dashboard-donut-chart\"></div>")
                    // World Pie data script
                    .append("<script>")
                    .append("var ").append(worldId).append("series = {name:'World Playtime',colors: worldPieColors,colorByPoint:true,data:").append(worldData[0]).append("};")
                    .append("var ").append(worldId).append("gmseries = ").append(worldData[1]).append(";")
                    .append("</script>")
                    .append("</div>") // Right col-6
                    .append("</div>") // Closes row clearfix
                    .append("<div class=\"row clearfix\">")
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Left2 col-6
                    .append(killTable)
                    .append("</div>"); // Closes Left2 col-6
            if (appendName) {
                html.append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Right2 col-6
                        .append("<a target=\"_blank\" href=\"").append(link).append("\"><button href=\"").append(link)
                        .append("\" type=\"button\" class=\"pull-right btn bg-blue waves-effect\"><i class=\"material-icons\">person</i><span>INSPECT PAGE</span></button></a>")
                        .append("</div>"); // Closes Right2 col-6
            }
            html.append("</div>") // Closes row clearfix
                    .append("</div>") // Closes panel-body
                    .append("</div>") // Closes panel-collapse
                    .append("</div>"); // Closes panel

            viewScript.append("worldPie(")
                    .append(worldId).append(", ")
                    .append(worldId).append("series, ")
                    .append(worldId).append("gmseries")
                    .append(");");
            i++;
        }
        return new String[]{html.append("</div>").toString(), viewScript.toString()};
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

    public static String[] creteStructure(Map<UUID, List<Session>> sessions, List<Session> allSessions) {
        if (Settings.DISPLAY_SESSIONS_AS_TABLE.isTrue()) {
            return new String[]{Html.TABLE_SESSIONS.parse("", "", "", SessionsTableCreator.createTable(sessions, allSessions)[0]), ""};
        }

        Map<UUID, Map<String, List<Session>>> map = new HashMap<>();

        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            Map<String, List<Session>> serverSpecificMap = new HashMap<>();
            serverSpecificMap.put("This server", entry.getValue());
            map.put(entry.getKey(), serverSpecificMap);
        }

        return creteStructure(map, allSessions, true);
    }
}