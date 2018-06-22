/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.PerServerDataMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
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
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.graphs.PunchCardGraph;
import com.djrapitops.plan.utilities.html.graphs.calendar.PlayerCalendar;
import com.djrapitops.plan.utilities.html.graphs.pie.ServerPreferencePie;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.ServerAccordion;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;
import com.djrapitops.plan.utilities.html.tables.GeoInfoTable;
import com.djrapitops.plan.utilities.html.tables.NicknameTable;
import com.djrapitops.plan.utilities.html.tables.PlayerSessionTable;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used for parsing Inspect page out of database data and the html.
 *
 * @author Rsl1122
 */
public class InspectPage extends Page {

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

        addValue("refresh", FormatUtils.formatTimeStampClock(now));
        addValue("version", MiscUtils.getPlanVersion());
        addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());

        String online = "Offline";
        Optional<Session> activeSession = SessionCache.getCachedSession(uuid);
        if (activeSession.isPresent()) {
            Session session = activeSession.get();
            session.setSessionID(Integer.MAX_VALUE);
            online = serverNames.get(serverUUID);
            container.putRawData(PlayerKeys.ACTIVE_SESSION, session);
        }

        String playerName = container.getValue(PlayerKeys.NAME).orElse("Unknown");
        long registered = container.getValue(PlayerKeys.REGISTERED).orElse(-1L);
        int timesKicked = container.getValue(PlayerKeys.KICK_COUNT).orElse(0);
        long lastSeen = container.getValue(PlayerKeys.LAST_SEEN).orElse(-1L);

        addValue("registered", Formatters.year().apply(() -> registered));
        addValue("playerName", playerName);
        addValue("kickCount", timesKicked);

        addValue("lastSeen", Formatters.year().apply(() -> lastSeen));

        PerServerContainer perServerContainer = container.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer());
        PerServerDataMutator perServerDataMutator = new PerServerDataMutator(perServerContainer);

        Map<UUID, WorldTimes> worldTimesPerServer = perServerDataMutator.worldTimesPerServer();
        addValue("serverPieSeries", new ServerPreferencePie(serverNames, worldTimesPerServer).toHighChartsSeries());
        addValue("worldPieColors", Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        addValue("gmPieColors", Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        addValue("serverPieColors", Theme.getValue(ThemeVal.GRAPH_SERVER_PREF_PIE));

        String favoriteServer = serverNames.getOrDefault(perServerDataMutator.favoriteServer(), "Unknown");
        addValue("favoriteServer", favoriteServer);

        addValue("tableBodyNicknames", new NicknameTable(
                container.getValue(PlayerKeys.NICKNAMES).orElse(new ArrayList<>()), serverNames)
                .parseBody());
        addValue("tableBodyIPs", new GeoInfoTable(container.getValue(PlayerKeys.GEO_INFO).orElse(new ArrayList<>())).parseBody());

        Map<UUID, List<Session>> sessions = perServerDataMutator.sessionsPerServer();
        Map<String, List<Session>> sessionsByServerName = sessions.entrySet().stream()
                .collect(Collectors.toMap(entry -> serverNames.get(entry.getKey()), Map.Entry::getValue));

        List<Session> allSessions = container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        SessionsMutator sessionsMutator = SessionsMutator.forContainer(container);
        allSessions.sort(new SessionStartComparator());

        String sessionAccordionViewScript = "";
        if (allSessions.isEmpty()) {
            addValue("accordionSessions", "<div class=\"body\">" + "<p>No Sessions</p>" + "</div>");
        } else {
            if (Settings.DISPLAY_SESSIONS_AS_TABLE.isTrue()) {
                addValue("accordionSessions", new PlayerSessionTable(playerName, allSessions).parseHtml());
            } else {
                SessionAccordion sessionAccordion = SessionAccordion.forPlayer(allSessions, () -> serverNames);
                addValue("accordionSessions", sessionAccordion.toHtml());
                sessionAccordionViewScript = sessionAccordion.toViewScript();
            }
        }

        // TODO Session table if setting is enabled
        ServerAccordion serverAccordion = new ServerAccordion(container, serverNames);

        PlayerCalendar playerCalendar = new PlayerCalendar(allSessions, registered);

        addValue("calendarSeries", playerCalendar.toCalendarSeries());
        addValue("firstDay", 1);

        addValue("accordionServers", serverAccordion.toHtml());
        addValue("sessionTabGraphViewFunctions", sessionAccordionViewScript + serverAccordion.toViewScript());

        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        SessionsMutator daySessionsMutator = sessionsMutator.filterSessionsBetween(dayAgo, now);
        SessionsMutator weekSessionsMutator = sessionsMutator.filterSessionsBetween(weekAgo, now);
        SessionsMutator monthSessionsMutator = sessionsMutator.filterSessionsBetween(monthAgo, now);

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
        addValue("playtimeTotal", formatter.apply(playtime));
        addValue("playtimeDay", formatter.apply(playtimeDay));
        addValue("playtimeWeek", formatter.apply(playtimeWeek));
        addValue("playtimeMonth", formatter.apply(playtimeMonth));

        addValue("activeTotal", formatter.apply(activeTotal));

        addValue("afkTotal", formatter.apply(afk));
        addValue("afkDay", formatter.apply(afkDay));
        addValue("afkWeek", formatter.apply(afkWeek));
        addValue("afkMonth", formatter.apply(afkMonth));

        addValue("sessionLengthLongest", formatter.apply(longestSession));
        addValue("sessionLongestDay", formatter.apply(longestSessionDay));
        addValue("sessionLongestWeek", formatter.apply(longestSessionWeek));
        addValue("sessionLongestMonth", formatter.apply(longestSessionMonth));

        addValue("sessionLengthMedian", formatter.apply(sessionMedian));
        addValue("sessionMedianDay", formatter.apply(sessionMedianDay));
        addValue("sessionMedianWeek", formatter.apply(sessionMedianWeek));
        addValue("sessionMedianMonth", formatter.apply(sessionMedianMonth));

        addValue("sessionAverage", formatter.apply(sessionAverage));
        addValue("sessionAverageDay", formatter.apply(sessionAverageDay));
        addValue("sessionAverageWeek", formatter.apply(sessionAverageWeek));
        addValue("sessionAverageMonth", formatter.apply(sessionAverageMonth));

        addValue("sessionCount", sessionCount);
        addValue("sessionCountDay", sessionCountDay);
        addValue("sessionCountWeek", sessionCountWeek);
        addValue("sessionCountMonth", sessionCountMonth);

        String punchCardData = new PunchCardGraph(allSessions).toHighChartsSeries();
        WorldTimes worldTimes = container.getValue(PlayerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()));

        WorldPie worldPie = new WorldPie(worldTimes);

        addValue("worldPieSeries", worldPie.toHighChartsSeries());
        addValue("gmSeries", worldPie.toHighChartsDrilldown());

        addValue("punchCardSeries", punchCardData);

        long playerKillCount = allSessions.stream().map(Session::getPlayerKills).mapToLong(Collection::size).sum();
        long mobKillCount = allSessions.stream().mapToLong(Session::getMobKills).sum();
        long deathCount = allSessions.stream().mapToLong(Session::getDeaths).sum();

        addValue("playerKillCount", playerKillCount);
        addValue("mobKillCount", mobKillCount);
        addValue("deathCount", deathCount);

        ActivityIndex activityIndex = container.getActivityIndex(now);

        addValue("activityIndexNumber", activityIndex.getFormattedValue());
        addValue("activityIndexColor", activityIndex.getColor());
        addValue("activityIndex", activityIndex.getGroup());

        addValue("playerStatus", HtmlStructure.playerStatus(online,
                container.getValue(PlayerKeys.BANNED).orElse(false),
                container.getValue(PlayerKeys.OPERATOR).orElse(false)));

        if (!InfoSystem.getInstance().getConnectionSystem().isServerAvailable()) {
            addValue("networkName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
        }

        return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("web/player.html"), placeHolders);
    }
}
