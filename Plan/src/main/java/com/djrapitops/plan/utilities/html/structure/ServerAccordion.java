/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.data.PlayerProfile;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plugin.utilities.Format;

import java.util.*;

/**
 * HTML utility class for creating a Server Accordion.
 *
 * @author Rsl1122
 */
public class ServerAccordion extends AbstractAccordion {

    private final StringBuilder viewScript;

    private final Map<UUID, String> serverNames;
    private PerServerContainer perServer;

    public ServerAccordion(PlayerContainer container, Map<UUID, String> serverNames) {
        super("server_accordion");

        viewScript = new StringBuilder();

        this.serverNames = serverNames;
        Optional<PerServerContainer> perServerData = container.getValue(PlayerKeys.PER_SERVER);
        if (perServerData.isPresent()) {
            perServer = perServerData.get();
        } else {
            return;
        }

        addElements();
    }

    public String toViewScript() {
        return viewScript.toString();
    }

    private void addElements() {
        int i = 0;

        Formatter<Long> timeFormatter = Formatters.timeAmount();

        for (Map.Entry<UUID, DataContainer> entry : perServer.entrySet()) {
            UUID serverUUID = entry.getKey();
            DataContainer container = entry.getValue();
            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            WorldTimes worldTimes = container.getValue(PerServerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));

            List<Session> sessions = container.getValue(PerServerKeys.SESSIONS).orElse(new ArrayList<>());

            boolean banned = container.getValue(PerServerKeys.BANNED).orElse(false);
            boolean opeator = container.getValue(PerServerKeys.OPERATOR).orElse(false);
            long registered = container.getValue(PerServerKeys.REGISTERED).orElse(0L);

            long playtime = PlayerProfile.getPlaytime(sessions.stream());
            long afkTime = PlayerProfile.getAFKTime(sessions.stream());
            int sessionCount = sessions.size();
            long avgSession = MathUtils.averageLong(playtime, sessionCount);
            long sessionMedian = PlayerProfile.getSessionMedian(sessions.stream());
            long longestSession = PlayerProfile.getLongestSession(sessions.stream());

            long mobKills = PlayerProfile.getMobKillCount(sessions.stream());
            long playerKills = PlayerProfile.getPlayerKills(sessions.stream()).count();
            long deaths = PlayerProfile.getDeathCount(sessions.stream());

            String play = timeFormatter.apply(playtime);
            String afk = timeFormatter.apply(afkTime);
            String avg = timeFormatter.apply(avgSession);
            String median = timeFormatter.apply(sessionMedian);
            String longest = timeFormatter.apply(longestSession);

            String sanitizedServerName = new Format(serverName)
                    .removeSymbols()
                    .removeWhitespace().toString() + i;
            String htmlID = "server_" + sanitizedServerName;

            String worldId = "worldPieServer" + sanitizedServerName;

            WorldPie worldPie = new WorldPie(worldTimes);

            String title = serverName + "<span class=\"pull-right\">" + play + "</span>";

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold("blue", "superpowers", "Operator", opeator ? "Yes" : "No")
                    .addRowBold("red", "gavel", "Banned", banned ? "Yes" : "No")
                    .addRowBold("light-green", "user-plus", "Registered", Formatters.year().apply(() -> registered))
                    .addBreak()
                    .addRowBold("teal", "calendar-check-o", "Sessions", sessionCount)
                    .addRowBold("green", "clock-o", "Server Playtime", play)
                    .addRowBold("grey", "clock-o", "Time AFK", afk)
                    .addRowBold("teal", "clock-o", "Longest Session", longest)
                    .addRowBold("teal", "clock-o", "Session Median", median)
                    .addBreak()
                    .addRowBold("red", "crosshairs", "Player Kills", playerKills)
                    .addRowBold("green", "crosshairs", "Mob Kills", mobKills)
                    .addRowBold("red", "frown-o", "Deaths", deaths)
                    .toHtml();

            String rightSide = "<div id=\"" + worldId + "\" class=\"dashboard-donut-chart\"></div>" +
                    "<script>" +
                    "var " + worldId + "series = {name:'World Playtime',colorByPoint:true,data:" + worldPie.toHighChartsSeries() + "};" +
                    "var " + worldId + "gmseries = " + worldPie.toHighChartsDrilldown() + ";" +
                    "</script>";

            addElement(new AccordionElement(htmlID, title)
                    .setColor(Theme.getValue(ThemeVal.PARSED_SERVER_ACCORDION))
                    .setLeftSide(leftSide)
                    .setRightSide(rightSide));

            viewScript.append("worldPie(")
                    .append(worldId).append(", ")
                    .append(worldId).append("series, ")
                    .append(worldId).append("gmseries")
                    .append(");");

            i++;
        }
    }
}