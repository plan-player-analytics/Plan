package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.store.mutators.combiners.MultiBanCombiner;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.mutators.health.HealthInformation;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.analysis.ServerBanDataReader;
import com.djrapitops.plan.utilities.html.graphs.ActivityStackGraph;
import com.djrapitops.plan.utilities.html.graphs.PunchCardGraph;
import com.djrapitops.plan.utilities.html.graphs.WorldMap;
import com.djrapitops.plan.utilities.html.graphs.bar.GeolocationBarGraph;
import com.djrapitops.plan.utilities.html.graphs.calendar.ServerCalendar;
import com.djrapitops.plan.utilities.html.graphs.line.*;
import com.djrapitops.plan.utilities.html.graphs.pie.ActivityPie;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.structure.AnalysisPluginsTabContentCreator;
import com.djrapitops.plan.utilities.html.structure.RecentLoginList;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;
import com.djrapitops.plan.utilities.html.tables.CommandUseTable;
import com.djrapitops.plan.utilities.html.tables.PingTable;
import com.djrapitops.plan.utilities.html.tables.PlayersTable;
import com.djrapitops.plan.utilities.html.tables.ServerSessionTable;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Container used for analysis.
 *
 * @author Rsl1122
 * @see AnalysisKeys for Key objects
 * @see PlaceholderKey for placeholder information
 */
public class AnalysisContainer extends DataContainer {

    private static final Key<Map<UUID, String>> serverNames = new Key<>(new Type<Map<UUID, String>>() {
    }, "SERVER_NAMES");
    private final ServerContainer serverContainer;

    public AnalysisContainer(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
        addAnalysisSuppliers();
    }

    public ServerContainer getServerContainer() {
        return serverContainer;
    }

    private void addAnalysisSuppliers() {
        putSupplier(AnalysisKeys.SESSIONS_MUTATOR, () -> SessionsMutator.forContainer(serverContainer));
        putSupplier(AnalysisKeys.TPS_MUTATOR, () -> TPSMutator.forContainer(serverContainer));
        putSupplier(AnalysisKeys.PLAYERS_MUTATOR, () -> PlayersMutator.forContainer(serverContainer));

        addConstants();
        addPlayerSuppliers();
        addSessionSuppliers();
        addGraphSuppliers();
        addTPSAverageSuppliers();
        addCommandSuppliers();
        addServerHealth();
        addPluginSuppliers();

        runCombiners();
    }

    private void runCombiners() {
        new MultiBanCombiner(this.serverContainer).combine(getUnsafe(AnalysisKeys.BAN_DATA));
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(AnalysisKeys.ANALYSIS_TIME, now);
        putRawData(AnalysisKeys.ANALYSIS_TIME_DAY_AGO, now - TimeAmount.DAY.ms());
        putRawData(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO, now - TimeAmount.WEEK.ms());
        putRawData(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO, now - TimeAmount.MONTH.ms());
        putSupplier(AnalysisKeys.REFRESH_TIME_F, () -> Formatters.second().apply(() -> getUnsafe(AnalysisKeys.ANALYSIS_TIME)));

        putRawData(AnalysisKeys.VERSION, PlanHelper.getInstance().getVersion());
        putSupplier(AnalysisKeys.TIME_ZONE, MiscUtils::getTimeZoneOffsetHours);
        putRawData(AnalysisKeys.FIRST_DAY, 1);
        putRawData(AnalysisKeys.TPS_MEDIUM, Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber());
        putRawData(AnalysisKeys.TPS_HIGH, Settings.THEME_GRAPH_TPS_THRESHOLD_HIGH.getNumber());

        addServerProperties();
        addThemeColors();
    }

    private void addServerProperties() {
        putSupplier(AnalysisKeys.SERVER_NAME, () ->
                getUnsafe(serverNames).getOrDefault(serverContainer.getUnsafe(ServerKeys.SERVER_UUID), "Plan")
        );

        ServerProperties serverProperties = ServerInfo.getServerProperties();
        putRawData(AnalysisKeys.PLAYERS_MAX, serverProperties.getMaxPlayers());
        putRawData(AnalysisKeys.PLAYERS_ONLINE, serverProperties.getOnlinePlayers());
    }

