/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.html;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.html.structure.SessionTabStructureCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.SessionsTableCreator;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.Serializable;
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
        for (String element : elements) {
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
        if (Verify.isEmpty(sessions)) {
            return "<div class=\"column\"><div class=\"box-header\"><h2><i class=\"fa fa-server\" aria-hidden=\"true\"></i> No Sessions</h2> </div>" +
                    "<div class=\"box\" style=\"margin-bottom: 5px;\"><p>No sessions to calculate server specific playtime.</p></div></div>";
        }
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
            builder.append("Longest Session: ").append(FormatUtils.formatTimeAmount(longestSessionLength));

            // Content and box end
            builder.append("</p></div>");

        }
        // Column ends
        builder.append("</div>");
        return builder.toString();
    }

    public static String[] createSessionsTabContentInspectPage(Map<String, List<Session>> sessions, List<Session> allSessions, UUID uuid) {
        Map<UUID, Map<String, List<Session>>> map = new HashMap<>();
        map.put(uuid, sessions);
        return SessionTabStructureCreator.creteStructure(map, allSessions, false);
    }

    private static String[] getSessionsAsTable(Map<String, List<Session>> sessions, List<Session> allSessions, UUID uuid) {
        Map<Integer, UUID> uuidByID = new HashMap<>();
        for (List<Session> sessionList : sessions.values()) {
            for (Session session : sessionList) {
                uuidByID.put(session.getSessionID(), uuid);
            }
        }

        return new String[]{Html.TABLE_SESSIONS.parse(SessionsTableCreator.createTable(uuidByID, allSessions)[0]), ""};
    }

    public static String createInspectPluginsTabContent(String serverName, List<PluginData> plugins, Map<String, Serializable> replaceMap) {
        if (plugins.isEmpty()) {
            String icon = Html.FONT_AWESOME_ICON.parse("server");
            // TODO Move plain text to Locale
            String headerText = Html.HEADER_2.parse(icon + " " + serverName) + Html.PARAGRAPH.parse("No Compatible Plugins");
            return Html.DIV_W_CLASS.parse("plugins-server",
                    Html.DIV_W_CLASS.parse("plugins-header",
                            Html.ROW.parse(Html.DIV_W_CLASS.parse("box-header", headerText))
                    )
            );
        }

        Map<String, List<String>> placeholders = getPlaceholdersInspect(plugins);

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"plugins-server\">")
                // Header
                .append("<div class=\"plugins-header\">")
                .append("<div class=\"row\">")
                .append("<div class=\"column\">")
                .append("<div class=\"box-header\">")
                .append("<h2><i style=\"padding: 8px;\" class=\"fa fa-chevron-down\" aria-hidden=\"true\"></i> ")
                .append(serverName)
                .append("</h2>")
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
                html.append(ph);
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
                html.append(ph);
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

            pluginPlaceholderList.add(source.getPlaceholder());

            placeholders.put(pluginName, pluginPlaceholderList);
        }
        return placeholders;
    }

    public static String createInspectPageTabContentCalculating() {
        return "<div class=\"row\">" +
                "<div class=\"column\">" +
                "<div class=\"box-header\">" +
                "<h2><i class=\"fa fa-cube\" aria-hidden=\"true\"></i> No Plugins</h2></div>" +
                "<div class=\"box plugin\">" +
                "<p><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Plugins tab is still being calculated, please refresh the page after a while (F5)</p>" +
                "</div></div>" +
                "</div>";
    }

    public static String createNetworkPageContent(Map<UUID, String> networkPageContents) {
        if (Verify.isEmpty(networkPageContents)) {
            return "";
        }
        int i = 0;
        StringBuilder b = new StringBuilder();
        List<String> values = new ArrayList<>(networkPageContents.values());
        Collections.sort(values);
        int size = values.size();
        int extra = size % 3;
        for (int j = 0; j < extra; j++) {
            values.add("<div class=\"column\"></div>");
        }
        for (String server : values) {
            if (i % 3 == 0) {
                b.append("<div class=\"row\">");
            }
            b.append(server);
            if ((i + 1) % 3 == 0 || i + 1 == size) {
                b.append("</div>");
            }
            i++;
        }
        return b.toString();
    }

    public static String createServerContainer(Plan plugin) {
        int maxPlayers = plugin.getVariable().getMaxPlayers();
        int online = plugin.getServer().getOnlinePlayers().size();
        Optional<Long> analysisRefreshDate = ((BukkitInformationManager) plugin.getInfoManager()).getAnalysisRefreshDate();
        String refresh = analysisRefreshDate.map(FormatUtils::formatTimeStamp).orElse("-");

        String serverName = plugin.getServerInfoManager().getServerName();
        String address = "../server/" + serverName;


        return "<div class=\"column\">" + "<div class=\"box-header\"><h2><i class=\"fa fa-server\" aria-hidden=\"true\"></i> " +
                serverName +
                "</h2></div>" +
                "<div class=\"box\"><p>" + online + "/" + maxPlayers +
                " Players Online</p></div>" +
                "<div class=\"box-footer\"><p>Last Refresh: " + refresh + "</p>" +
                "<a href=\"" + address + "\" class=\"button right\">Analysis</a>" +
                "</div></div>";
    }

    public static String parseOfflineServerContainer(String oldContent) {
        if (oldContent == null) {
            return "";
        }
        String[] split = oldContent.split("<p>", 2);
        String[] split2 = split[1].split("box-footer", 2);
        return split[0] + "<p>Offline</p></div><div class=\"box-footer" + split2[1];
    }
}