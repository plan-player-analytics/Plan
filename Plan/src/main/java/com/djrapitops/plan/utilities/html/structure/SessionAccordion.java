package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;

import java.util.*;
import java.util.function.Supplier;

/**
 * Utility for creating Session accordion html and javascript from Session objects.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.container.Session for object
 */
public class SessionAccordion extends AbstractAccordion {

    private final boolean forPlayer;
    private final List<Session> sessions;
    private final Supplier<Map<UUID, String>> serverNamesSupplier;
    private final Supplier<Map<UUID, String>> playerNamesSupplier;

    private final StringBuilder viewScript;
    private final boolean appendWorldPercentage;
    private int maxSessions;

    // TODO
    private Theme theme;
    private Graphs graphs;
    private HtmlTables tables;
    private Formatter<DateHolder> yearFormatter;
    private Formatter<Long> timeAmountFormatter;

    private SessionAccordion(boolean forPlayer, List<Session> sessions,
                             Supplier<Map<UUID, String>> serverNamesSupplier,
                             Supplier<Map<UUID, String>> playerNamesSupplier) {
        super("session_accordion");

        this.forPlayer = forPlayer;
        this.sessions = sessions;
        this.serverNamesSupplier = serverNamesSupplier;
        this.playerNamesSupplier = playerNamesSupplier;
        viewScript = new StringBuilder();

        maxSessions = Settings.MAX_SESSIONS.getNumber();
        if (maxSessions <= 0) {
            maxSessions = 50;
        }
        appendWorldPercentage = Settings.APPEND_WORLD_PERC.isTrue();

        addElements();
    }

    public static SessionAccordion forServer(List<Session> sessions, Supplier<Map<UUID, String>> serverNamesSupplier,
                                             Supplier<Map<UUID, String>> playerNamesSupplier) {
        return new SessionAccordion(false, sessions, serverNamesSupplier, playerNamesSupplier);
    }

    public static SessionAccordion forPlayer(List<Session> sessions, Supplier<Map<UUID, String>> serverNamesSupplier) {
        return new SessionAccordion(true, sessions, serverNamesSupplier, HashMap::new);
    }

    public String toViewScript() {
        return viewScript.toString();
    }

    private void addElements() {
        if (forPlayer) {
            addElementsForPlayer();
        } else {
            addElementsForServer();
        }
        // Requires refactoring of Session object to contain information about player and server
    }

    private void addElementsForServer() {
        Map<UUID, String> serverNames = serverNamesSupplier.get();
        Map<UUID, String> playerNames = playerNamesSupplier.get();
        sessions.sort(new DateHolderRecentComparator());

        int i = 0;
        for (Session session : sessions) {
            if (i >= maxSessions) {
                break;
            }

            String serverName = serverNames.getOrDefault(session.getValue(SessionKeys.SERVER_UUID).orElse(null), "Unknown");
            String playerName = playerNames.getOrDefault(session.getValue(SessionKeys.UUID).orElse(null), "Unknown");
            String sessionStart = yearFormatter.apply(session);

            WorldTimes worldTimes = session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));
            WorldPie worldPie = graphs.pie().worldPie(worldTimes);
            String longestWorldPlayed = session.getValue(SessionKeys.LONGEST_WORLD_PLAYED).orElse("Unknown");

            boolean hasEnded = session.supports(SessionKeys.END);
            String sessionEnd = hasEnded ? yearFormatter.apply(() -> session.getUnsafe(SessionKeys.END)) : "Online";

            String length = (hasEnded ? "" : "(Online) ") + timeAmountFormatter.apply(session.getValue(SessionKeys.LENGTH).orElse(0L));
            String afk = (hasEnded ? "" : "(Inaccurate) ") + timeAmountFormatter.apply(session.getValue(SessionKeys.AFK_TIME).orElse(0L));

            int playerKillCount = session.getValue(SessionKeys.PLAYER_KILL_COUNT).orElse(0);
            int mobKillCount = session.getValue(SessionKeys.MOB_KILL_COUNT).orElse(0);
            int deaths = session.getValue(SessionKeys.DEATH_COUNT).orElse(0);

            String info = appendWorldPercentage
                    ? HtmlStructure.separateWithDots(sessionStart, longestWorldPlayed)
                    : sessionStart;
            String title = HtmlStructure.separateWithDots(playerName, info) + "<span class=\"pull-right\">" + length + "</span>";
            String htmlID = "" + session.getValue(SessionKeys.START).orElse(0L) + i;
            String worldHtmlID = "worldPieSession" + htmlID;

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold(Icons.SESSION_LENGTH, "Session Ended", sessionEnd)
                    .addRowBold(Icons.PLAYTIME, "Session Length", length)
                    .addRowBold(Icons.AFK_LENGTH, "AFK", afk)
                    .addRowBold(Icons.SERVER, "Server", serverName)
                    .addBreak()
                    .addRowBold(Icons.PLAYER_KILLS, "Player Kills", playerKillCount)
                    .addRowBold(Icons.MOB_KILLS, "Mob Kills", mobKillCount)
                    .addRowBold(Icons.DEATHS, "Deaths", deaths)
                    .toHtml();

