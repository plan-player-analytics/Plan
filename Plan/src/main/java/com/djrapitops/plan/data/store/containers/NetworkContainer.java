package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.mutators.health.NetworkHealthInformation;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.bar.BarGraph;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraph;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private PlanConfig config;
    private Theme theme;
    private Database database;
    private Graphs graphs;

    private Formatter<DateHolder> yearFormatter;
    private Formatter<Long> secondLongFormatter;

    private final Map<UUID, AnalysisContainer> serverContainers;

    public NetworkContainer(ServerContainer bungeeContainer) {
        this.bungeeContainer = bungeeContainer;
        serverContainers = new HashMap<>();

        putSupplier(NetworkKeys.PLAYERS_MUTATOR, () -> PlayersMutator.forContainer(bungeeContainer));

        addConstants();
        addPlayerInformation();
        addNetworkHealth();
    }

    private void addNetworkHealth() {
        Key<NetworkHealthInformation> healthInformation = new Key<>(NetworkHealthInformation.class, "HEALTH_INFORMATION");
        putSupplier(healthInformation, () -> new NetworkHealthInformation(this));
        putSupplier(NetworkKeys.HEALTH_INDEX, () -> getUnsafe(healthInformation).getServerHealth());
        putSupplier(NetworkKeys.HEALTH_NOTES, () -> getUnsafe(healthInformation).toHtml());
    }

    public void putAnalysisContainer(AnalysisContainer analysisContainer) {
        serverContainers.put(analysisContainer.getServerContainer().getUnsafe(ServerKeys.SERVER_UUID), analysisContainer);
    }

    public Optional<AnalysisContainer> getAnalysisContainer(UUID serverUUID) {
        AnalysisContainer container = serverContainers.get(serverUUID);
        if (container != null) {
            return Optional.of(container);
        }
        AnalysisContainer analysisContainer = new AnalysisContainer(database.fetch().getServerContainer(serverUUID));
        serverContainers.put(serverUUID, analysisContainer);
        return Optional.of(analysisContainer);
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(NetworkKeys.REFRESH_TIME, now);
        putRawData(NetworkKeys.REFRESH_TIME_DAY_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.DAY.ms());
        putRawData(NetworkKeys.REFRESH_TIME_WEEK_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.WEEK.ms());
        putRawData(NetworkKeys.REFRESH_TIME_MONTH_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.MONTH.ms());
        putSupplier(NetworkKeys.REFRESH_TIME_F, () -> secondLongFormatter.apply(getUnsafe(NetworkKeys.REFRESH_TIME)));

        putRawData(NetworkKeys.VERSION, PlanPlugin.getInstance().getVersion());
        putSupplier(NetworkKeys.TIME_ZONE, MiscUtils::getTimeZoneOffsetHours);

        putSupplier(NetworkKeys.NETWORK_NAME, () ->
                Check.isBungeeAvailable() ?
                        config.getString(Settings.BUNGEE_NETWORK_NAME) :
                        bungeeContainer.getValue(ServerKeys.NAME).orElse("Plan")
        );
        putSupplier(NetworkKeys.PLAYERS_ONLINE, ServerInfo.getServerProperties_Old()::getOnlinePlayers);
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
        putSupplier(NetworkKeys.ACTIVITY_DATA, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR).toActivityDataMap(getUnsafe(NetworkKeys.REFRESH_TIME)));
        putSupplier(activityStackGraph, () -> graphs.stack().activityStackGraph(getUnsafe(NetworkKeys.ACTIVITY_DATA)));
        putSupplier(NetworkKeys.ACTIVITY_STACK_CATEGORIES, () -> getUnsafe(activityStackGraph).toHighChartsLabels());
        putSupplier(NetworkKeys.ACTIVITY_STACK_SERIES, () -> getUnsafe(activityStackGraph).toHighChartsSeries());
        putSupplier(NetworkKeys.ACTIVITY_PIE_SERIES, () -> graphs.pie().activityPie(
                getUnsafe(NetworkKeys.ACTIVITY_DATA).get(getUnsafe(NetworkKeys.REFRESH_TIME))).toHighChartsSeries()
        );

        putSupplier(NetworkKeys.ALL_TIME_PEAK_TIME_F, () ->
                bungeeContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS).map(yearFormatter::apply).orElse("No data")
        );
        putSupplier(NetworkKeys.RECENT_PEAK_TIME_F, () ->
                bungeeContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS).map(yearFormatter::apply).orElse("No data")
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