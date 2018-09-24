/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;
import com.djrapitops.plan.utilities.html.HtmlStructure;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.calendar.PlayerCalendar;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.Accordions;
import com.djrapitops.plan.utilities.html.structure.ServerAccordion;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.benchmarking.Timings;

import java.io.IOException;
import java.util.*;

/**
 * Used for parsing Inspect page out of database data and the html.
 *
 * @author Rsl1122
 */
public class InspectPage implements Page {

    private final PlayerContainer player;
    private final Map<UUID, String> serverNames;

    private final String version;

    private final PlanFiles planFiles;
    private final PlanConfig config;
    private final Theme theme;
    private final Graphs graphs;
    private final HtmlTables tables;
    private final Accordions accordions;
    private final ServerInfo serverInfo;
    private final Timings timings;

    private final Formatter<Long> timeAmountFormatter;
    private final Formatter<Long> clockLongFormatter;
    private final Formatter<Long> yearLongFormatter;
    private final Formatter<Double> decimalFormatter;

    InspectPage(
            PlayerContainer player, Map<UUID, String> serverNames,
            String version,
            PlanFiles planFiles,
            PlanConfig config,
            Theme theme,
            Graphs graphs,
            HtmlTables tables,
            Accordions accordions,
            Formatters formatters,
            ServerInfo serverInfo,
            Timings timings
    ) {
        this.player = player;
        this.serverNames = serverNames;
        this.version = version;
        this.planFiles = planFiles;
        this.config = config;
        this.theme = theme;
        this.graphs = graphs;
        this.tables = tables;
        this.accordions = accordions;
        this.serverInfo = serverInfo;
        this.timings = timings;

        timeAmountFormatter = formatters.timeAmount();
        clockLongFormatter = formatters.clockLong();
        yearLongFormatter = formatters.yearLong();
        decimalFormatter = formatters.decimals();
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            timings.start("Inspect Parse, Fetch");
            if (!player.getValue(PlayerKeys.REGISTERED).isPresent()) {
                throw new IllegalStateException("Player is not registered");
            }
            UUID serverUUID = serverInfo.getServerUUID();

            timings.end("Inspect Parse, Fetch");

            return parse(player, serverUUID, serverNames);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }

    public String parse(PlayerContainer player, UUID serverUUID, Map<UUID, String> serverNames) throws IOException {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUnsafe(PlayerKeys.UUID);

        PlaceholderReplacer replacer = new PlaceholderReplacer();

        replacer.put("refresh", clockLongFormatter.apply(now));
        replacer.put("version", version);
        replacer.put("timeZone", config.getTimeZoneOffsetHours());

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

        replacer.addAllPlaceholdersFrom(player, yearLongFormatter,
                PlayerKeys.REGISTERED, PlayerKeys.LAST_SEEN
        );

        replacer.put("playerName", playerName);
        replacer.put("kickCount", timesKicked);

        PerServerContainer perServerContainer = player.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer());
        PerServerMutator perServerMutator = new PerServerMutator(perServerContainer);

        Map<UUID, WorldTimes> worldTimesPerServer = perServerMutator.worldTimesPerServer();
        replacer.put("serverPieSeries", graphs.pie().serverPreferencePie(serverNames, worldTimesPerServer).toHighChartsSeries());
        replacer.put("worldPieColors", theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        replacer.put("gmPieColors", theme.getValue(ThemeVal.GRAPH_GM_PIE));
        replacer.put("serverPieColors", theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));

        String favoriteServer = serverNames.getOrDefault(perServerMutator.favoriteServer(), "Unknown");
        replacer.put("favoriteServer", favoriteServer);

