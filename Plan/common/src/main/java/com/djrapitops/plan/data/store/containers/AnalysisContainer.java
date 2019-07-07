/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.store.mutators.health.HealthInformation;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.bar.BarGraph;
import com.djrapitops.plan.utilities.html.graphs.line.PingGraph;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraph;
import com.djrapitops.plan.utilities.html.pages.AnalysisPluginTabs;
import com.djrapitops.plan.utilities.html.structure.Accordions;
import com.djrapitops.plan.utilities.html.structure.AnalysisPluginsTabContentCreator;
import com.djrapitops.plan.utilities.html.structure.RecentLoginList;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;
import com.djrapitops.plan.utilities.html.tables.HtmlTables;
import com.djrapitops.plugin.api.TimeAmount;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Container used for analysis.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.AnalysisKeys for Key objects
 * @see com.djrapitops.plan.data.store.PlaceholderKey for placeholder information
 */
public class AnalysisContainer extends DynamicDataContainer {

    private final ServerContainer serverContainer;

    private final String version;
    private final Locale locale;
    private final PlanConfig config;
    private final Theme theme;
    private final ServerProperties serverProperties;
    private final Formatters formatters;
    private final Graphs graphs;
    private final HtmlTables tables;
    private final Accordions accordions;
    private final AnalysisPluginsTabContentCreator pluginsTabContentCreator;
    private TimeZone timeZone;

    public AnalysisContainer(
            ServerContainer serverContainer,
            String version,
            Locale locale,
            PlanConfig config,
            Theme theme,
            ServerProperties serverProperties,
            Formatters formatters,
            Graphs graphs,
            HtmlTables tables,
            Accordions accordions,
            AnalysisPluginsTabContentCreator pluginsTabContentCreator
    ) {
        this.serverContainer = serverContainer;
        this.version = version;
        this.locale = locale;
        this.config = config;
        this.theme = theme;
        this.serverProperties = serverProperties;
        this.formatters = formatters;
        this.graphs = graphs;
        this.tables = tables;
        this.accordions = accordions;
        this.pluginsTabContentCreator = pluginsTabContentCreator;

        timeZone = config.get(TimeSettings.USE_SERVER_TIME) ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT");

        addAnalysisSuppliers();
    }

    public ServerContainer getServerContainer() {
        return serverContainer;
    }

