/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.graphs.PunchCardGraph;
import com.djrapitops.plan.utilities.html.graphs.calendar.PlayerCalendar;
import com.djrapitops.plan.utilities.html.graphs.pie.ServerPreferencePie;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.ServerAccordion;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;
import com.djrapitops.plan.utilities.html.tables.*;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;

import java.io.IOException;
import java.util.*;

/**
 * Used for parsing Inspect page out of database data and the html.
 *
 * @author Rsl1122
 */
public class InspectPage implements Page {

    private final UUID uuid;

    public InspectPage(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            if (uuid == null) {
                throw new IllegalStateException("UUID was null!");
            }
            Benchmark.start("Inspect Parse, Fetch");
            Database db = Database.getActive();
            PlayerContainer container = db.fetch().getPlayerContainer(uuid);
            if (!container.getValue(PlayerKeys.REGISTERED).isPresent()) {
                throw new IllegalStateException("Player is not registered");
            }
            UUID serverUUID = ServerInfo.getServerUUID();
            Map<UUID, String> serverNames = db.fetch().getServerNames();

            Benchmark.stop("Inspect Parse, Fetch");

            return parse(container, serverUUID, serverNames);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public String parse(PlayerContainer player, UUID serverUUID, Map<UUID, String> serverNames) throws IOException {
        long now = System.currentTimeMillis();

        PlaceholderReplacer replacer = new PlaceholderReplacer();

        replacer.put("refresh", FormatUtils.formatTimeStampClock(now));
        replacer.put("version", MiscUtils.getPlanVersion());
        replacer.put("timeZone", MiscUtils.getTimeZoneOffsetHours());

        boolean online = false;
        Optional<Session> activeSession = SessionCache.getCachedSession(uuid);
        if (activeSession.isPresent()) {
            Session session = activeSession.get();
            session.setSessionID(Integer.MAX_VALUE);
            online = true;
            player.putRawData(PlayerKeys.ACTIVE_SESSION, session);
        }

        String playerName = player.getValue(PlayerKeys.NAME).orElse("Unknown");
        int timesKicked = player.getValue(PlayerKeys.KICK_COUNT).orElse(0);

        replacer.addAllPlaceholdersFrom(player, Formatters.yearLongValue(),
                PlayerKeys.REGISTERED, PlayerKeys.LAST_SEEN
        );

        replacer.put("playerName", playerName);
        replacer.put("kickCount", timesKicked);

        PerServerContainer perServerContainer = player.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer());
        PerServerMutator perServerMutator = new PerServerMutator(perServerContainer);

        Map<UUID, WorldTimes> worldTimesPerServer = perServerMutator.worldTimesPerServer();
        replacer.put("serverPieSeries", new ServerPreferencePie(serverNames, worldTimesPerServer).toHighChartsSeries());
        replacer.put("worldPieColors", Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        replacer.put("gmPieColors", Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        replacer.put("serverPieColors", Theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));

        String favoriteServer = serverNames.getOrDefault(perServerMutator.favoriteServer(), "Unknown");
        replacer.put("favoriteServer", favoriteServer);

        replacer.put("tableBodyNicknames", new NicknameTable(
                player.getValue(PlayerKeys.NICKNAMES).orElse(new ArrayList<>()), serverNames)
                .parseBody());
        replacer.put("tableBodyIPs", new GeoInfoTable(player.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>())).parseBody());

        PingMutator pingMutator = PingMutator.forContainer(player);
        double averagePing = pingMutator.average();
        int minPing = pingMutator.min();
        int maxPing = pingMutator.max();
        replacer.put("avgPing", averagePing != -1 ? FormatUtils.cutDecimals(averagePing) + " ms" : "Unavailable");
        replacer.put("minPing", minPing != -1 ? minPing + " ms" : "Unavailable");
        replacer.put("maxPing", maxPing != -1 ? maxPing + " ms" : "Unavailable");

        List<Session> allSessions = player.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
        allSessions.sort(new SessionStartComparator());

        String sessionAccordionViewScript = "";
        if (allSessions.isEmpty()) {
            replacer.put("accordionSessions", "<div class=\"body\">" + "<p>No Sessions</p>" + "</div>");
        } else {
            if (Settings.DISPLAY_SESSIONS_AS_TABLE.isTrue()) {
                replacer.put("accordionSessions", new PlayerSessionTable(playerName, allSessions).parseHtml());
            } else {
                SessionAccordion sessionAccordion = SessionAccordion.forPlayer(allSessions, () -> serverNames);
                replacer.put("accordionSessions", sessionAccordion.toHtml());
                sessionAccordionViewScript = sessionAccordion.toViewScript();
            }
        }

        ServerAccordion serverAccordion = new ServerAccordion(player, serverNames);

        PlayerCalendar playerCalendar = new PlayerCalendar(player);

        replacer.put("calendarSeries", playerCalendar.toCalendarSeries());
        replacer.put("firstDay", 1);

        replacer.put("accordionServers", serverAccordion.toHtml());
        replacer.put("sessionTabGraphViewFunctions", sessionAccordionViewScript + serverAccordion.toViewScript());

        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        SessionsMutator daySessionsMutator = sessionsMutator.filterSessionsBetween(dayAgo, now);
        SessionsMutator weekSessionsMutator = sessionsMutator.filterSessionsBetween(weekAgo, now);
        SessionsMutator monthSessionsMutator = sessionsMutator.filterSessionsBetween(monthAgo, now);

        sessionsAndPlaytime(replacer, sessionsMutator, daySessionsMutator, weekSessionsMutator, monthSessionsMutator);

        String punchCardData = new PunchCardGraph(allSessions).toHighChartsSeries();
        WorldTimes worldTimes = player.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));

        WorldPie worldPie = new WorldPie(worldTimes);

        replacer.put("worldPieSeries", worldPie.toHighChartsSeries());
        replacer.put("gmSeries", worldPie.toHighChartsDrilldown());

        replacer.put("punchCardSeries", punchCardData);

        pvpAndPve(replacer, sessionsMutator, weekSessionsMutator, monthSessionsMutator);

        ActivityIndex activityIndex = player.getActivityIndex(now);

        replacer.put("activityIndexNumber", activityIndex.getFormattedValue());
        replacer.put("activityIndexColor", activityIndex.getColor());
        replacer.put("activityIndex", activityIndex.getGroup());

        replacer.put("playerStatus", HtmlStructure.playerStatus(online,
                player.getValue(PlayerKeys.BANNED).orElse(false),
                player.getValue(PlayerKeys.OPERATOR).orElse(false)));

        String serverName = serverNames.get(serverUUID);
        replacer.put("networkName",
                serverName.equalsIgnoreCase("bungeecord")
                        ? Settings.BUNGEE_NETWORK_NAME.toString()
                        : serverName
        );

        return replacer.apply(FileUtil.getStringFromResource("web/player.html"));
    }

    private void sessionsAndPlaytime(PlaceholderReplacer replacer, SessionsMutator sessionsMutator, SessionsMutator daySessionsMutator, SessionsMutator weekSessionsMutator, SessionsMutator monthSessionsMutator) {
        long playtime = sessionsMutator.toPlaytime();
        long playtimeDay = daySessionsMutator.toPlaytime();
        long playtimeWeek = weekSessionsMutator.toPlaytime();
        long playtimeMonth = monthSessionsMutator.toPlaytime();

        long afk = sessionsMutator.toAfkTime();
        long afkDay = daySessionsMutator.toAfkTime();
        long afkWeek = weekSessionsMutator.toAfkTime();
        long afkMonth = monthSessionsMutator.toAfkTime();

        long activeTotal = playtime - afk;

        long longestSession = sessionsMutator.toLongestSessionLength();
        long longestSessionDay = daySessionsMutator.toLongestSessionLength();
        long longestSessionWeek = weekSessionsMutator.toLongestSessionLength();
        long longestSessionMonth = monthSessionsMutator.toLongestSessionLength();

        long sessionMedian = sessionsMutator.toMedianSessionLength();
        long sessionMedianDay = daySessionsMutator.toMedianSessionLength();
        long sessionMedianWeek = weekSessionsMutator.toMedianSessionLength();
        long sessionMedianMonth = monthSessionsMutator.toMedianSessionLength();

        int sessionCount = sessionsMutator.count();
        int sessionCountDay = daySessionsMutator.count();
        int sessionCountWeek = weekSessionsMutator.count();
        int sessionCountMonth = monthSessionsMutator.count();

        long sessionAverage = sessionsMutator.toAverageSessionLength();
        long sessionAverageDay = daySessionsMutator.toAverageSessionLength();
        long sessionAverageWeek = weekSessionsMutator.toAverageSessionLength();
        long sessionAverageMonth = monthSessionsMutator.toAverageSessionLength();

        Formatter<Long> formatter = Formatters.timeAmount();
        replacer.put("playtimeTotal", formatter.apply(playtime));
        replacer.put("playtimeDay", formatter.apply(playtimeDay));
        replacer.put("playtimeWeek", formatter.apply(playtimeWeek));
        replacer.put("playtimeMonth", formatter.apply(playtimeMonth));

        replacer.put("activeTotal", formatter.apply(activeTotal));

        replacer.put("afkTotal", formatter.apply(afk));
        replacer.put("afkDay", formatter.apply(afkDay));
        replacer.put("afkWeek", formatter.apply(afkWeek));
        replacer.put("afkMonth", formatter.apply(afkMonth));

        replacer.put("sessionLengthLongest", formatter.apply(longestSession));
        replacer.put("sessionLongestDay", formatter.apply(longestSessionDay));
        replacer.put("sessionLongestWeek", formatter.apply(longestSessionWeek));
        replacer.put("sessionLongestMonth", formatter.apply(longestSessionMonth));

        replacer.put("sessionLengthMedian", formatter.apply(sessionMedian));
        replacer.put("sessionMedianDay", formatter.apply(sessionMedianDay));
        replacer.put("sessionMedianWeek", formatter.apply(sessionMedianWeek));
        replacer.put("sessionMedianMonth", formatter.apply(sessionMedianMonth));

        replacer.put("sessionAverage", formatter.apply(sessionAverage));
        replacer.put("sessionAverageDay", formatter.apply(sessionAverageDay));
        replacer.put("sessionAverageWeek", formatter.apply(sessionAverageWeek));
        replacer.put("sessionAverageMonth", formatter.apply(sessionAverageMonth));

        replacer.put("sessionCount", sessionCount);
        replacer.put("sessionCountDay", sessionCountDay);
        replacer.put("sessionCountWeek", sessionCountWeek);
        replacer.put("sessionCountMonth", sessionCountMonth);
    }

    private void pvpAndPve(PlaceholderReplacer replacer, SessionsMutator sessionsMutator, SessionsMutator weekSessionsMutator, SessionsMutator monthSessionsMutator) {
        String playerKillsTable = new KillsTable(sessionsMutator.toPlayerKillList()).parseHtml();
        String playerDeathTable = new DeathsTable(sessionsMutator.toPlayerDeathList()).parseHtml();

        PvpInfoMutator pvpInfoMutator = PvpInfoMutator.forMutator(sessionsMutator);
        PvpInfoMutator pvpInfoMutatorMonth = PvpInfoMutator.forMutator(monthSessionsMutator);
        PvpInfoMutator pvpInfoMutatorWeek = PvpInfoMutator.forMutator(weekSessionsMutator);

        replacer.put("tablePlayerKills", playerKillsTable);
        replacer.put("tablePlayerDeaths", playerDeathTable);

        replacer.put("playerKillCount", pvpInfoMutator.playerKills());
        replacer.put("mobKillCount", pvpInfoMutator.mobKills());
        replacer.put("playerDeathCount", pvpInfoMutator.playerCausedDeaths());
        replacer.put("mobDeathCount", pvpInfoMutator.mobCausedDeaths());
        replacer.put("deathCount", pvpInfoMutator.deaths());
        replacer.put("KDR", FormatUtils.cutDecimals(pvpInfoMutator.killDeathRatio()));
        replacer.put("mobKDR", FormatUtils.cutDecimals(pvpInfoMutator.mobKillDeathRatio()));

        replacer.put("playerKillCountMonth", pvpInfoMutatorMonth.playerKills());
        replacer.put("mobKillCountMonth", pvpInfoMutatorMonth.mobKills());
        replacer.put("playerDeathCountMonth", pvpInfoMutatorMonth.playerCausedDeaths());
        replacer.put("mobDeathCountMonth", pvpInfoMutatorMonth.mobCausedDeaths());
        replacer.put("deathCountMonth", pvpInfoMutatorMonth.deaths());
        replacer.put("KDRMonth", FormatUtils.cutDecimals(pvpInfoMutatorMonth.killDeathRatio()));
        replacer.put("mobKDRMonth", FormatUtils.cutDecimals(pvpInfoMutatorMonth.mobKillDeathRatio()));

        replacer.put("playerKillCountWeek", pvpInfoMutatorWeek.playerKills());
        replacer.put("mobKillCountWeek", pvpInfoMutatorWeek.mobKills());
        replacer.put("playerDeathCountWeek", pvpInfoMutatorWeek.playerCausedDeaths());
        replacer.put("mobDeathCountWeek", pvpInfoMutatorWeek.mobCausedDeaths());
        replacer.put("deathCountWeek", pvpInfoMutatorWeek.deaths());
        replacer.put("KDRWeek", FormatUtils.cutDecimals(pvpInfoMutatorWeek.killDeathRatio()));
        replacer.put("mobKDRWeek", FormatUtils.cutDecimals(pvpInfoMutatorWeek.mobKillDeathRatio()));
    }
}
