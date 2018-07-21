package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.mutators.health.NetworkHealthInformation;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.html.graphs.ActivityStackGraph;
import com.djrapitops.plan.utilities.html.graphs.WorldMap;
import com.djrapitops.plan.utilities.html.graphs.bar.GeolocationBarGraph;
import com.djrapitops.plan.utilities.html.graphs.line.OnlineActivityGraph;
import com.djrapitops.plan.utilities.html.graphs.pie.ActivityPie;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * DataContainer for the whole network.
 *
 * @author Rsl1122
 * @see NetworkKeys for Key objects
 * @see PlaceholderKey for placeholder information
 */
public class NetworkContainer extends DataContainer {

    private final ServerContainer bungeeContainer;

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
        try {
            AnalysisContainer analysisContainer = new AnalysisContainer(Database.getActive().fetch().getServerContainer(serverUUID));
            serverContainers.put(serverUUID, analysisContainer);
            return Optional.of(analysisContainer);
        } catch (DBOpException e) {
            Log.toLog(this.getClass(), e);
        }
        return Optional.empty();
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(NetworkKeys.REFRESH_TIME, now);
        putRawData(NetworkKeys.REFRESH_TIME_DAY_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.DAY.ms());
        putRawData(NetworkKeys.REFRESH_TIME_WEEK_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.WEEK.ms());
        putRawData(NetworkKeys.REFRESH_TIME_MONTH_AGO, getUnsafe(NetworkKeys.REFRESH_TIME) - TimeAmount.MONTH.ms());
        putSupplier(NetworkKeys.REFRESH_TIME_F, () -> Formatters.second().apply(() -> getUnsafe(NetworkKeys.REFRESH_TIME)));

        putRawData(NetworkKeys.VERSION, PlanHelper.getInstance().getVersion());
        putSupplier(NetworkKeys.TIME_ZONE, MiscUtils::getTimeZoneOffsetHours);

        putSupplier(NetworkKeys.NETWORK_NAME, () ->
                Check.isBungeeAvailable() ?
                        Settings.BUNGEE_NETWORK_NAME.toString() :
                        bungeeContainer.getValue(ServerKeys.NAME).orElse("Plan")
        );
        putSupplier(NetworkKeys.PLAYERS_ONLINE, ServerInfo.getServerProperties()::getOnlinePlayers);
        putRawData(NetworkKeys.WORLD_MAP_LOW_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_LOW));
        putRawData(NetworkKeys.WORLD_MAP_HIGH_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        putRawData(NetworkKeys.PLAYERS_GRAPH_COLOR, Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
    }

    private void addPlayerInformation() {
        putSupplier(NetworkKeys.PLAYERS_TOTAL, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR).count());
        putSupplier(NetworkKeys.WORLD_MAP_SERIES, () ->
                new WorldMap(PlayersMutator.forContainer(bungeeContainer)).toHighChartsSeries()
        );
        Key<GeolocationBarGraph> geolocationBarChart = new Key<>(GeolocationBarGraph.class, "GEOLOCATION_BAR_CHART");
        putSupplier(geolocationBarChart, () -> new GeolocationBarGraph(getUnsafe(NetworkKeys.PLAYERS_MUTATOR)));
        putSupplier(NetworkKeys.COUNTRY_CATEGORIES, () -> getUnsafe(geolocationBarChart).toHighChartsCategories());
        putSupplier(NetworkKeys.COUNTRY_SERIES, () -> getUnsafe(geolocationBarChart).toHighChartsSeries());

        putSupplier(NetworkKeys.PLAYERS_ONLINE_SERIES, () ->
                new OnlineActivityGraph(TPSMutator.forContainer(bungeeContainer)).toHighChartsSeries()
        );
        Key<ActivityStackGraph> activityStackGraph = new Key<>(ActivityStackGraph.class, "ACTIVITY_STACK_GRAPH");
        putSupplier(NetworkKeys.ACTIVITY_DATA, () -> getUnsafe(NetworkKeys.PLAYERS_MUTATOR).toActivityDataMap(getUnsafe(NetworkKeys.REFRESH_TIME)));
        putSupplier(activityStackGraph, () -> new ActivityStackGraph(getUnsafe(NetworkKeys.ACTIVITY_DATA)));
        putSupplier(NetworkKeys.ACTIVITY_STACK_CATEGORIES, () -> getUnsafe(activityStackGraph).toHighChartsLabels());
        putSupplier(NetworkKeys.ACTIVITY_STACK_SERIES, () -> getUnsafe(activityStackGraph).toHighChartsSeries());
        putSupplier(NetworkKeys.ACTIVITY_PIE_SERIES, () ->
                new ActivityPie(getUnsafe(NetworkKeys.ACTIVITY_DATA).get(getUnsafe(NetworkKeys.REFRESH_TIME))).toHighChartsSeries()
        );

        putSupplier(NetworkKeys.ALL_TIME_PEAK_TIME_F, () ->
                bungeeContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS).map(Formatters.year()::apply).orElse("No data")
        );
        putSupplier(NetworkKeys.RECENT_PEAK_TIME_F, () ->
                bungeeContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS).map(Formatters.year()::apply).orElse("No data")
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