/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.kingdoms;

import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.ServerProfile;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import org.kingdoms.constants.kingdom.OfflineKingdom;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates a Town Accordion for Towny tab.
 *
 * @author Rsl1122
 */
public class KingdomAccordionCreator {

    public static String createAccordion(Map<String, OfflineKingdom> kingdoms) {
        StringBuilder html = new StringBuilder("<div class=\"panel-group scrollbar\" id=\"towny_accordion\" role=\"tablist\" aria-multiselectable=\"true\">");

        ServerProfile serverProfile = Analysis.getServerProfile();
        List<PlayerProfile> players = serverProfile != null ? serverProfile.getPlayers() : new ArrayList<>();

        for (Map.Entry<String, OfflineKingdom> entry : kingdoms.entrySet()) {
            String kingdomName = entry.getKey();

            OfflineKingdom kingdom = entry.getValue();
            String kingName = kingdom.getKingName();
            String kingdomLore = kingdom.getKingdomLore();

            int might = kingdom.getMight();
            int resourcePoints = kingdom.getResourcepoints();

            List<UUID> members = kingdom.getMembersList();
            int residentsNum = members.size();

            List<PlayerProfile> memberProfiles = players.stream().filter(p -> members.contains(p.getUniqueId())).collect(Collectors.toList());

            List<Session> sessions = memberProfiles.stream()
                    .map(PlayerProfile::getSessions)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            long playerKills = ServerProfile.getPlayerKills(sessions).size();
            long mobKills = ServerProfile.getMobKillCount(sessions);
            long deaths = ServerProfile.getDeathCount(sessions);

            String separated = HtmlStructure.separateWithDots(("Members: " + residentsNum), kingName);

            String htmlID = "kingdom_" + kingdomName.replace(" ", "-");

            // Accordion panel header
            html.append("<div class=\"panel panel-col-amber\">")
                    .append("<div class=\"panel-heading\" role=\"tab\" id=\"heading_").append(htmlID).append("\">")
                    .append("<h4 class=\"panel-title\">")
                    .append("<a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#session_accordion\" ")
                    .append("href=\"#session_").append(htmlID).append("\" aria-expanded=\"false\" ")
                    .append("aria-controls=\"session_").append(htmlID).append("\">")
                    .append(kingdomName).append("<span class=\"pull-right\">").append(separated).append("</span>") // Title (header)
                    .append("</a></h4>") // Closes collapsed, panel title
                    .append("</div>"); // Closes panel heading

            // Content
            html.append("<div id=\"session_").append(htmlID).append("\" class=\"panel-collapse collapse\" role=\"tabpanel\"")
                    .append(" aria-labelledby=\"heading_").append(htmlID).append("\">")
                    .append("<div class=\"panel-body\"><div class=\"row clearfix\">")
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">"); // Left col-6
            // Lore
            if (kingdomLore != null) {
                html.append("<p>").append(kingdomLore).append("</p>");
            }
            html.append("<p><i class=\"col-amber fa fa-user\"></i> Mayor <span class=\"pull-right\">").append(kingName).append("</span></p>")
                    // Playtime
                    .append("<p><i class=\"col-amber fa fa-users\"></i> Members<span class=\"pull-right\"><b>").append(residentsNum).append("</b></span></p>")
                    .append("<p><i class=\"col-amber fa fa-bolt\"></i> Might<span class=\"pull-right\"><b>").append(might).append("</b></span></p>")
                    .append("<p><i class=\"col-amber fa fa-cubes\"></i> Resources<span class=\"pull-right\"><b>").append(resourcePoints).append("</b></span></p>")
                    .append("</div>") // Closes Left col-6
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Right col-6
                    // Player Kills
                    .append("<p><i class=\"col-red fa fa-crosshairs\"></i> Player Kills<span class=\"pull-right\"><b>").append(playerKills).append("</b></span></p>")
                    .append("<p><i class=\"col-green fa fa-crosshairs\"></i> Mob Kills<span class=\"pull-right\"><b>").append(mobKills).append("</b></span></p>")
                    // Deaths
                    .append("<p><i class=\"col-red fa fa-frown-o\"></i> Deaths<span class=\"pull-right\"><b>").append(deaths).append("</b></span></p>")
                    .append("</div>") // Right col-6
                    .append("</div>") // Closes row clearfix
                    .append("</div>") // Closes panel-body
                    .append("</div>") // Closes panel-collapse
                    .append("</div>"); // Closes panel
        }
        return html.append("</div>").toString();
    }

}