            String rightSide = "<div id=\"" + worldHtmlID + "\" class=\"dashboard-donut-chart\"></div>" +
                    "<script>" +
                    "var " + worldHtmlID + "series = {name:'World Playtime',colorByPoint:true,data:" + worldPie.toHighChartsSeries() + "};" +
                    "var " + worldHtmlID + "gmseries = " + worldPie.toHighChartsDrilldown() + ";" +
                    "</script>";
            viewScript.append("worldPie(")
                    .append(worldHtmlID).append(", ")
                    .append(worldHtmlID).append("series, ")
                    .append(worldHtmlID).append("gmseries")
                    .append(");");

            List<PlayerKill> kills = session.getValue(SessionKeys.PLAYER_KILLS).orElse(new ArrayList<>());
            String leftBottom = tables.killsTable(kills, null).parseHtml();

            String link = PlanAPI.getInstance().getPlayerInspectPageLink(playerName);
            String rightBottom = "<a target=\"_blank\" href=\"" + link + "\"><button href=\"" + link +
                    "\" type=\"button\" class=\"pull-right btn bg-blue waves-effect\">" +
                    "<i class=\"material-icons\">person</i><span>INSPECT PAGE</span></button></a>";

            addElement(new AccordionElement(htmlID, title)
                    .setColor(theme.getValue(ThemeVal.PARSED_SESSION_ACCORDION))
                    .setLeftSide(leftSide + leftBottom)
                    .setRightSide(rightSide + rightBottom));
            i++;
        }
    }

    private void addElementsForPlayer() {
        Map<UUID, String> serverNames = serverNamesSupplier.get();
        sessions.sort(new DateHolderRecentComparator());

        int i = 0;
        for (Session session : new ArrayList<>(sessions)) {
            if (i >= maxSessions) {
                break;
            }

            String serverName = serverNames.getOrDefault(session.getValue(SessionKeys.SERVER_UUID).orElse(null), "Unknown");
            String sessionStart = yearFormatter.apply(session);

            WorldTimes worldTimes = session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));
            WorldPie worldPie = graphs.pie().worldPie(worldTimes);
            String longestWorldPlayed = session.getValue(SessionKeys.LONGEST_WORLD_PLAYED).orElse("Unknown");

            boolean hasEnded = session.supports(SessionKeys.END);
            String sessionEnd = hasEnded ? yearFormatter.apply(() -> session.getValue(SessionKeys.END).orElse(0L)) : "Online";

            String length = (hasEnded ? "" : "(Online) ") + timeAmountFormatter.apply(session.getValue(SessionKeys.LENGTH).orElse(0L));
            String afk = (hasEnded ? "" : "(Inaccurate) ") + timeAmountFormatter.apply(session.getValue(SessionKeys.AFK_TIME).orElse(0L));

            int playerKillCount = session.getValue(SessionKeys.PLAYER_KILL_COUNT).orElse(0);
            int mobKillCount = session.getValue(SessionKeys.MOB_KILL_COUNT).orElse(0);
            int deaths = session.getValue(SessionKeys.DEATH_COUNT).orElse(0);

            String info = appendWorldPercentage
                    ? HtmlStructure.separateWithDots(sessionStart, longestWorldPlayed)
                    : sessionStart;
            String title = HtmlStructure.separateWithDots(serverName, info) + "<span class=\"pull-right\">" + length + "</span>";
            String htmlID = "" + session.getValue(SessionKeys.START).orElse(0L) + i;
            String worldHtmlID = "worldPieSession" + htmlID;

            String leftSide = new AccordionElementContentBuilder()
                    .addRowBold(Icons.SESSION_LENGTH, "Session Ended", sessionEnd)
                    .addRowBold(Icons.PLAYTIME, "Session Length", length)
                    .addRowBold(Icons.AFK_LENGTH, "AFK", afk)
                    .addRowBold(Icons.SERVER, "Server", serverName)
                    .addBreak()
                    .addRowBold(Icons.PLAYER_KILLS, "Player Kills", playerKillCount)
                    .addRowBold(Icons.MOB_KILLS, "Mob Kills", mobKillCount)
                    .addRowBold(Icons.DEATHS, "Deaths", deaths)
                    .toHtml();

            String rightSide = "<div id=\"" + worldHtmlID + "\" class=\"dashboard-donut-chart\"></div>" +
                    "<script>" +
                    "var " + worldHtmlID + "series = {name:'World Playtime',colorByPoint:true,data:" + worldPie.toHighChartsSeries() + "};" +
                    "var " + worldHtmlID + "gmseries = " + worldPie.toHighChartsDrilldown() + ";" +
                    "</script>";
            viewScript.append("worldPie(")
                    .append(worldHtmlID).append(", ")
                    .append(worldHtmlID).append("series, ")
                    .append(worldHtmlID).append("gmseries")
                    .append(");");

            List<PlayerKill> kills = session.getValue(SessionKeys.PLAYER_KILLS).orElse(new ArrayList<>());
            String leftBottom = tables.killsTable(kills, null).parseHtml();

            addElement(new AccordionElement(htmlID, title)
                    .setColor(theme.getValue(ThemeVal.PARSED_SESSION_ACCORDION))
                    .setLeftSide(leftSide + leftBottom)
                    .setRightSide(rightSide));

            i++;
        }
    }

}