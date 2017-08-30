/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.WorldPieCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.KillsTableCreator;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.FileNotFoundException;
import java.util.*;

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

    public static String createSessionsTabContent(Map<String, List<Session>> sessions, List<Session> allSessions) throws FileNotFoundException {
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

            String id = "worldPie" + session.getSessionStart();

            builder.append("<div id=\"").append(id).append("\" style=\"width: 100%; height: 400px;\"></div>");

            String[] worldData = WorldPieCreator.createSeriesData(session.getWorldTimes());

            builder.append("<script>")
                    .append("var ").append(id).append("series = {name:'World Playtime',colorByPoint:true,data:").append(worldData[0]).append("};")
                    .append("var ").append(id).append("gmseries = ").append(worldData[1]).append(";")
                    .append("$( document ).ready(function() {worldPie(")
                    .append(id).append(", ")
                    .append(id).append("series, ")
                    .append(id).append("gmseries")
                    .append(");})")
                    .append("</script>");

            // Session-col, Row, Session-Content, Session-column ends.
            builder.append("</div>")
                    .append("</div>")
                    .append("</div>")
                    .append("</div>");

            i++;
        }
        return builder.toString();
    }

    public static String createInspectPageTabContent(String serverName, List<PluginData> plugins, Map<String, String> replaceMap) {
        if (plugins.isEmpty()) {
            return "";
        }

        Map<String, List<String>> placeholders = getPlaceholdersInspect(plugins);

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"plugins-server\">")
                // Header
                .append("<div class=\"plugins-header\">")
                .append("<div class=\"row\">")
                .append("<div class=\"column\">")
                .append("<div class=\"box-header\">")
                .append("<h2><i class=\"fa fa-server\" aria-hidden=\"true\"></i> ")
                .append(serverName)
                .append("</h2><p>Click to Expand</p>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("</div>")
                // Content
                .append("<div class=\"plugins-content\">");
        List<String> pluginNames = new ArrayList<>(placeholders.keySet());
        Collections.sort(pluginNames);


        int pluginCount = pluginNames.size();
        int lastRowColumns = pluginCount % 3;

        int column = 0;
        for (String pluginName : pluginNames) {
            List<String> pluginPhs = placeholders.get(pluginName);

            if (column % 3 == 0) {
                html.append("<div class=\"row\">");
            }

            html.append("<div class=\"column\">")
                    .append("<div class=\"box-header\">")
                    .append("<h2><i class=\"fa fa-cube\" aria-hidden=\"true\"></i> ").append(pluginName).append("</h2></div>");
            html.append("<div class=\"box plugin\">");

            for (String ph : pluginPhs) {
                html.append(ph).append("<br>");
            }

            html.append("</div></div>"); // Closes column


            if ((column + 1) % 3 == 0) {
                html.append("</div>");
            }
            column++;
        }

        if (lastRowColumns != 0) {
            if (lastRowColumns == 1) {
                html.append("<div class=\"column\" style=\"width: 200%;\">");
            } else if (lastRowColumns == 2) {
                html.append("<div class=\"column\">");
            }
            html.append("</div>");
        }

        html.append("</div>") // Close content
                .append("</div>");
        return StrSubstitutor.replace(html.toString(), replaceMap);
    }

    public static String createAnalysisPluginsTabLayout(List<PluginData> plugins) {
        StringBuilder html = new StringBuilder();
        if (plugins.isEmpty()) {
            html.append("<div class=\"row\">")
                    .append("<div class=\"column\">")
                    .append("<div class=\"box-header\">")
                    .append("<h2><i class=\"fa fa-cube\" aria-hidden=\"true\"></i> No Plugins</h2></div>")
                    .append("<div class=\"box plugin\">")
                    .append("<p>No Supported Plugins were detected.</p>")
                    .append("</div></div>")
                    .append("</div>");
            return html.toString();
        }

        Map<String, List<String>> placeholders = getPlaceholdersAnalysis(plugins);
        List<String> pluginNames = new ArrayList<>(placeholders.keySet());
        Collections.sort(pluginNames);


        int pluginCount = pluginNames.size();
        int lastRowColumns = pluginCount % 3;

        int column = 0;
        for (String pluginName : pluginNames) {
            List<String> pluginPhs = placeholders.get(pluginName);

            if (column % 3 == 0) {
                html.append("<div class=\"row\">");
            }

            html.append("<div class=\"column\">")
                    .append("<div class=\"box-header\">")
                    .append("<h2><i class=\"fa fa-cube\" aria-hidden=\"true\"></i> ").append(pluginName).append("</h2></div>");
            html.append("<div class=\"box plugin\">");

            for (String ph : pluginPhs) {
                html.append(ph).append("<br>");
            }

            html.append("</div></div>"); // Closes column


            if ((column + 1) % 3 == 0) {
                html.append("</div>");
            }
            column++;
        }

        if (lastRowColumns != 0) {
            if (lastRowColumns == 1) {
                html.append("<div class=\"column\" style=\"width: 200%;\">");
            } else if (lastRowColumns == 2) {
                html.append("<div class=\"column\">");
            }
            html.append("<div class=\"box-header\" style=\"margin-top: 10px;\">")
                    .append("<h2>That's all..</h2>")
                    .append("</div>")
                    .append("<div class=\"box plugin\">")
                    .append("<p>Do you have more plugins? ._.</p>")
                    .append("</div>")
                    .append("</div>");
        }
        return html.toString();
    }

    private static Map<String, List<String>> getPlaceholdersAnalysis(List<PluginData> plugins) {
        Map<String, List<String>> placeholders = new HashMap<>();
        for (PluginData source : plugins) {
            List<AnalysisType> analysisTypes = source.getAnalysisTypes();
            if (analysisTypes.isEmpty()) {
                continue;
            }
            String pluginName = source.getSourcePlugin();
            List<String> pluginPlaceholderList = placeholders.getOrDefault(pluginName, new ArrayList<>());

            for (AnalysisType t : analysisTypes) {
                pluginPlaceholderList.add(source.getPlaceholder(t.getPlaceholderModifier()));
            }

            placeholders.put(pluginName, pluginPlaceholderList);
        }
        return placeholders;
    }

    private static Map<String, List<String>> getPlaceholdersInspect(List<PluginData> plugins) {
        Map<String, List<String>> placeholders = new HashMap<>();
        for (PluginData source : plugins) {
            if (source.analysisOnly()) {
                continue;
            }
            String pluginName = source.getSourcePlugin();
            List<String> pluginPlaceholderList = placeholders.getOrDefault(pluginName, new ArrayList<>());

            pluginPlaceholderList.add(source.getPlaceholder(""));

            placeholders.put(pluginName, pluginPlaceholderList);
        }
        return placeholders;
    }
}