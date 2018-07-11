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
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.PerServerDataMutator;
import com.djrapitops.plan.data.store.mutators.PvpInfoMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
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

    public String parse(PlayerContainer container, UUID serverUUID, Map<UUID, String> serverNames) throws IOException {
        long now = System.currentTimeMillis();

        PlaceholderReplacer replacer = new PlaceholderReplacer();

        replacer.put("refresh", FormatUtils.formatTimeStampClock(now));
        replacer.put("version", MiscUtils.getPlanVersion());
        replacer.put("timeZone", MiscUtils.getTimeZoneOffsetHours());

        String online = "Offline";
        Optional<Session> activeSession = SessionCache.getCachedSession(uuid);
        if (activeSession.isPresent()) {
            Session session = activeSession.get();
            session.setSessionID(Integer.MAX_VALUE);
            online = serverNames.get(serverUUID);
            container.putRawData(PlayerKeys.ACTIVE_SESSION, session);
        }

        String playerName = container.getValue(PlayerKeys.NAME).orElse("Unknown");
        int timesKicked = container.getValue(PlayerKeys.KICK_COUNT).orElse(0);

        replacer.addAllPlaceholdersFrom(container, Formatters.yearLongValue(),
                PlayerKeys.REGISTERED, PlayerKeys.LAST_SEEN
        );

        replacer.put("playerName", playerName);
        replacer.put("kickCount", timesKicked);

        PerServerContainer perServerContainer = container.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer());
        PerServerDataMutator perServerDataMutator = new PerServerDataMutator(perServerContainer);

        Map<UUID, WorldTimes> worldTimesPerServer = perServerDataMutator.worldTimesPerServer();
        replacer.put("serverPieSeries", new ServerPreferencePie(serverNames, worldTimesPerServer).toHighChartsSeries());
        replacer.put("worldPieColors", Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        replacer.put("gmPieColors", Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        replacer.put("serverPieColors", Theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));

        String favoriteServer = serverNames.getOrDefault(perServerDataMutator.favoriteServer(), "Unknown");
        replacer.put("favoriteServer", favoriteServer);

        replacer.put("tableBodyNicknames", new NicknameTable(
                container.getValue(PlayerKeys.NICKNAMES).orElse(new ArrayList<>()), serverNames)
                .parseBody());
        replacer.put("tableBodyIPs", new GeoInfoTable(container.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>())).parseBody());

        List<Session> allSessions = container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(container);
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

        ServerAccordion serverAccordion = new ServerAccordion(container, serverNames);

        PlayerCalendar playerCalendar = new PlayerCalendar(container);

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
        WorldTimes worldTimes = container.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));

        WorldPie worldPie = new WorldPie(worldTimes);

        replacer.put("worldPieSeries", worldPie.toHighChartsSeries());
        replacer.put("gmSeries", worldPie.toHighChartsDrilldown());

        replacer.put("punchCardSeries", punchCardData);

        pvpAndPve(replacer, sessionsMutator, weekSessionsMutator, monthSessionsMutator);

        ActivityIndex activityIndex = container.getActivityIndex(now);

        replacer.put("activityIndexNumber", activityIndex.getFormattedValue());
        replacer.put("activityIndexColor", activityIndex.getColor());
        replacer.put("activityIndex", activityIndex.getGroup());

        replacer.put("playerStatus", HtmlStructure.playerStatus(online,
                container.getValue(PlayerKeys.BANNED).orElse(false),
                container.getValue(PlayerKeys.OPERATOR).orElse(false)));

        if (!InfoSystem.getInstance().getConnectionSystem().isServerAvailable()) {
            replacer.put("networkName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        }

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
