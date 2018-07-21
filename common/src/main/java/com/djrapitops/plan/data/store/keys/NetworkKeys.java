package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.info.server.Server;

import java.util.*;

/**
 * Key objects for {@link com.djrapitops.plan.data.store.containers.NetworkContainer}.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.containers.NetworkContainer for DataContainer.
 */
public class NetworkKeys {

    public static final PlaceholderKey<String> VERSION = CommonPlaceholderKeys.VERSION;
    public static final PlaceholderKey<String> NETWORK_NAME = new PlaceholderKey<>(String.class, "networkName");
    public static final PlaceholderKey<Integer> TIME_ZONE = CommonPlaceholderKeys.TIME_ZONE;
    public static final PlaceholderKey<Integer> PLAYERS_ONLINE = CommonPlaceholderKeys.PLAYERS_ONLINE;
    public static final PlaceholderKey<Integer> PLAYERS_TOTAL = CommonPlaceholderKeys.PLAYERS_TOTAL;
    public static final PlaceholderKey<String> PLAYERS_GRAPH_COLOR = CommonPlaceholderKeys.PLAYERS_GRAPH_COLOR;
    public static final PlaceholderKey<String> WORLD_MAP_HIGH_COLOR = CommonPlaceholderKeys.WORLD_MAP_HIGH_COLOR;
    public static final PlaceholderKey<String> WORLD_MAP_LOW_COLOR = CommonPlaceholderKeys.WORLD_MAP_LOW_COLOR;

    public static final PlaceholderKey<String> REFRESH_TIME_F = CommonPlaceholderKeys.REFRESH_TIME_F;
    public static final PlaceholderKey<String> RECENT_PEAK_TIME_F = CommonPlaceholderKeys.LAST_PEAK_TIME_F;
    public static final PlaceholderKey<String> ALL_TIME_PEAK_TIME_F = CommonPlaceholderKeys.ALL_TIME_PEAK_TIME_F;
    public static final PlaceholderKey<String> PLAYERS_RECENT_PEAK = CommonPlaceholderKeys.PLAYERS_LAST_PEAK;
    public static final PlaceholderKey<String> PLAYERS_ALL_TIME_PEAK = CommonPlaceholderKeys.PLAYERS_ALL_TIME_PEAK;
    public static final PlaceholderKey<Integer> PLAYERS_DAY = CommonPlaceholderKeys.PLAYERS_DAY;
    public static final PlaceholderKey<Integer> PLAYERS_WEEK = CommonPlaceholderKeys.PLAYERS_WEEK;
    public static final PlaceholderKey<Integer> PLAYERS_MONTH = CommonPlaceholderKeys.PLAYERS_MONTH;
    public static final PlaceholderKey<Integer> PLAYERS_NEW_DAY = CommonPlaceholderKeys.PLAYERS_NEW_DAY;
    public static final PlaceholderKey<Integer> PLAYERS_NEW_WEEK = CommonPlaceholderKeys.PLAYERS_NEW_WEEK;
    public static final PlaceholderKey<Integer> PLAYERS_NEW_MONTH = CommonPlaceholderKeys.PLAYERS_NEW_MONTH;

    public static final PlaceholderKey<String> WORLD_MAP_SERIES = CommonPlaceholderKeys.WORLD_MAP_SERIES;
    public static final PlaceholderKey<String> PLAYERS_ONLINE_SERIES = CommonPlaceholderKeys.PLAYERS_ONLINE_SERIES;
    public static final PlaceholderKey<String> ACTIVITY_STACK_SERIES = CommonPlaceholderKeys.ACTIVITY_STACK_SERIES;
    public static final PlaceholderKey<String> ACTIVITY_STACK_CATEGORIES = CommonPlaceholderKeys.ACTIVITY_STACK_CATEGORIES;
    public static final PlaceholderKey<String> ACTIVITY_PIE_SERIES = CommonPlaceholderKeys.ACTIVITY_PIE_SERIES;
    public static final PlaceholderKey<String> COUNTRY_CATEGORIES = CommonPlaceholderKeys.COUNTRY_CATEGORIES;
    public static final PlaceholderKey<String> COUNTRY_SERIES = CommonPlaceholderKeys.COUNTRY_SERIES;
    public static final PlaceholderKey<Double> HEALTH_INDEX = CommonPlaceholderKeys.HEALTH_INDEX;
    public static final PlaceholderKey<String> HEALTH_NOTES = CommonPlaceholderKeys.HEALTH_NOTES;

    public static final Key<Long> REFRESH_TIME = new Key<>(Long.class, "REFRESH_TIME");
    public static final Key<Long> REFRESH_TIME_DAY_AGO = new Key<>(Long.class, "REFRESH_TIME_DAY_AGO");
    public static final Key<Long> REFRESH_TIME_WEEK_AGO = new Key<>(Long.class, "REFRESH_TIME_WEEK_AGO");
    public static final Key<Long> REFRESH_TIME_MONTH_AGO = new Key<>(Long.class, "REFRESH_TIME_MONTH_AGO");
    public static final Key<PlayersMutator> PLAYERS_MUTATOR = CommonKeys.PLAYERS_MUTATOR;

    public static final Key<Collection<Server>> BUKKIT_SERVERS = new Key<>(new Type<Collection<Server>>() {
    }, "BUKKIT_SERVERS");
    public static final Key<TreeMap<Long, Map<String, Set<UUID>>>> ACTIVITY_DATA = CommonKeys.ACTIVITY_DATA;

    private NetworkKeys() {
        /* static variable class */
    }

}