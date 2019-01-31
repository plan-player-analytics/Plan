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

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.mutators.health.NetworkHealthInformation;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.ServerAggregateQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.ProxySettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.bar.BarGraph;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraph;
import com.djrapitops.plan.utilities.html.structure.NetworkServerBox;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * DataContainer for the whole network.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.NetworkKeys for Key objects
 * @see com.djrapitops.plan.data.store.PlaceholderKey for placeholder information
 */
public class NetworkContainer extends DataContainer {

    private final ServerContainer bungeeContainer;

    private final String version;
    private final PlanConfig config;
    private final Locale locale;
    private final Theme theme;
    private final ServerProperties serverProperties;
    private final Formatters formatters;
    private final Graphs graphs;
    private final Database database;

    public NetworkContainer(
            ServerContainer bungeeContainer,
            String version,
            PlanConfig config,
            Locale locale,
            Theme theme,
            DBSystem dbSystem,
            ServerProperties serverProperties,
            Formatters formatters,
            Graphs graphs
    ) {
        this.bungeeContainer = bungeeContainer;
        this.version = version;
        this.config = config;
        this.locale = locale;
        this.theme = theme;
        this.database = dbSystem.getDatabase();
        this.serverProperties = serverProperties;
        this.formatters = formatters;
        this.graphs = graphs;

        putCachingSupplier(NetworkKeys.PLAYERS_MUTATOR, () -> PlayersMutator.forContainer(bungeeContainer));

        addConstants();
        addServerBoxes();
        addPlayerInformation();
        addNetworkHealth();
    }

    private void addServerBoxes() {
        putSupplier(NetworkKeys.NETWORK_PLAYER_ONLINE_DATA, () -> database.fetch().getPlayersOnlineForServers(
                getValue(NetworkKeys.BUKKIT_SERVERS).orElse(new ArrayList<>()))
        );
        putSupplier(NetworkKeys.SERVERS_TAB, () -> {
            StringBuilder serverBoxes = new StringBuilder();
            Map<Integer, List<TPS>> playersOnlineData = getValue(NetworkKeys.NETWORK_PLAYER_ONLINE_DATA).orElse(new HashMap<>());
            Map<UUID, Integer> userCounts = database.query(ServerAggregateQueries.serverUserCounts());
            Collection<Server> servers = getValue(NetworkKeys.BUKKIT_SERVERS).orElse(new ArrayList<>());
            servers.stream()
                    .sorted((one, two) -> String.CASE_INSENSITIVE_ORDER.compare(one.getName(), two.getName()))
                    .forEach(server -> {
                        TPSMutator tpsMutator = new TPSMutator(playersOnlineData.getOrDefault(server.getId(), new ArrayList<>()));
                        int registered = userCounts.getOrDefault(server.getUuid(), 0);
                        NetworkServerBox serverBox = new NetworkServerBox(server, registered, tpsMutator, graphs);
                        serverBoxes.append(serverBox.toHtml());
                    });
            if (servers.isEmpty()) {
                serverBoxes.append("<div class=\"row clearfix\">" +
                        "<div class=\"col-xs-12 col-sm-12 col-md-12 col-lg-12\">" +
                        "<div class=\"card\">" +
                        "<div class=\"header\">" +
                        "<div class=\"row clearfix\">" +
                        "<div class=\"col-xs-6 col-sm-6 col-lg-6\">" +
                        "<h2><i class=\"col-light-green fa fa-servers\"></i> No Servers</h2>" +
                        "</div><div class=\"body\">" +
                        "<p>No Bukkit/Sponge servers connected to Plan.</p>" +
                        "</div></div></div></div></div></div>");
            }
            return serverBoxes.toString();
        });
    }