    private void addThemeColors() {
        putRawData(AnalysisKeys.ACTIVITY_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE));
        putRawData(AnalysisKeys.GM_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        putRawData(AnalysisKeys.PLAYERS_GRAPH_COLOR, Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
        putRawData(AnalysisKeys.TPS_LOW_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_LOW));
        putRawData(AnalysisKeys.TPS_MEDIUM_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_MED));
        putRawData(AnalysisKeys.TPS_HIGH_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_HIGH));
        putRawData(AnalysisKeys.WORLD_MAP_LOW_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_LOW));
        putRawData(AnalysisKeys.WORLD_MAP_HIGH_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        putRawData(AnalysisKeys.WORLD_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        putRawData(AnalysisKeys.AVG_PING_COLOR, Theme.getValue(ThemeVal.GRAPH_AVG_PING));
        putRawData(AnalysisKeys.MAX_PING_COLOR, Theme.getValue(ThemeVal.GRAPH_MAX_PING));
        putRawData(AnalysisKeys.MIN_PING_COLOR, Theme.getValue(ThemeVal.GRAPH_MIN_PING));
    }

    private void addPlayerSuppliers() {
        putSupplier(AnalysisKeys.PLAYER_NAMES, () -> serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(
                        p -> p.getUnsafe(PlayerKeys.UUID), p -> p.getValue(PlayerKeys.NAME).orElse("?"))
                )
        );
        putSupplier(AnalysisKeys.PLAYERS_TOTAL, () -> serverContainer.getValue(ServerKeys.PLAYER_COUNT).orElse(0));
        putSupplier(AnalysisKeys.PLAYERS_LAST_PEAK, () ->
                serverContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS)
                        .map(dateObj -> Integer.toString(dateObj.getValue())).orElse("-")
        );
        putSupplier(AnalysisKeys.PLAYERS_ALL_TIME_PEAK, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> Integer.toString(dateObj.getValue())).orElse("-")
        );
        putSupplier(AnalysisKeys.LAST_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS)
                        .map(dateObj -> Formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.ALL_TIME_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> Formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.OPERATORS, () -> serverContainer.getValue(ServerKeys.OPERATORS).map(List::size).orElse(0));
        putSupplier(AnalysisKeys.PLAYERS_TABLE, () ->
                PlayersTable.forServerPage(getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).all()).parseHtml()
        );
        putSupplier(AnalysisKeys.PING_TABLE, () ->
                new PingTable(
                        getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                                .getPingPerCountry(serverContainer.getUnsafe(ServerKeys.SERVER_UUID))
                ).parseHtml()
        );

        newAndUniquePlayerCounts();
    }

    private void newAndUniquePlayerCounts() {
        Key<PlayersMutator> newDay = new Key<>(PlayersMutator.class, "NEW_DAY");
        Key<PlayersMutator> newWeek = new Key<>(PlayersMutator.class, "NEW_WEEK");
        Key<PlayersMutator> newMonth = new Key<>(PlayersMutator.class, "NEW_MONTH");
        Key<PlayersMutator> uniqueDay = new Key<>(PlayersMutator.class, "UNIQUE_DAY");
        Key<PlayersMutator> uniqueWeek = new Key<>(PlayersMutator.class, "UNIQUE_WEEK");
        Key<PlayersMutator> uniqueMonth = new Key<>(PlayersMutator.class, "UNIQUE_MONTH");
        putSupplier(newDay, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(newWeek, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(newMonth, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(uniqueDay, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(uniqueWeek, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(uniqueMonth, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putSupplier(AnalysisKeys.PLAYERS_NEW_DAY, () -> getUnsafe(newDay).count());
        putSupplier(AnalysisKeys.PLAYERS_NEW_WEEK, () -> getUnsafe(newWeek).count());
        putSupplier(AnalysisKeys.PLAYERS_NEW_MONTH, () -> getUnsafe(newMonth).count());
        putSupplier(AnalysisKeys.PLAYERS_DAY, () -> getUnsafe(uniqueDay).count());
        putSupplier(AnalysisKeys.PLAYERS_WEEK, () -> getUnsafe(uniqueWeek).count());
        putSupplier(AnalysisKeys.PLAYERS_MONTH, () -> getUnsafe(uniqueMonth).count());
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).averageNewPerDay());
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW_DAY, () -> getUnsafe(newDay).averageNewPerDay());
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW_WEEK, () -> getUnsafe(newWeek).averageNewPerDay());
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW_MONTH, () -> getUnsafe(newMonth).averageNewPerDay());

        putSupplier(AnalysisKeys.UNIQUE_PLAYERS_PER_DAY, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).uniqueJoinsPerDay());
        putSupplier(AnalysisKeys.NEW_PLAYERS_PER_DAY, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).newPerDay());
        putSupplier(AnalysisKeys.UNIQUE_PLAYERS_SERIES, () ->
                new AbstractLineGraph(MutatorFunctions.toPoints(
                        getUnsafe(AnalysisKeys.UNIQUE_PLAYERS_PER_DAY))
                ).toHighChartsSeries()
        );
        putSupplier(AnalysisKeys.NEW_PLAYERS_SERIES, () ->
                new AbstractLineGraph(MutatorFunctions.toPoints(
                        getUnsafe(AnalysisKeys.NEW_PLAYERS_PER_DAY))
                ).toHighChartsSeries()
        );

        Key<Integer> retentionDay = new Key<>(Integer.class, "RETENTION_DAY");
        // compareAndFindThoseLikelyToBeRetained can throw exception.
        putSupplier(retentionDay, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).compareAndFindThoseLikelyToBeRetained(
                getUnsafe(newDay).all(), getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO),
                getUnsafe(AnalysisKeys.PLAYERS_ONLINE_RESOLVER)
                ).count()
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_DAY, () -> {
            try {
                return getUnsafe(retentionDay);
            } catch (IllegalStateException noPlayersAfterDateFiltering) {
                return 0;
            }
        });
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_WEEK, () ->
                getUnsafe(newWeek).filterRetained(
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO),
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME)
                ).count()
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_MONTH, () ->
                getUnsafe(newMonth).filterRetained(
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO),
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME)
                ).count()
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_DAY_PERC, () -> {
            try {
                Integer playersNewDay = getUnsafe(AnalysisKeys.PLAYERS_NEW_DAY);
                return playersNewDay != 0 ? Formatters.percentage().apply(1.0 * getUnsafe(retentionDay) / playersNewDay) : "-";
            } catch (IllegalStateException noPlayersAfterDateFiltering) {
                return "Not enough data";
            }
        });
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_WEEK_PERC, () -> {
                    Integer playersNewWeek = getUnsafe(AnalysisKeys.PLAYERS_NEW_WEEK);
                    return playersNewWeek != 0 ? Formatters.percentage().apply(
                            1.0 * getUnsafe(AnalysisKeys.PLAYERS_RETAINED_WEEK) / playersNewWeek) : "-";
                }
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_MONTH_PERC, () -> {
                    Integer playersNewMonth = getUnsafe(AnalysisKeys.PLAYERS_NEW_MONTH);
                    return playersNewMonth != 0 ? Formatters.percentage().apply(
                            1.0 * getUnsafe(AnalysisKeys.PLAYERS_RETAINED_MONTH) / playersNewMonth) : "-";
                }
        );
    }

    private void addSessionSuppliers() {
        Key<SessionAccordion> sessionAccordion = new Key<>(SessionAccordion.class, "SESSION_ACCORDION");
        putSupplier(serverNames, () -> Database.getActive().fetch().getServerNames());
        putSupplier(sessionAccordion, () -> SessionAccordion.forServer(
                getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).all(),
                getSupplier(serverNames),
                () -> getUnsafe(AnalysisKeys.PLAYER_NAMES)
        ));
        putSupplier(AnalysisKeys.SESSION_ACCORDION_HTML, () -> getUnsafe(sessionAccordion).toHtml());
        putSupplier(AnalysisKeys.SESSION_ACCORDION_FUNCTIONS, () -> getUnsafe(sessionAccordion).toViewScript());

        putSupplier(AnalysisKeys.RECENT_LOGINS, () -> new RecentLoginList(
                        serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>())
                ).toHtml()
        );
        putSupplier(AnalysisKeys.SESSION_TABLE, () -> new ServerSessionTable(
                getUnsafe(AnalysisKeys.PLAYER_NAMES), getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).all()).parseHtml()
        );

        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );
        putSupplier(AnalysisKeys.SESSION_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).count());
        putSupplier(AnalysisKeys.PLAYTIME_TOTAL, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlaytime());
        putSupplier(AnalysisKeys.DEATHS, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toDeathCount());
        putSupplier(AnalysisKeys.MOB_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toMobKillCount());
        putSupplier(AnalysisKeys.PLAYER_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlayerKillCount());
        putSupplier(AnalysisKeys.PLAYTIME_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL))
        );
        putSupplier(AnalysisKeys.AVERAGE_PLAYTIME_F, () -> {
                    long players = getUnsafe(AnalysisKeys.PLAYERS_TOTAL);
                    return players != 0 ? Formatters.timeAmount()
                            .apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL) / players) : "-";
                }
        );
        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );

        Key<SessionsMutator> sessionsDay = new Key<>(SessionsMutator.class, "SESSIONS_DAY");
        Key<SessionsMutator> sessionsWeek = new Key<>(SessionsMutator.class, "SESSIONS_WEEK");
        Key<SessionsMutator> sessionsMonth = new Key<>(SessionsMutator.class, "SESSIONS_MONTH");
        putSupplier(sessionsDay, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR)
                .filterSessionsBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(sessionsWeek, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR)
                .filterSessionsBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(sessionsMonth, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR)
                .filterSessionsBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putSupplier(AnalysisKeys.PUNCHCARD_SERIES, () -> new PunchCardGraph(getUnsafe(sessionsMonth).all()).toHighChartsSeries());
        putSupplier(AnalysisKeys.AVG_PLAYERS, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageUniqueJoinsPerDay());
        putSupplier(AnalysisKeys.AVG_PLAYERS_DAY, () -> getUnsafe(sessionsDay).toAverageUniqueJoinsPerDay());
        putSupplier(AnalysisKeys.AVG_PLAYERS_WEEK, () -> getUnsafe(sessionsWeek).toAverageUniqueJoinsPerDay());
        putSupplier(AnalysisKeys.AVG_PLAYERS_MONTH, () -> getUnsafe(sessionsMonth).toAverageUniqueJoinsPerDay());
    }

    private void addGraphSuppliers() {
        Key<WorldPie> worldPie = new Key<>(WorldPie.class, "WORLD_PIE");
        putSupplier(worldPie, () -> new WorldPie(serverContainer.getValue(ServerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>()))));
        putSupplier(AnalysisKeys.WORLD_PIE_SERIES, () -> getUnsafe(worldPie).toHighChartsSeries());
        putSupplier(AnalysisKeys.GM_PIE_SERIES, () -> getUnsafe(worldPie).toHighChartsDrilldown());
        putSupplier(AnalysisKeys.PLAYERS_ONLINE_SERIES, () ->
                new OnlineActivityGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries()
        );
        putSupplier(AnalysisKeys.TPS_SERIES, () -> new TPSGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.CPU_SERIES, () -> new CPUGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.RAM_SERIES, () -> new RamGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.ENTITY_SERIES, () -> new EntityGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.CHUNK_SERIES, () -> new ChunkGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.WORLD_MAP_SERIES, () ->
                new WorldMap(getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).getGeolocations()).toHighChartsSeries()
        );
        Key<GeolocationBarGraph> geolocationBarChart = new Key<>(GeolocationBarGraph.class, "GEOLOCATION_BAR_CHART");
        putSupplier(geolocationBarChart, () -> new GeolocationBarGraph(getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)));
        putSupplier(AnalysisKeys.COUNTRY_CATEGORIES, () -> getUnsafe(geolocationBarChart).toHighChartsCategories());
        putSupplier(AnalysisKeys.COUNTRY_SERIES, () -> getUnsafe(geolocationBarChart).toHighChartsSeries());

        Key<PingGraph> pingGraph = new Key<>(PingGraph.class, "PING_GRAPH");
        putSupplier(pingGraph, () -> new PingGraph(
                PingMutator.forContainer(serverContainer).mutateToByMinutePings().all()
        ));
        putSupplier(AnalysisKeys.AVG_PING_SERIES, () -> getUnsafe(pingGraph).toAvgSeries());
        putSupplier(AnalysisKeys.MAX_PING_SERIES, () -> getUnsafe(pingGraph).toMaxSeries());
        putSupplier(AnalysisKeys.MIN_PING_SERIES, () -> getUnsafe(pingGraph).toMinSeries());

        putSupplier(AnalysisKeys.CALENDAR_SERIES, () -> new ServerCalendar(
                getUnsafe(AnalysisKeys.PLAYERS_MUTATOR),
                getUnsafe(AnalysisKeys.UNIQUE_PLAYERS_PER_DAY),
                getUnsafe(AnalysisKeys.NEW_PLAYERS_PER_DAY)
        ).toCalendarSeries());

        putSupplier(AnalysisKeys.ACTIVITY_DATA, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).toActivityDataMap(getUnsafe(AnalysisKeys.ANALYSIS_TIME)));
        Key<ActivityStackGraph> activityStackGraph = new Key<>(ActivityStackGraph.class, "ACTIVITY_STACK_GRAPH");
        putSupplier(activityStackGraph, () -> new ActivityStackGraph(getUnsafe(AnalysisKeys.ACTIVITY_DATA)));
        putSupplier(AnalysisKeys.ACTIVITY_STACK_CATEGORIES, () -> getUnsafe(activityStackGraph).toHighChartsLabels());
        putSupplier(AnalysisKeys.ACTIVITY_STACK_SERIES, () -> getUnsafe(activityStackGraph).toHighChartsSeries());
        putSupplier(AnalysisKeys.ACTIVITY_PIE_SERIES, () ->
                new ActivityPie(getUnsafe(AnalysisKeys.ACTIVITY_DATA).get(getUnsafe(AnalysisKeys.ANALYSIS_TIME))).toHighChartsSeries()
        );
        putSupplier(AnalysisKeys.PLAYERS_REGULAR, () -> {
            Map<String, Set<UUID>> activityNow = getUnsafe(AnalysisKeys.ACTIVITY_DATA)
                    .floorEntry(getUnsafe(AnalysisKeys.ANALYSIS_TIME)).getValue();
            Set<UUID> veryActiveNow = activityNow.getOrDefault("Very Active", new HashSet<>());
            Set<UUID> activeNow = activityNow.getOrDefault("Active", new HashSet<>());
            Set<UUID> regularNow = activityNow.getOrDefault("Regular", new HashSet<>());
            return veryActiveNow.size() + activeNow.size() + regularNow.size();
        });
    }

    private void addTPSAverageSuppliers() {
        Key<TPSMutator> tpsMonth = new Key<>(TPSMutator.class, "TPS_MONTH");
        Key<TPSMutator> tpsWeek = new Key<>(TPSMutator.class, "TPS_WEEK");
        Key<TPSMutator> tpsDay = new Key<>(TPSMutator.class, "TPS_DAY");

        putSupplier(tpsMonth, () -> getUnsafe(AnalysisKeys.TPS_MUTATOR)
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(tpsWeek, () -> getUnsafe(AnalysisKeys.TPS_MUTATOR)
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putSupplier(tpsDay, () -> getUnsafe(AnalysisKeys.TPS_MUTATOR)
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putSupplier(AnalysisKeys.PLAYERS_ONLINE_RESOLVER, () -> new PlayersOnlineResolver(getUnsafe(AnalysisKeys.TPS_MUTATOR)));

        putSupplier(AnalysisKeys.TPS_SPIKE_MONTH, () -> getUnsafe(tpsMonth).lowTpsSpikeCount());
        putSupplier(AnalysisKeys.AVG_TPS_MONTH, () -> getUnsafe(tpsMonth).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_MONTH, () -> getUnsafe(tpsMonth).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_MONTH, () -> getUnsafe(tpsMonth).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_MONTH, () -> getUnsafe(tpsMonth).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_MONTH, () -> getUnsafe(tpsMonth).averageChunks());
        putSupplier(AnalysisKeys.TPS_SPIKE_WEEK, () -> getUnsafe(tpsWeek).lowTpsSpikeCount());
        putSupplier(AnalysisKeys.AVG_TPS_WEEK, () -> getUnsafe(tpsWeek).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_WEEK, () -> getUnsafe(tpsWeek).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_WEEK, () -> getUnsafe(tpsWeek).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_WEEK, () -> getUnsafe(tpsWeek).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_WEEK, () -> getUnsafe(tpsWeek).averageChunks());
        putSupplier(AnalysisKeys.TPS_SPIKE_DAY, () -> getUnsafe(tpsDay).lowTpsSpikeCount());
        putSupplier(AnalysisKeys.AVG_TPS_DAY, () -> getUnsafe(tpsDay).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_DAY, () -> getUnsafe(tpsDay).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_DAY, () -> getUnsafe(tpsDay).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_DAY, () -> getUnsafe(tpsDay).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_DAY, () -> getUnsafe(tpsDay).averageChunks());
    }

    private void addCommandSuppliers() {
        putSupplier(AnalysisKeys.COMMAND_USAGE_TABLE, () -> new CommandUseTable(serverContainer).parseHtml());
        putSupplier(AnalysisKeys.COMMAND_COUNT_UNIQUE, () -> serverContainer.getValue(ServerKeys.COMMAND_USAGE).map(Map::size).orElse(0));
        putSupplier(AnalysisKeys.COMMAND_COUNT, () -> CommandUseMutator.forContainer(serverContainer).commandUsageCount());
    }

    private void addServerHealth() {
        Key<HealthInformation> healthInformation = new Key<>(HealthInformation.class, "HEALTH_INFORMATION");
        putSupplier(healthInformation, () -> new HealthInformation(this));
        putSupplier(AnalysisKeys.HEALTH_INDEX, () -> getUnsafe(healthInformation).getServerHealth());
        putSupplier(AnalysisKeys.HEALTH_NOTES, () -> getUnsafe(healthInformation).toHtml());
    }

    private void addPluginSuppliers() {
        // TODO Refactor into a system that supports running the analysis on Bungee
        Key<String[]> navAndTabs = new Key<>(new Type<String[]>() {
        }, "NAV_AND_TABS");
        putSupplier(navAndTabs, () ->
                AnalysisPluginsTabContentCreator.createContent(
                        getUnsafe(AnalysisKeys.PLAYERS_MUTATOR),
                        this
                )
        );
        putSupplier(AnalysisKeys.BAN_DATA, () -> new ServerBanDataReader().readBanDataForContainer(this));
        putSupplier(AnalysisKeys.PLUGINS_TAB_NAV, () -> getUnsafe(navAndTabs)[0]);
        putSupplier(AnalysisKeys.PLUGINS_TAB, () -> getUnsafe(navAndTabs)[1]);
    }
}