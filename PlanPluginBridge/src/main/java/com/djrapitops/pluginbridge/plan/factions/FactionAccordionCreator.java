/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.factions;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.ServerProfile;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.Analysis;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Creates Faction accordion for Factions tab.
 *
 * @author Rsl1122
 */
public class FactionAccordionCreator {

    public static String createAccordion(List<Faction> factions) {
        StringBuilder html = new StringBuilder("<div class=\"panel-group scrollbar\" id=\"faction_accordion\" role=\"tablist\" aria-multiselectable=\"true\">");

        ServerProfile serverProfile = Analysis.getServerProfile();
        List<PlayerProfile> players = serverProfile != null ? serverProfile.getPlayers() : new ArrayList<>();

        for (Faction faction : factions) {
            String factionName = faction.getName();
            long createdAtMillis = faction.getCreatedAtMillis();
            String created = FormatUtils.formatTimeStampYear(createdAtMillis);
            double power = faction.getPower();
            double maxPower = faction.getPowerMax();
            String powerString = FormatUtils.cutDecimals(power) + " / " + FormatUtils.cutDecimals(maxPower);
            MPlayer leader = faction.getLeader();
            String leaderName = leader != null ? leader.getName() : "No Leader";

            int landCount = faction.getLandCount();

            Set<UUID> members = new HashSet<>();
            List<MPlayer> mPlayers = faction.getMPlayers();
            int membersNum = mPlayers.size();
            for (MPlayer mPlayer : mPlayers) {
                if (mPlayer == null) {
                    continue;
                }
                UUID uuid = DataCache.getInstance().getUUIDof(mPlayer.getName());
                if (uuid != null) {
                    members.add(uuid);
                }
            }

            List<PlayerProfile> memberProfiles = players.stream().filter(p -> members.contains(p.getUuid())).collect(Collectors.toList());

            List<Session> sessions = memberProfiles.stream()
                    .map(PlayerProfile::getSessions)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            long playerKills = ServerProfile.getPlayerKills(sessions).size();
            long deaths = ServerProfile.getDeathCount(sessions);

            String separated = HtmlStructure.separateWithDots(("Power: " + powerString), leaderName);

            String htmlID = "faction_" + factionName + "_" + createdAtMillis;

            // Accordion panel header
            html.append("<div class=\"panel panel-col-deep-purple\">")
                    .append("<div class=\"panel-heading\" role=\"tab\" id=\"heading_").append(htmlID).append("\">")
                    .append("<h4 class=\"panel-title\">")
                    .append("<a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#faction_accordion\" ")
                    .append("href=\"#").append(htmlID).append("\" aria-expanded=\"false\" ")
                    .append("aria-controls=\"").append(htmlID).append("\">")
                    .append(factionName).append("<span class=\"pull-right\">").append(separated).append("</span>") // Title (header)
                    .append("</a></h4>") // Closes collapsed, panel title
                    .append("</div>"); // Closes panel heading

            // Content
            html.append("<div id=\"").append(htmlID).append("\" class=\"panel-collapse collapse\" role=\"tabpanel\"")
                    .append(" aria-labelledby=\"heading_").append(htmlID).append("\">")
                    .append("<div class=\"panel-body\"><div class=\"row clearfix\">")
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Left col-6
                    // Sessions
                    .append("<p><i class=\"col-deep-purple fa fa-calendar-o\"></i> Created <span class=\"pull-right\">").append(created).append("</span></p>")
                    // Playtime
                    .append("<p><i class=\"col-purple fa fa-bolt\"></i> Power<span class=\"pull-right\"><b>").append(powerString).append("</b></span></p>")
                    .append("<p><i class=\"col-purple fa fa-user\"></i> Leader<span class=\"pull-right\"><b>").append(leaderName).append("</b></span></p>")
                    .append("<p><i class=\"col-purple fa fa-users\"></i> Members<span class=\"pull-right\"><b>").append(membersNum).append("</b></span></p>")
                    .append("<p><i class=\"col-purple fa fa-map\"></i> Land Count<span class=\"pull-right\"><b>").append(landCount).append("</b></span></p>")
                    .append("</div>") // Closes Left col-6
                    .append("<div class=\"col-xs-12 col-sm-6 col-md-6 col-lg-6\">") // Right col-6
                    // Player Kills
                    .append("<p><i class=\"col-red fa fa-crosshairs\"></i> Player Kills<span class=\"pull-right\"><b>").append(playerKills).append("</b></span></p>")
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