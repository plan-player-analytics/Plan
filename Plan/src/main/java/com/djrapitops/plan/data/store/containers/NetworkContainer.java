package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.mutators.health.NetworkHealthInformation;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.bar.BarGraph;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraph;
import com.djrapitops.plan.utilities.html.structure.NetworkServerBox;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // TODO
    private String version;
    private PlanConfig config;
    private Theme theme;
    private Database database;
    private ServerProperties serverProperties;
    private Formatters formatters;
    private Graphs graphs;

    public NetworkContainer(ServerContainer bungeeContainer) {
        this.bungeeContainer = bungeeContainer;

        putSupplier(NetworkKeys.PLAYERS_MUTATOR, () -> PlayersMutator.forContainer(bungeeContainer));

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
            for (Server server : getValue(NetworkKeys.BUKKIT_SERVERS).orElse(new ArrayList<>())) {
                TPSMutator tpsMutator = new TPSMutator(playersOnlineData.getOrDefault(server.getId(), new ArrayList<>()));

                // TODO Add Registered players per server.
                NetworkServerBox serverBox = new NetworkServerBox(server, 0, tpsMutator, graphs);
                serverBoxes.append(serverBox.toHtml());
            }
            return serverBoxes.toString();
        });
    }

    private void addNetworkHealth() {
        Key<NetworkHealthInformation> healthInformation = new Key<>(NetworkHealthInformation.class, "HEALTH_INFORMATION");
        putSupplier(healthInformation, () -> new NetworkHealthInformation(
                this,
                config.getNumber(Settings.ACTIVE_PLAY_THRESHOLD),
                config.getNumber(Settings.ACTIVE_LOGIN_THRESHOLD),
                formatters.timeAmount(), formatters.decimals(), formatters.percentage()
        ));
        putSupplier(NetworkKeys.HEALTH_INDEX, () -> getUnsafe(healthInformation).getServerHealth());
        putSupplier(NetworkKeys.HEALTH_NOTES, () -> getUnsafe(healthInformation).toHtml());
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

        putSupplier(NetworkKeys.NETWORK_NAME, () ->
                Check.isBungeeAvailable() ?
                        config.getString(Settings.BUNGEE_NETWORK_NAME) :
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
                graphs.special().worldMap(PlayersMutator.forContainer(bungeeContainer)).toHighChartsSeries()
        );
        Key<BarGraph> geolocationBarChart = new Key<>(BarGraph.class, "GEOLOCATION_BAR_GRAPH");
        putSupplier(geolocationBarChart, () -> graphs.bar().geolocationBarGraph(getUnsafe(NetworkKeys.PLAYERS_MUTATOR)));
        putSupplier(NetworkKeys.COUNTRY_CATEGORIES, () -> getUnsafe(geolocationBarChart).toHighChartsCategories());
        putSupplier(NetworkKeys.COUNTRY_SERIES, () -> getUnsafe(geolocationBarChart).toHighChartsSeries());

        putSupplier(NetworkKeys.PLAYERS_ONLINE_SERIES, () ->
                graphs.line().playersOnlineGraph(TPSMutator.forContainer(bungeeContainer)).toHighChartsSeries()
        );
        Key<StackGraph> activityStackGraph = new Key<>(StackGraph.class, "ACTIVITY_STACK_GRAPH");
        putSupplier(NetworkKeys.ACTIVITY_DATA, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR).toActivityDataMap(getUnsafe(NetworkKeys.REFRESH_TIME), config.getNumber(Settings.ACTIVE_PLAY_THRESHOLD), config.getNumber(Settings.ACTIVE_LOGIN_THRESHOLD)));
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
        putSupplier(newDay, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(NetworkKeys.REFRESH_TIME_DAY_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putSupplier(newWeek, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(NetworkKeys.REFRESH_TIME_WEEK_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putSupplier(newMonth, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterRegisteredBetween(getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putSupplier(uniqueDay, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(NetworkKeys.REFRESH_TIME_DAY_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putSupplier(uniqueWeek, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(NetworkKeys.REFRESH_TIME_WEEK_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );
        putSupplier(uniqueMonth, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR)
                .filterPlayedBetween(getUnsafe(NetworkKeys.REFRESH_TIME_MONTH_AGO), getUnsafe(NetworkKeys.REFRESH_TIME))
        );

        putSupplier(NetworkKeys.PLAYERS_NEW_DAY, () -> getUnsafe(newDay).count());
        putSupplier(NetworkKeys.PLAYERS_NEW_WEEK, () -> getUnsafe(newWeek).count());
        putSupplier(NetworkKeys.PLAYERS_NEW_MONTH, () -> getUnsafe(newMonth).count());
        putSupplier(NetworkKeys.PLAYERS_DAY, () -> getUnsafe(uniqueDay).count());
        putSupplier(NetworkKeys.PLAYERS_WEEK, () -> getUnsafe(uniqueWeek).count());
        putSupplier(NetworkKeys.PLAYERS_MONTH, () -> getUnsafe(uniqueMonth).count());
    }

}