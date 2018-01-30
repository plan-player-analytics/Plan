/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plugin.utilities.Format;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * HTML utility class for creating a Session Accordion.
 *
 * @author Rsl1122
 */
public class ServerAccordionCreator {

    public static String[] createAccordion(PlayerProfile profile, Map<UUID, String> serverNames) {
        StringBuilder html = new StringBuilder("<div class=\"panel-group scrollbar\" id=\"session_accordion\" role=\"tablist\" aria-multiselectable=\"true\">");
        StringBuilder viewScript = new StringBuilder();

        Map<UUID, WorldTimes> worldTimesPerServer = profile.getWorldTimesPerServer();

        if (worldTimesPerServer.isEmpty()) {
            return new String[]{"<div class=\"body\">" +
                    "<p>No Sessions</p>" +
                    "</div>", ""};
        }

        int i = 0;
        for (Map.Entry<UUID, WorldTimes> entry : worldTimesPerServer.entrySet()) {
            UUID serverUUID = entry.getKey();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            WorldTimes worldTimes = entry.getValue();

            List<Session> sessions = profile.getSessions(serverUUID);
            long playtime = PlayerProfile.getPlaytime(sessions.stream());
            int sessionCount = sessions.size();
            long avgSession = MathUtils.averageLong(playtime, sessionCount);
            long sessionMedian = PlayerProfile.getSessionMedian(sessions.stream());
            long longestSession = PlayerProfile.getLongestSession(sessions.stream());

            long mobKills = PlayerProfile.getMobKillCount(sessions.stream());
            long playerKills = PlayerProfile.getPlayerKills(sessions.stream()).count();
            long deaths = PlayerProfile.getDeathCount(sessions.stream());

            String play = FormatUtils.formatTimeAmount(playtime);
            String avg = sessionCount != 0 ? FormatUtils.formatTimeAmount(avgSession) : "-";
            String median = sessionCount != 0 ? FormatUtils.formatTimeAmount(sessionMedian) : "-";
            String longest = sessionCount != 0 ? FormatUtils.formatTimeAmount(longestSession) : "-";

            String sanitizedServerName = new Format(serverName)
                    .removeSymbols()
                    .removeWhitespace().toString() + i;
            String htmlID = "server_" + sanitizedServerName;

            String worldId = "worldPieServer" + sanitizedServerName;

            WorldPie worldPie = new WorldPie(worldTimes);

            // Accordion panel header
            html.append("<div class=\"panel panel-col-").append(Theme.getValue(ThemeVal.PARSED_SERVER_ACCORDION)).append("\">")
                    .append("<div class=\"panel-heading\" role=\"tab\" id=\"heading_").append(htmlID).append("\">")
                    .append("<h4 class=\"panel-title\">")
                    .append("<a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#session_accordion\" ")
                    .append("href=\"#session_").append(htmlID).append("\" aria-expanded=\"false\" ")
                    .append("aria-controls=\"session_").append(htmlID).append("\">")
                    .append(serverName).append("<span class=\"pull-right\">").append(play).append("</span>") // Title (header)
                    .append("</a></h4>") // Closes collapsed, panel title
                    .append("</div>"); // Closes panel heading

            // Content
            html.append("<div id=\"session_").append(htmlID).append("\" class=\"panel-collapse collapse\" role=\"tabpanel\"")
                    .append(" aria-labelledby=\"heading_").append(htmlID).append("\">")
                    .append("<div class=\"panel-body\"><div class=\"row clearfix\">")
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Left col-6
                    // Sessions
                    .append("<p><i class=\"col-teal fa fa-calendar-check-o\"></i> Sessions <span class=\"pull-right\"><b>").append(sessionCount).append("</b></span></p>")
                    // Playtime
                    .append("<p><i class=\"col-green fa fa-clock-o\"></i> Server Playtime<span class=\"pull-right\"><b>").append(play).append("</b></span></p>")
                    .append("<p><i class=\"col-teal fa fa-clock-o\"></i> Longest Session<span class=\"pull-right\"><b>").append(longest).append("</b></span></p>")
                    .append("<p><i class=\"col-teal fa fa-clock-o\"></i> Session Median<span class=\"pull-right\"><b>").append(median).append("</b></span></p>")
                    .append("<br>")
                    // Player Kills
                    .append("<p><i class=\"col-red fa fa-crosshairs\"></i> Player Kills<span class=\"pull-right\"><b>").append(playerKills).append("</b></span></p>")
                    // Mob Kills
                    .append("<p><i class=\"col-green fa fa-crosshairs\"></i> Mob Kills<span class=\"pull-right\"><b>").append(mobKills).append("</b></span></p>")
                    // Deaths
                    .append("<p><i class=\"col-red fa fa-frown-o\"></i> Deaths<span class=\"pull-right\"><b>").append(deaths).append("</b></span></p>")
                    .append("</div>") // Closes Left col-6
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Right col-6
                    .append("<div id=\"").append(worldId).append("\" class=\"dashboard-donut-chart\"></div>")
                    // World Pie data script
                    .append("<script>")
                    .append("var ").append(worldId).append("series = {name:'World Playtime',colorByPoint:true,data:").append(worldPie.toHighChartsSeries()).append("};")
                    .append("var ").append(worldId).append("gmseries = ").append(worldPie.toHighChartsDrilldown()).append(";")
                    .append("</script>")
                    .append("</div>") // Right col-6
                    .append("</div>") // Closes row clearfix
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

}