    private void addAnalysisSuppliers() {
        putCachingSupplier(AnalysisKeys.SESSIONS_MUTATOR, () -> SessionsMutator.forContainer(serverContainer));
        putCachingSupplier(AnalysisKeys.TPS_MUTATOR, () -> TPSMutator.forContainer(serverContainer));
        putCachingSupplier(AnalysisKeys.PLAYERS_MUTATOR, () -> PlayersMutator.forContainer(serverContainer));

        addConstants();
        addPlayerSuppliers();
        addSessionSuppliers();
        addGraphSuppliers();
        addTPSAverageSuppliers();
        addCommandSuppliers();
        addServerHealth();
        addPluginSuppliers();
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(AnalysisKeys.ANALYSIS_TIME, now);
        putRawData(AnalysisKeys.ANALYSIS_TIME_DAY_AGO, now - TimeUnit.DAYS.toMillis(1L));
        putRawData(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO, now - TimeAmount.WEEK.toMillis(1L));
        putRawData(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO, now - TimeAmount.MONTH.toMillis(1L));
        putSupplier(AnalysisKeys.REFRESH_TIME_F, () -> formatters.clockLong().apply(getUnsafe(AnalysisKeys.ANALYSIS_TIME)));
        putSupplier(AnalysisKeys.REFRESH_TIME_FULL_F, () -> formatters.secondLong().apply(getUnsafe(AnalysisKeys.ANALYSIS_TIME)));

        putRawData(AnalysisKeys.VERSION, version);
        putSupplier(AnalysisKeys.TIME_ZONE, config::getTimeZoneOffsetHours);
        putRawData(AnalysisKeys.FIRST_DAY, 1);
        putRawData(AnalysisKeys.TPS_MEDIUM, config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED));
        putRawData(AnalysisKeys.TPS_HIGH, config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_HIGH));
        putRawData(AnalysisKeys.DISK_MEDIUM, config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_MED));
        putRawData(AnalysisKeys.DISK_HIGH, config.get(DisplaySettings.GRAPH_DISK_THRESHOLD_HIGH));

        addServerProperties();
        addThemeColors();
    }

    private void addServerProperties() {
        putCachingSupplier(AnalysisKeys.SERVER_NAME, () -> serverContainer.getValue(ServerKeys.NAME).orElse("Plan"));

        putRawData(AnalysisKeys.PLAYERS_MAX, serverProperties.getMaxPlayers());
        putRawData(AnalysisKeys.PLAYERS_ONLINE, serverProperties.getOnlinePlayers());
    }

    private void addThemeColors() {
        putRawData(AnalysisKeys.ACTIVITY_PIE_COLORS, theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE));
        putRawData(AnalysisKeys.GM_PIE_COLORS, theme.getValue(ThemeVal.GRAPH_GM_PIE));
        putRawData(AnalysisKeys.PLAYERS_GRAPH_COLOR, theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
        putRawData(AnalysisKeys.TPS_LOW_COLOR, theme.getValue(ThemeVal.GRAPH_TPS_LOW));
        putRawData(AnalysisKeys.TPS_MEDIUM_COLOR, theme.getValue(ThemeVal.GRAPH_TPS_MED));
        putRawData(AnalysisKeys.TPS_HIGH_COLOR, theme.getValue(ThemeVal.GRAPH_TPS_HIGH));
        putRawData(AnalysisKeys.WORLD_MAP_LOW_COLOR, theme.getValue(ThemeVal.WORLD_MAP_LOW));
        putRawData(AnalysisKeys.WORLD_MAP_HIGH_COLOR, theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        putRawData(AnalysisKeys.WORLD_PIE_COLORS, theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
        putRawData(AnalysisKeys.AVG_PING_COLOR, theme.getValue(ThemeVal.GRAPH_AVG_PING));
        putRawData(AnalysisKeys.MAX_PING_COLOR, theme.getValue(ThemeVal.GRAPH_MAX_PING));
        putRawData(AnalysisKeys.MIN_PING_COLOR, theme.getValue(ThemeVal.GRAPH_MIN_PING));
    }

    private void addPlayerSuppliers() {
        putCachingSupplier(AnalysisKeys.PLAYER_NAMES, () -> serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>())
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
                        .map(dateObj -> formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.ALL_TIME_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.OPERATORS, () -> serverContainer.getValue(ServerKeys.OPERATORS).map(List::size).orElse(0));
        putSupplier(AnalysisKeys.PLAYERS_TABLE, () ->
                tables.playerTableForServerPage(getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).all()).parseHtml()
        );
        putSupplier(AnalysisKeys.PING_TABLE, () ->
                tables.pingTable(
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
        putCachingSupplier(newDay, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(newWeek, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(newMonth, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(uniqueDay, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(uniqueWeek, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(uniqueMonth, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putSupplier(AnalysisKeys.PLAYERS_NEW_DAY, () -> getUnsafe(newDay).count());
        putSupplier(AnalysisKeys.PLAYERS_NEW_WEEK, () -> getUnsafe(newWeek).count());
        putSupplier(AnalysisKeys.PLAYERS_NEW_MONTH, () -> getUnsafe(newMonth).count());
        putSupplier(AnalysisKeys.PLAYERS_DAY, () -> getUnsafe(uniqueDay).count());
        putSupplier(AnalysisKeys.PLAYERS_WEEK, () -> getUnsafe(uniqueWeek).count());
        putSupplier(AnalysisKeys.PLAYERS_MONTH, () -> getUnsafe(uniqueMonth).count());
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).averageNewPerDay(timeZone));
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW_DAY, () -> getUnsafe(newDay).averageNewPerDay(timeZone));
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW_WEEK, () -> getUnsafe(newWeek).averageNewPerDay(timeZone));
        putSupplier(AnalysisKeys.AVG_PLAYERS_NEW_MONTH, () -> getUnsafe(newMonth).averageNewPerDay(timeZone));

        putSupplier(AnalysisKeys.UNIQUE_PLAYERS_PER_DAY, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).uniqueJoinsPerDay(timeZone));
        putSupplier(AnalysisKeys.NEW_PLAYERS_PER_DAY, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).newPerDay(timeZone));
        putSupplier(AnalysisKeys.UNIQUE_PLAYERS_SERIES, () -> graphs.line().lineGraph(
                MutatorFunctions.toPointsWithRemovedOffset(getUnsafe(AnalysisKeys.UNIQUE_PLAYERS_PER_DAY), timeZone)).toHighChartsSeries()
        );
        putSupplier(AnalysisKeys.NEW_PLAYERS_SERIES, () -> graphs.line().lineGraph(
                MutatorFunctions.toPointsWithRemovedOffset(getUnsafe(AnalysisKeys.NEW_PLAYERS_PER_DAY), timeZone)).toHighChartsSeries()
        );

        Key<Integer> retentionDay = new Key<>(Integer.class, "RETENTION_DAY");
        // compareAndFindThoseLikelyToBeRetained can throw exception.
        putCachingSupplier(retentionDay, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).compareAndFindThoseLikelyToBeRetained(
                getUnsafe(newDay).all(), getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO),
                getUnsafe(AnalysisKeys.PLAYERS_ONLINE_RESOLVER),
                config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD)
                ).count()
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_DAY, () -> {
            try {
                return getUnsafe(retentionDay);
            } catch (IllegalStateException noPlayersAfterDateFiltering) {
                return 0;
            }
        });
        putCachingSupplier(AnalysisKeys.PLAYERS_RETAINED_WEEK, () ->
                getUnsafe(newWeek).filterRetained(
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO),
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME)
                ).count()
        );
        putCachingSupplier(AnalysisKeys.PLAYERS_RETAINED_MONTH, () ->
                getUnsafe(newMonth).filterRetained(
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO),
                        getUnsafe(AnalysisKeys.ANALYSIS_TIME)
                ).count()
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_DAY_PERC, () -> {
            try {
                Integer playersNewDay = getUnsafe(AnalysisKeys.PLAYERS_NEW_DAY);
                return playersNewDay != 0
                        ? formatters.percentage().apply(1.0 * getUnsafe(retentionDay) / playersNewDay)
                        : "-";
            } catch (IllegalStateException noPlayersAfterDateFiltering) {
                return "Not enough data";
            }
        });
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_WEEK_PERC, () -> {
                    Integer playersNewWeek = getUnsafe(AnalysisKeys.PLAYERS_NEW_WEEK);
                    return playersNewWeek != 0 ? formatters.percentage().apply(1.0 * getUnsafe(AnalysisKeys.PLAYERS_RETAINED_WEEK) / playersNewWeek) : "-";
                }
        );
        putSupplier(AnalysisKeys.PLAYERS_RETAINED_MONTH_PERC, () -> {
                    Integer playersNewMonth = getUnsafe(AnalysisKeys.PLAYERS_NEW_MONTH);
                    return playersNewMonth != 0
                            ? formatters.percentage().apply(1.0 * getUnsafe(AnalysisKeys.PLAYERS_RETAINED_MONTH) / playersNewMonth)
                            : "-";
                }
        );
    }

    private void addSessionSuppliers() {
        Key<SessionAccordion> sessionAccordion = new Key<>(SessionAccordion.class, "SESSION_ACCORDION");
        putCachingSupplier(sessionAccordion, () -> accordions.serverSessionAccordion(
                getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).all(),
                () -> Collections.singletonMap(
                        serverContainer.getUnsafe(ServerKeys.SERVER_UUID),
                        serverContainer.getValue(ServerKeys.NAME).orElse("This server")
                ),
                () -> getUnsafe(AnalysisKeys.PLAYER_NAMES)
        ));
        putSupplier(AnalysisKeys.SESSION_ACCORDION_HTML, () -> getUnsafe(sessionAccordion).toHtml());
        putSupplier(AnalysisKeys.SESSION_ACCORDION_FUNCTIONS, () -> getUnsafe(sessionAccordion).toViewScript());

        putSupplier(AnalysisKeys.RECENT_LOGINS, () -> new RecentLoginList(
                serverContainer.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>()),
                formatters.secondLong()).toHtml()
        );
        putSupplier(AnalysisKeys.SESSION_TABLE, () -> tables.serverSessionTable(
                getUnsafe(AnalysisKeys.PLAYER_NAMES), getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).all()).parseHtml()
        );

        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F,
                () -> formatters.timeAmount().apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );
        putSupplier(AnalysisKeys.SESSION_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).count());
        putSupplier(AnalysisKeys.PLAYTIME_TOTAL, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlaytime());
        putSupplier(AnalysisKeys.DEATHS, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toDeathCount());
        putSupplier(AnalysisKeys.MOB_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toMobKillCount());
        putSupplier(AnalysisKeys.PLAYER_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlayerKillCount());
        putSupplier(AnalysisKeys.PLAYTIME_F,
                () -> formatters.timeAmount().apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL))
        );
        putSupplier(AnalysisKeys.AVERAGE_PLAYTIME_F, () -> {
                    long players = getUnsafe(AnalysisKeys.PLAYERS_TOTAL);
                    return players != 0
                            ? formatters.timeAmount().apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL) / players)
                            : "-";
                }
        );
        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F,
                () -> formatters.timeAmount().apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );

        Key<SessionsMutator> sessionsDay = new Key<>(SessionsMutator.class, "SESSIONS_DAY");
        Key<SessionsMutator> sessionsWeek = new Key<>(SessionsMutator.class, "SESSIONS_WEEK");
        Key<SessionsMutator> sessionsMonth = new Key<>(SessionsMutator.class, "SESSIONS_MONTH");
        putCachingSupplier(sessionsDay, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR)
                .filterSessionsBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(sessionsWeek, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR)
                .filterSessionsBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(sessionsMonth, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR)
                .filterSessionsBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putSupplier(AnalysisKeys.PUNCHCARD_SERIES, () -> graphs.special().punchCard(getUnsafe(sessionsMonth).all()).toHighChartsSeries());
        putSupplier(AnalysisKeys.AVG_PLAYERS, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageUniqueJoinsPerDay(timeZone));
        putSupplier(AnalysisKeys.AVG_PLAYERS_DAY, () -> getUnsafe(sessionsDay).toAverageUniqueJoinsPerDay(timeZone));
        putSupplier(AnalysisKeys.AVG_PLAYERS_WEEK, () -> getUnsafe(sessionsWeek).toAverageUniqueJoinsPerDay(timeZone));
        putSupplier(AnalysisKeys.AVG_PLAYERS_MONTH, () -> getUnsafe(sessionsMonth).toAverageUniqueJoinsPerDay(timeZone));
    }

    private void addGraphSuppliers() {
        Key<WorldPie> worldPie = new Key<>(WorldPie.class, "WORLD_PIE");
        putCachingSupplier(worldPie, () -> graphs.pie().worldPie(
                serverContainer.getValue(ServerKeys.WORLD_TIMES).orElse(new WorldTimes())
        ));
        putSupplier(AnalysisKeys.WORLD_PIE_SERIES, () -> getUnsafe(worldPie).toHighChartsSeries());
        putSupplier(AnalysisKeys.GM_PIE_SERIES, () -> getUnsafe(worldPie).toHighChartsDrilldown());
        putSupplier(AnalysisKeys.PLAYERS_ONLINE_SERIES, () ->
                graphs.line().playersOnlineGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries()
        );
        putSupplier(AnalysisKeys.TPS_SERIES, () -> graphs.line().tpsGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.CPU_SERIES, () -> graphs.line().cpuGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.RAM_SERIES, () -> graphs.line().ramGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.DISK_SERIES, () -> graphs.line().diskGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.ENTITY_SERIES, () -> graphs.line().entityGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.CHUNK_SERIES, () -> graphs.line().chunkGraph(getUnsafe(AnalysisKeys.TPS_MUTATOR)).toHighChartsSeries());
        putSupplier(AnalysisKeys.WORLD_MAP_SERIES, () ->
                graphs.special().worldMap(getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)).toHighChartsSeries()
        );
        Key<BarGraph> geolocationBarChart = new Key<>(BarGraph.class, "GEOLOCATION_BAR_GRAPH");
        putCachingSupplier(geolocationBarChart, () -> graphs.bar().geolocationBarGraph(getUnsafe(AnalysisKeys.PLAYERS_MUTATOR)));
        putSupplier(AnalysisKeys.COUNTRY_CATEGORIES, () -> getUnsafe(geolocationBarChart).toHighChartsCategories());
        putSupplier(AnalysisKeys.COUNTRY_SERIES, () -> getUnsafe(geolocationBarChart).toHighChartsSeries());

        Key<PingGraph> pingGraph = new Key<>(PingGraph.class, "PING_GRAPH");
        putCachingSupplier(pingGraph, () ->
                graphs.line().pingGraph(PingMutator.forContainer(serverContainer).mutateToByMinutePings().all())
        );
        putSupplier(AnalysisKeys.AVG_PING_SERIES, () -> getUnsafe(pingGraph).toAvgSeries());
        putSupplier(AnalysisKeys.MAX_PING_SERIES, () -> getUnsafe(pingGraph).toMaxSeries());
        putSupplier(AnalysisKeys.MIN_PING_SERIES, () -> getUnsafe(pingGraph).toMinSeries());

        putSupplier(AnalysisKeys.CALENDAR_SERIES, () -> graphs.calendar().serverCalendar(
                getUnsafe(AnalysisKeys.PLAYERS_MUTATOR),
                getUnsafe(AnalysisKeys.UNIQUE_PLAYERS_PER_DAY),
                getUnsafe(AnalysisKeys.NEW_PLAYERS_PER_DAY)
        ).toCalendarSeries());

        putCachingSupplier(AnalysisKeys.ACTIVITY_DATA, () -> getUnsafe(AnalysisKeys.PLAYERS_MUTATOR).toActivityDataMap(getUnsafe(AnalysisKeys.ANALYSIS_TIME), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD)));
        Key<StackGraph> activityStackGraph = new Key<>(StackGraph.class, "ACTIVITY_STACK_GRAPH");
        putCachingSupplier(activityStackGraph, () -> graphs.stack().activityStackGraph(getUnsafe(AnalysisKeys.ACTIVITY_DATA)));
        putSupplier(AnalysisKeys.ACTIVITY_STACK_CATEGORIES, () -> getUnsafe(activityStackGraph).toHighChartsLabels());
        putSupplier(AnalysisKeys.ACTIVITY_STACK_SERIES, () -> getUnsafe(activityStackGraph).toHighChartsSeries());
        putSupplier(AnalysisKeys.ACTIVITY_PIE_SERIES, () -> graphs.pie().activityPie_old(
                getUnsafe(AnalysisKeys.ACTIVITY_DATA).get(getUnsafe(AnalysisKeys.ANALYSIS_TIME))).toHighChartsSeries()
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

        putCachingSupplier(tpsMonth, () -> getUnsafe(AnalysisKeys.TPS_MUTATOR)
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(tpsWeek, () -> getUnsafe(AnalysisKeys.TPS_MUTATOR)
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );
        putCachingSupplier(tpsDay, () -> getUnsafe(AnalysisKeys.TPS_MUTATOR)
                .filterDataBetween(getUnsafe(AnalysisKeys.ANALYSIS_TIME_DAY_AGO), getUnsafe(AnalysisKeys.ANALYSIS_TIME))
        );

        putCachingSupplier(AnalysisKeys.PLAYERS_ONLINE_RESOLVER, () -> new PlayersOnlineResolver(getUnsafe(AnalysisKeys.TPS_MUTATOR)));

        int threshold = config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED);

        putSupplier(AnalysisKeys.TPS_SPIKE_MONTH, () -> getUnsafe(tpsMonth).lowTpsSpikeCount(threshold));
        putSupplier(AnalysisKeys.AVG_TPS_MONTH, () -> getUnsafe(tpsMonth).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_MONTH, () -> getUnsafe(tpsMonth).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_MONTH, () -> getUnsafe(tpsMonth).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_MONTH, () -> getUnsafe(tpsMonth).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_MONTH, () -> getUnsafe(tpsMonth).averageChunks());
        putSupplier(AnalysisKeys.AVG_FREE_DISK_MONTH, () -> getUnsafe(tpsMonth).averageFreeDisk());
        putSupplier(AnalysisKeys.MAX_FREE_DISK_MONTH, () -> getUnsafe(tpsMonth).maxFreeDisk());
        putSupplier(AnalysisKeys.MIN_FREE_DISK_MONTH, () -> getUnsafe(tpsMonth).minFreeDisk());

        putSupplier(AnalysisKeys.TPS_SPIKE_WEEK, () -> getUnsafe(tpsWeek).lowTpsSpikeCount(threshold));
        putSupplier(AnalysisKeys.AVG_TPS_WEEK, () -> getUnsafe(tpsWeek).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_WEEK, () -> getUnsafe(tpsWeek).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_WEEK, () -> getUnsafe(tpsWeek).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_WEEK, () -> getUnsafe(tpsWeek).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_WEEK, () -> getUnsafe(tpsWeek).averageChunks());
        putSupplier(AnalysisKeys.AVG_FREE_DISK_WEEK, () -> getUnsafe(tpsWeek).averageFreeDisk());
        putSupplier(AnalysisKeys.MAX_FREE_DISK_WEEK, () -> getUnsafe(tpsWeek).maxFreeDisk());
        putSupplier(AnalysisKeys.MIN_FREE_DISK_WEEK, () -> getUnsafe(tpsWeek).minFreeDisk());

        putSupplier(AnalysisKeys.TPS_SPIKE_DAY, () -> getUnsafe(tpsDay).lowTpsSpikeCount(threshold));
        putSupplier(AnalysisKeys.AVG_TPS_DAY, () -> getUnsafe(tpsDay).averageTPS());
        putSupplier(AnalysisKeys.AVG_CPU_DAY, () -> getUnsafe(tpsDay).averageCPU());
        putSupplier(AnalysisKeys.AVG_RAM_DAY, () -> getUnsafe(tpsDay).averageRAM());
        putSupplier(AnalysisKeys.AVG_ENTITY_DAY, () -> getUnsafe(tpsDay).averageEntities());
        putSupplier(AnalysisKeys.AVG_CHUNK_DAY, () -> getUnsafe(tpsDay).averageChunks());
        putSupplier(AnalysisKeys.AVG_FREE_DISK_DAY, () -> getUnsafe(tpsDay).averageFreeDisk());
        putSupplier(AnalysisKeys.MAX_FREE_DISK_DAY, () -> getUnsafe(tpsDay).maxFreeDisk());
        putSupplier(AnalysisKeys.MIN_FREE_DISK_DAY, () -> getUnsafe(tpsDay).minFreeDisk());
    }

    private void addCommandSuppliers() {
        putSupplier(AnalysisKeys.COMMAND_USAGE_TABLE, () -> tables.commandUseTable(serverContainer).parseHtml());
        putSupplier(AnalysisKeys.COMMAND_COUNT_UNIQUE, () -> serverContainer.getValue(ServerKeys.COMMAND_USAGE).map(Map::size).orElse(0));
        putSupplier(AnalysisKeys.COMMAND_COUNT, () -> CommandUseMutator.forContainer(serverContainer).commandUsageCount());
    }

    private void addServerHealth() {
        Key<HealthInformation> healthInformation = new Key<>(HealthInformation.class, "HEALTH_INFORMATION");
        putCachingSupplier(healthInformation, () -> new HealthInformation(
                this,
                locale,
                config.get(DisplaySettings.GRAPH_TPS_THRESHOLD_MED),
                config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD),
                formatters.timeAmount(), formatters.decimals(), formatters.percentage()
        ));
        putSupplier(AnalysisKeys.HEALTH_INDEX, () -> getUnsafe(healthInformation).getServerHealth());
        putSupplier(AnalysisKeys.HEALTH_NOTES, () -> getUnsafe(healthInformation).toHtml());
    }

    private void addPluginSuppliers() {
        // TODO Refactor into a system that supports running the analysis on Bungee
        Key<String[]> navAndTabs = new Key<>(new Type<String[]>() {}, "NAV_AND_TABS");
        Key<AnalysisPluginTabs> pluginTabs = new Key<>(AnalysisPluginTabs.class, "PLUGIN_TABS");
        putCachingSupplier(navAndTabs, () -> pluginsTabContentCreator.createContent(
                this, getValue(AnalysisKeys.PLAYERS_MUTATOR).orElse(new PlayersMutator(new ArrayList<>()))
        ));
        putCachingSupplier(pluginTabs, () -> new AnalysisPluginTabs(serverContainer.getValue(ServerKeys.EXTENSION_DATA).orElse(new ArrayList<>()), formatters));
        putSupplier(AnalysisKeys.PLUGINS_TAB_NAV, () -> getUnsafe(pluginTabs).getNav() + getUnsafe(navAndTabs)[0]);
        putSupplier(AnalysisKeys.PLUGINS_TAB, () -> getUnsafe(pluginTabs).getTabs() + getUnsafe(navAndTabs)[1]);
    }

    @Singleton
    public static class Factory {

        private final String version;
        private final PlanConfig config;
        private final Locale locale;
        private final Theme theme;
        private final ServerProperties serverProperties;
        private final Formatters formatters;
        private final Graphs graphs;
        private final HtmlTables tables;
        private final Accordions accordions;
        private final AnalysisPluginsTabContentCreator pluginsTabContentCreator;

        @Inject
        public Factory(
                @Named("currentVersion") String version,
                PlanConfig config,
                Locale locale,
                Theme theme,
                ServerProperties serverProperties,
                Formatters formatters,
                Graphs graphs,
                HtmlTables tables,
                Accordions accordions,
                AnalysisPluginsTabContentCreator pluginsTabContentCreator
        ) {
            this.version = version;
            this.config = config;
            this.locale = locale;
            this.theme = theme;
            this.serverProperties = serverProperties;
            this.formatters = formatters;
            this.graphs = graphs;
            this.tables = tables;
            this.accordions = accordions;
            this.pluginsTabContentCreator = pluginsTabContentCreator;
        }

        public AnalysisContainer forServerContainer(ServerContainer serverContainer) {
            return new AnalysisContainer(
                    serverContainer,
                    version,
                    locale,
                    config,
                    theme,
                    serverProperties,
                    formatters,
                    graphs,
                    tables,
                    accordions,
                    pluginsTabContentCreator
            );
        }
    }
}