        replacer.put("tableBodyNicknames",
                tables.nicknameTable(player.getValue(PlayerKeys.NICKNAMES).orElse(new ArrayList<>()), serverNames).parseBody()
        );
        replacer.put("tableBodyIPs", tables.geoInfoTable(player.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>())).parseBody());

        PingMutator pingMutator = PingMutator.forContainer(player);
        double averagePing = pingMutator.average();
        int minPing = pingMutator.min();
        int maxPing = pingMutator.max();
        String unavailable = "Unavailable";
        replacer.put("avgPing", averagePing != -1 ? decimalFormatter.apply(averagePing) + " ms" : unavailable);
        replacer.put("minPing", minPing != -1 ? minPing + " ms" : unavailable);
        replacer.put("maxPing", maxPing != -1 ? maxPing + " ms" : unavailable);

        List<Session> allSessions = player.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
        allSessions.sort(new SessionStartComparator());

        String sessionAccordionViewScript = "";
        if (allSessions.isEmpty()) {
            replacer.put("accordionSessions", "<div class=\"body\">" + "<p>No Sessions</p>" + "</div>");
        } else {
            if (config.isTrue(Settings.DISPLAY_SESSIONS_AS_TABLE)) {
                replacer.put("accordionSessions", tables.playerSessionTable(playerName, allSessions).parseHtml());
            } else {
                SessionAccordion sessionAccordion = accordions.playerSessionAccordion(allSessions, () -> serverNames);
                replacer.put("accordionSessions", sessionAccordion.toHtml());
                sessionAccordionViewScript = sessionAccordion.toViewScript();
            }
        }

        ServerAccordion serverAccordion = accordions.serverAccordion(player, serverNames);

        PlayerCalendar playerCalendar = graphs.calendar().playerCalendar(player);

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

        String punchCardData = graphs.special().punchCard(allSessions).toHighChartsSeries();
        WorldTimes worldTimes = player.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));

        WorldPie worldPie = graphs.pie().worldPie(worldTimes);

        replacer.put("worldPieSeries", worldPie.toHighChartsSeries());
        replacer.put("gmSeries", worldPie.toHighChartsDrilldown());

        replacer.put("punchCardSeries", punchCardData);

        pvpAndPve(replacer, sessionsMutator, weekSessionsMutator, monthSessionsMutator);

        ActivityIndex activityIndex = player.getActivityIndex(now);

        replacer.put("activityIndexNumber", activityIndex.getFormattedValue(decimalFormatter));
        replacer.put("activityIndexColor", activityIndex.getColor());
        replacer.put("activityIndex", activityIndex.getGroup());

        replacer.put("playerStatus", HtmlStructure.playerStatus(online,
                player.getValue(PlayerKeys.BANNED).orElse(false),
                player.getValue(PlayerKeys.OPERATOR).orElse(false)));

        String serverName = serverNames.get(serverUUID);
        replacer.put("networkName",
                serverName.equalsIgnoreCase("bungeecord")
                        ? config.getString(Settings.BUNGEE_NETWORK_NAME)
                        : serverName
        );

        return replacer.apply(planFiles.readCustomizableResourceFlat("web/player.html"));
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

        Formatter<Long> formatter = timeAmountFormatter;
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
        String playerKillsTable = tables.killsTable(sessionsMutator.toPlayerKillList(), "red").parseHtml();
        String playerDeathTable = tables.deathsTable(sessionsMutator.toPlayerDeathList()).parseHtml();

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
        replacer.put("KDR", decimalFormatter.apply(pvpInfoMutator.killDeathRatio()));
        replacer.put("mobKDR", decimalFormatter.apply(pvpInfoMutator.mobKillDeathRatio()));

        replacer.put("playerKillCountMonth", pvpInfoMutatorMonth.playerKills());
        replacer.put("mobKillCountMonth", pvpInfoMutatorMonth.mobKills());
        replacer.put("playerDeathCountMonth", pvpInfoMutatorMonth.playerCausedDeaths());
        replacer.put("mobDeathCountMonth", pvpInfoMutatorMonth.mobCausedDeaths());
        replacer.put("deathCountMonth", pvpInfoMutatorMonth.deaths());
        replacer.put("KDRMonth", decimalFormatter.apply(pvpInfoMutatorMonth.killDeathRatio()));
        replacer.put("mobKDRMonth", decimalFormatter.apply(pvpInfoMutatorMonth.mobKillDeathRatio()));

        replacer.put("playerKillCountWeek", pvpInfoMutatorWeek.playerKills());
        replacer.put("mobKillCountWeek", pvpInfoMutatorWeek.mobKills());
        replacer.put("playerDeathCountWeek", pvpInfoMutatorWeek.playerCausedDeaths());
        replacer.put("mobDeathCountWeek", pvpInfoMutatorWeek.mobCausedDeaths());
        replacer.put("deathCountWeek", pvpInfoMutatorWeek.deaths());
        replacer.put("KDRWeek", decimalFormatter.apply(pvpInfoMutatorWeek.killDeathRatio()));
        replacer.put("mobKDRWeek", decimalFormatter.apply(pvpInfoMutatorWeek.mobKillDeathRatio()));
    }
}