    private void addNetworkHealth() {
        Key<NetworkHealthInformation> healthInformation = new Key<>(NetworkHealthInformation.class, "HEALTH_INFORMATION");
        putCachingSupplier(healthInformation, () -> new NetworkHealthInformation(
                this,
                locale,
                config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD),
                config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD),
                formatters.timeAmount(), formatters.decimals(), formatters.percentage()
        ));
        putCachingSupplier(NetworkKeys.HEALTH_INDEX, () -> getUnsafe(healthInformation).getServerHealth());
        putCachingSupplier(NetworkKeys.HEALTH_NOTES, () -> getUnsafe(healthInformation).toHtml());
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(NetworkKeys.REFRESH_TIME, now);
        putRawData(NetworkKeys.REFRESH_TIME_DAY_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeUnit.DAYS.toMillis(1L));
        putRawData(NetworkKeys.REFRESH_TIME_WEEK_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.WEEK.toMillis(1L));
        putRawData(NetworkKeys.REFRESH_TIME_MONTH_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.MONTH.toMillis(1L));
        putSupplier(NetworkKeys.REFRESH_TIME_F, () -> formatters.secondLong().apply(getUnsafe(NetworkKeys.REFRESH_TIME)));

        putRawData(NetworkKeys.VERSION, version);
        putSupplier(NetworkKeys.TIME_ZONE, config::getTimeZoneOffsetHours);

        putCachingSupplier(NetworkKeys.NETWORK_NAME, () ->
                Check.isBungeeAvailable() || Check.isVelocityAvailable() ?
                        config.get(ProxySettings.NETWORK_NAME) :
                        bungeeContainer.getValue(ServerKeys.NAME).orElse("Plan")
        );
        putSupplier(NetworkKeys.PLAYERS_ONLINE, serverProperties::getOnlinePlayers);
        putRawData(NetworkKeys.WORLD_MAP_LOW_COLOR, theme.getValue(ThemeVal.WORLD_MAP_LOW));
        putRawData(NetworkKeys.WORLD_MAP_HIGH_COLOR, theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        putRawData(NetworkKeys.PLAYERS_GRAPH_COLOR, theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
    }

    private void addPlayerInformation() {
        putSupplier(NetworkKeys.PLAYERS_TOTAL, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR).count());
        putSupplier(NetworkKeys.WORLD_MAP_SERIES, () ->
                graphs.special().worldMap(database.query(ServerAggregateQueries.networkGeolocationCounts())).toHighChartsSeries()
        );
        Key<BarGraph> geolocationBarChart = new Key<>(BarGraph.class, "GEOLOCATION_BAR_GRAPH");
        putSupplier(geolocationBarChart, () -> graphs.bar().geolocationBarGraph(getUnsafe(NetworkKeys.PLAYERS_MUTATOR)));
        putSupplier(NetworkKeys.COUNTRY_CATEGORIES, () -> getUnsafe(geolocationBarChart).toHighChartsCategories());
        putSupplier(NetworkKeys.COUNTRY_SERIES, () -> getUnsafe(geolocationBarChart).toHighChartsSeries());

        putSupplier(NetworkKeys.PLAYERS_ONLINE_SERIES, () ->
                graphs.line().playersOnlineGraph(TPSMutator.forContainer(bungeeContainer)).toHighChartsSeries()
        );
        Key<StackGraph> activityStackGraph = new Key<>(StackGraph.class, "ACTIVITY_STACK_GRAPH");
        putSupplier(NetworkKeys.ACTIVITY_DATA, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR).toActivityDataMap(getUnsafe(NetworkKeys.REFRESH_TIME), config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD), config.get(TimeSettings.ACTIVE_LOGIN_THRESHOLD)));
        putSupplier(activityStackGraph, () -> graphs.stack().activityStackGraph(getUnsafe(NetworkKeys.ACTIVITY_DATA)));
        putSupplier(NetworkKeys.ACTIVITY_STACK_CATEGORIES, () -> getUnsafe(activityStackGraph).toHighChartsLabels());
        putSupplier(NetworkKeys.ACTIVITY_STACK_SERIES, () -> getUnsafe(activityStackGraph).toHighChartsSeries());
        putSupplier(NetworkKeys.ACTIVITY_PIE_SERIES, () -> graphs.pie().activityPie(
                getUnsafe(NetworkKeys.ACTIVITY_DATA).get(getUnsafe(NetworkKeys.REFRESH_TIME))).toHighChartsSeries()
        );

        putSupplier(NetworkKeys.ALL_TIME_PEAK_TIME_F, () ->
                bungeeContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS).map(formatters.year()).orElse("No data")
        );
        putSupplier(NetworkKeys.RECENT_PEAK_TIME_F, () ->
                bungeeContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS).map(formatters.year()).orElse("No data")
        );
        putSupplier(NetworkKeys.PLAYERS_ALL_TIME_PEAK, () ->
                bungeeContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS).map(dateObj -> "" + dateObj.getValue()).orElse("-")
        );
        putSupplier(NetworkKeys.PLAYERS_RECENT_PEAK, () ->
                bungeeContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS).map(dateObj -> "" + dateObj.getValue()).orElse("-")
        );

        addPlayerCounts();
    }

    private void addPlayerCounts() {
        Key<PlayersMutator> newDay = new Key<>(PlayersMutator.class, "NEW_DAY");
        Key<PlayersMutator> newWeek = new Key<>(PlayersMutator.class, "NEW_WEEK");
        Key<PlayersMutator> newMonth = new Key<>(PlayersMutator.class, "NEW_MONTH");
        Key<PlayersMutator> uniqueDay = new Key<>(PlayersMutator.class, "UNIQUE_DAY");
        Key<PlayersMutator> uniqueWeek = new Key<>(PlayersMutator.class, "UNIQUE_WEEK");
        Key<PlayersMutator> uniqueMonth = new Key<>(PlayersMutator.class, "UNIQUE_MONTH");
        putCachingSupplier(newDay, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(NetworkKeys.REFRESH_TIME_DAY_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putCachingSupplier(newWeek, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(NetworkKeys.REFRESH_TIME_WEEK_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putCachingSupplier(newMonth, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putCachingSupplier(uniqueDay, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(NetworkKeys.REFRESH_TIME_DAY_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putCachingSupplier(uniqueWeek, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(NetworkKeys.REFRESH_TIME_WEEK_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putCachingSupplier(uniqueMonth, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );

        putSupplier(NetworkKeys.PLAYERS_NEW_DAY, () -> getUnsafe(newDay).count());
        putSupplier(NetworkKeys.PLAYERS_NEW_WEEK, () -> getUnsafe(newWeek).count());
        putSupplier(NetworkKeys.PLAYERS_NEW_MONTH, () -> getUnsafe(newMonth).count());
        putSupplier(NetworkKeys.PLAYERS_DAY, () -> getUnsafe(uniqueDay).count());
        putSupplier(NetworkKeys.PLAYERS_WEEK, () -> getUnsafe(uniqueWeek).count());
        putSupplier(NetworkKeys.PLAYERS_MONTH, () -> getUnsafe(uniqueMonth).count());
    }

    public ServerContainer getBungeeContainer() {
        return bungeeContainer;
    }

    @Singleton
    public static class Factory {

        private final Lazy<String> version;
        private final Lazy<PlanConfig> config;
        private final Lazy<Locale> locale;
        private final Lazy<Theme> theme;
        private final Lazy<DBSystem> dbSystem;
        private final Lazy<ServerProperties> serverProperties;
        private final Lazy<Formatters> formatters;
        private final Lazy<Graphs> graphs;

        @Inject
        public Factory(
                @Named("currentVersion") Lazy<String> version,
                Lazy<PlanConfig> config,
                Lazy<Locale> locale,
                Lazy<Theme> theme,
                Lazy<DBSystem> dbSystem,
                Lazy<ServerProperties> serverProperties,
                Lazy<Formatters> formatters,
                Lazy<Graphs> graphs
        ) {
            this.version = version;
            this.config = config;
            this.locale = locale;
            this.theme = theme;
            this.dbSystem = dbSystem;
            this.serverProperties = serverProperties;
            this.formatters = formatters;
            this.graphs = graphs;
        }

        public NetworkContainer forBungeeContainer(ServerContainer bungeeContainer) {
            return new NetworkContainer(
                    bungeeContainer,
                    version.get(),
                    config.get(),
                    locale.get(),
                    theme.get(),
                    dbSystem.get(),
                    serverProperties.get(),
                    formatters.get(),
                    graphs.get()
            );
        }
    }

}