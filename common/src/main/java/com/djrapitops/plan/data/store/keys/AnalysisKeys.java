package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.Type;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.PlayersOnlineResolver;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Key objects used for Analysis.
 * <p>
 * PlaceholderKey objects can be used for directly replacing a value on the html.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.containers.AnalysisContainer for Suppliers for each Key.
 */
public class AnalysisKeys {

    // Constants (Affected only by config settings)
    public static final PlaceholderKey<String> VERSION = CommonPlaceholderKeys.VERSION;
    public static final PlaceholderKey<String> SERVER_NAME = new PlaceholderKey<>(String.class, "serverName");
    public static final PlaceholderKey<Integer> TIME_ZONE = CommonPlaceholderKeys.TIME_ZONE;
    public static final PlaceholderKey<Integer> FIRST_DAY = new PlaceholderKey<>(Integer.class, "firstDay");
    public static final PlaceholderKey<Integer> TPS_MEDIUM = new PlaceholderKey<>(Integer.class, "tpsMedium");
    public static final PlaceholderKey<Integer> TPS_HIGH = new PlaceholderKey<>(Integer.class, "tpsHigh");
    public static final PlaceholderKey<Integer> PLAYERS_MAX = new PlaceholderKey<>(Integer.class, "playersMax");
    public static final PlaceholderKey<Integer> PLAYERS_ONLINE = CommonPlaceholderKeys.PLAYERS_ONLINE;
    public static final PlaceholderKey<Integer> PLAYERS_TOTAL = CommonPlaceholderKeys.PLAYERS_TOTAL;
    //
    public static final PlaceholderKey<String> WORLD_PIE_COLORS = new PlaceholderKey<>(String.class, "worldPieColors");
    public static final PlaceholderKey<String> GM_PIE_COLORS = new PlaceholderKey<>(String.class, "gmPieColors");
    public static final PlaceholderKey<String> ACTIVITY_PIE_COLORS = new PlaceholderKey<>(String.class, "activityPieColors");
    public static final PlaceholderKey<String> PLAYERS_GRAPH_COLOR = CommonPlaceholderKeys.PLAYERS_GRAPH_COLOR;
    public static final PlaceholderKey<String> TPS_HIGH_COLOR = new PlaceholderKey<>(String.class, "tpsHighColor");
    public static final PlaceholderKey<String> TPS_MEDIUM_COLOR = new PlaceholderKey<>(String.class, "tpsMediumColor");
    public static final PlaceholderKey<String> TPS_LOW_COLOR = new PlaceholderKey<>(String.class, "tpsLowColor");
    public static final PlaceholderKey<String> AVG_PING_COLOR = new PlaceholderKey<>(String.class, "avgPingColor");
    public static final PlaceholderKey<String> MIN_PING_COLOR = new PlaceholderKey<>(String.class, "minPingColor");
    public static final PlaceholderKey<String> MAX_PING_COLOR = new PlaceholderKey<>(String.class, "maxPingColor");
    public static final PlaceholderKey<String> WORLD_MAP_HIGH_COLOR = CommonPlaceholderKeys.WORLD_MAP_HIGH_COLOR;
    public static final PlaceholderKey<String> WORLD_MAP_LOW_COLOR = CommonPlaceholderKeys.WORLD_MAP_LOW_COLOR;

    // Tables & other structures
    public static final PlaceholderKey<String> PLAYERS_TABLE = new PlaceholderKey<>(String.class, "tablePlayerlist");
    public static final PlaceholderKey<String> SESSION_ACCORDION_HTML = new PlaceholderKey<>(String.class, "accordionSessions");
    public static final PlaceholderKey<String> SESSION_ACCORDION_FUNCTIONS = new PlaceholderKey<>(String.class, "sessionTabGraphViewFunctions");
    public static final PlaceholderKey<String> SESSION_TABLE = new PlaceholderKey<>(String.class, "tableBodySessions");
    public static final PlaceholderKey<String> PING_TABLE = new PlaceholderKey<>(String.class, "tablePing");
    public static final PlaceholderKey<String> RECENT_LOGINS = new PlaceholderKey<>(String.class, "listRecentLogins");
    public static final PlaceholderKey<String> COMMAND_USAGE_TABLE = new PlaceholderKey<>(String.class, "tableCommandUsage");
    public static final PlaceholderKey<String> HEALTH_NOTES = CommonPlaceholderKeys.HEALTH_NOTES;
    public static final PlaceholderKey<String> PLUGINS_TAB = new PlaceholderKey<>(String.class, "tabsPlugins");
    public static final PlaceholderKey<String> PLUGINS_TAB_NAV = new PlaceholderKey<>(String.class, "navPluginsTabs");
    // Formatted time values
    public static final PlaceholderKey<String> REFRESH_TIME_F = CommonPlaceholderKeys.REFRESH_TIME_F;
    public static final PlaceholderKey<String> LAST_PEAK_TIME_F = CommonPlaceholderKeys.LAST_PEAK_TIME_F;
    public static final PlaceholderKey<String> ALL_TIME_PEAK_TIME_F = CommonPlaceholderKeys.ALL_TIME_PEAK_TIME_F;
    public static final PlaceholderKey<String> AVERAGE_SESSION_LENGTH_F = new PlaceholderKey<>(String.class, "sessionAverage");
    public static final PlaceholderKey<String> AVERAGE_PLAYTIME_F = new PlaceholderKey<>(String.class, "playtimeAverage");
    public static final PlaceholderKey<String> PLAYTIME_F = new PlaceholderKey<>(String.class, "playtimeTotal");
    // Direct values, possibly formatted
    public static final PlaceholderKey<String> PLAYERS_LAST_PEAK = CommonPlaceholderKeys.PLAYERS_LAST_PEAK;
    public static final PlaceholderKey<String> PLAYERS_ALL_TIME_PEAK = CommonPlaceholderKeys.PLAYERS_ALL_TIME_PEAK;
    public static final PlaceholderKey<Integer> OPERATORS = new PlaceholderKey<>(Integer.class, "ops");
    public static final PlaceholderKey<Integer> PLAYERS_REGULAR = new PlaceholderKey<>(Integer.class, "playersRegular");
    public static final PlaceholderKey<Integer> SESSION_COUNT = new PlaceholderKey<>(Integer.class, "sessionCount");
    public static final PlaceholderKey<Integer> DEATHS = new PlaceholderKey<>(Integer.class, "deaths");
    public static final PlaceholderKey<Integer> MOB_KILL_COUNT = new PlaceholderKey<>(Integer.class, "mobKillCount");
    public static final PlaceholderKey<Integer> PLAYER_KILL_COUNT = new PlaceholderKey<>(Integer.class, "killCount");
    public static final PlaceholderKey<Double> HEALTH_INDEX = CommonPlaceholderKeys.HEALTH_INDEX;
    public static final PlaceholderKey<Integer> COMMAND_COUNT = new PlaceholderKey<>(Integer.class, "commandCount");
    public static final PlaceholderKey<Integer> COMMAND_COUNT_UNIQUE = new PlaceholderKey<>(Integer.class, "commandUniqueCount");
    //
    public static final PlaceholderKey<Integer> PLAYERS_DAY = CommonPlaceholderKeys.PLAYERS_DAY;
    public static final PlaceholderKey<Integer> PLAYERS_WEEK = CommonPlaceholderKeys.PLAYERS_WEEK;
    public static final PlaceholderKey<Integer> PLAYERS_MONTH = CommonPlaceholderKeys.PLAYERS_MONTH;
    public static final PlaceholderKey<Integer> PLAYERS_NEW_DAY = CommonPlaceholderKeys.PLAYERS_NEW_DAY;
    public static final PlaceholderKey<Integer> PLAYERS_NEW_WEEK = CommonPlaceholderKeys.PLAYERS_NEW_WEEK;
    public static final PlaceholderKey<Integer> PLAYERS_NEW_MONTH = CommonPlaceholderKeys.PLAYERS_NEW_MONTH;
    public static final PlaceholderKey<Integer> AVG_PLAYERS = new PlaceholderKey<>(Integer.class, "playersAverage");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_DAY = new PlaceholderKey<>(Integer.class, "playersAverageDay");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_WEEK = new PlaceholderKey<>(Integer.class, "playersAverageWeek");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_MONTH = new PlaceholderKey<>(Integer.class, "playersAverageMonth");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW = new PlaceholderKey<>(Integer.class, "playersNewAverage");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW_DAY = new PlaceholderKey<>(Integer.class, "playersNewAverageDay");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW_WEEK = new PlaceholderKey<>(Integer.class, "playersNewAverageWeek");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW_MONTH = new PlaceholderKey<>(Integer.class, "playersNewAverageMonth");
    public static final PlaceholderKey<Integer> PLAYERS_RETAINED_DAY = new PlaceholderKey<>(Integer.class, "playersStuckDay");
    public static final PlaceholderKey<String> PLAYERS_RETAINED_DAY_PERC = new PlaceholderKey<>(String.class, "playersStuckPercDay");
    public static final PlaceholderKey<Integer> PLAYERS_RETAINED_WEEK = new PlaceholderKey<>(Integer.class, "playersStuckWeek");
    public static final PlaceholderKey<String> PLAYERS_RETAINED_WEEK_PERC = new PlaceholderKey<>(String.class, "playersStuckPercWeek");
    public static final PlaceholderKey<Integer> PLAYERS_RETAINED_MONTH = new PlaceholderKey<>(Integer.class, "playersStuckMonth");
    public static final PlaceholderKey<String> PLAYERS_RETAINED_MONTH_PERC = new PlaceholderKey<>(String.class, "playersStuckPercMonth");
    //
    public static final PlaceholderKey<Integer> TPS_SPIKE_MONTH = new PlaceholderKey<>(Integer.class, "tpsSpikeMonth");
    public static final PlaceholderKey<Integer> TPS_SPIKE_WEEK = new PlaceholderKey<>(Integer.class, "tpsSpikeWeek");
    public static final PlaceholderKey<Integer> TPS_SPIKE_DAY = new PlaceholderKey<>(Integer.class, "tpsSpikeDay");
    public static final PlaceholderKey<Double> AVG_TPS_MONTH = new PlaceholderKey<>(Double.class, "tpsAverageMonth");
    public static final PlaceholderKey<Double> AVG_TPS_WEEK = new PlaceholderKey<>(Double.class, "tpsAverageWeek");
    public static final PlaceholderKey<Double> AVG_TPS_DAY = new PlaceholderKey<>(Double.class, "tpsAverageDay");
    public static final PlaceholderKey<Double> AVG_CPU_MONTH = new PlaceholderKey<>(Double.class, "cpuAverageMonth");
    public static final PlaceholderKey<Double> AVG_CPU_WEEK = new PlaceholderKey<>(Double.class, "cpuAverageWeek");
    public static final PlaceholderKey<Double> AVG_CPU_DAY = new PlaceholderKey<>(Double.class, "cpuAverageDay");
    public static final PlaceholderKey<Double> AVG_RAM_MONTH = new PlaceholderKey<>(Double.class, "ramAverageMonth");
    public static final PlaceholderKey<Double> AVG_RAM_WEEK = new PlaceholderKey<>(Double.class, "ramAverageWeek");
    public static final PlaceholderKey<Double> AVG_RAM_DAY = new PlaceholderKey<>(Double.class, "ramAverageDay");
    public static final PlaceholderKey<Double> AVG_ENTITY_MONTH = new PlaceholderKey<>(Double.class, "entityAverageMonth");
    public static final PlaceholderKey<Double> AVG_ENTITY_WEEK = new PlaceholderKey<>(Double.class, "entityAverageWeek");
    public static final PlaceholderKey<Double> AVG_ENTITY_DAY = new PlaceholderKey<>(Double.class, "entityAverageDay");
    public static final PlaceholderKey<Double> AVG_CHUNK_MONTH = new PlaceholderKey<>(Double.class, "chunkAverageMonth");
    public static final PlaceholderKey<Double> AVG_CHUNK_WEEK = new PlaceholderKey<>(Double.class, "chunkAverageWeek");
    public static final PlaceholderKey<Double> AVG_CHUNK_DAY = new PlaceholderKey<>(Double.class, "chunkAverageDay");
    // Data for Charts
    public static final PlaceholderKey<String> WORLD_PIE_SERIES = new PlaceholderKey<>(String.class, "worldSeries");
    public static final PlaceholderKey<String> GM_PIE_SERIES = new PlaceholderKey<>(String.class, "gmSeries");
    public static final PlaceholderKey<String> PLAYERS_ONLINE_SERIES = CommonPlaceholderKeys.PLAYERS_ONLINE_SERIES;
    public static final PlaceholderKey<String> TPS_SERIES = new PlaceholderKey<>(String.class, "tpsSeries");
    public static final PlaceholderKey<String> CPU_SERIES = new PlaceholderKey<>(String.class, "cpuSeries");
    public static final PlaceholderKey<String> RAM_SERIES = new PlaceholderKey<>(String.class, "ramSeries");
    public static final PlaceholderKey<String> ENTITY_SERIES = new PlaceholderKey<>(String.class, "entitySeries");
    public static final PlaceholderKey<String> CHUNK_SERIES = new PlaceholderKey<>(String.class, "chunkSeries");
    public static final PlaceholderKey<String> PUNCHCARD_SERIES = new PlaceholderKey<>(String.class, "punchCardSeries");
    public static final PlaceholderKey<String> WORLD_MAP_SERIES = CommonPlaceholderKeys.WORLD_MAP_SERIES;
    public static final PlaceholderKey<String> ACTIVITY_STACK_SERIES = CommonPlaceholderKeys.ACTIVITY_STACK_SERIES;
    public static final PlaceholderKey<String> ACTIVITY_STACK_CATEGORIES = CommonPlaceholderKeys.ACTIVITY_STACK_CATEGORIES;
    public static final PlaceholderKey<String> ACTIVITY_PIE_SERIES = CommonPlaceholderKeys.ACTIVITY_PIE_SERIES;
    public static final PlaceholderKey<String> CALENDAR_SERIES = new PlaceholderKey<>(String.class, "calendarSeries");
    public static final PlaceholderKey<String> UNIQUE_PLAYERS_SERIES = new PlaceholderKey<>(String.class, "uniquePlayersSeries");
    public static final PlaceholderKey<String> NEW_PLAYERS_SERIES = new PlaceholderKey<>(String.class, "newPlayersSeries");
    public static final PlaceholderKey<String> AVG_PING_SERIES = new PlaceholderKey<>(String.class, "avgPingSeries");
    public static final PlaceholderKey<String> MAX_PING_SERIES = new PlaceholderKey<>(String.class, "maxPingSeries");
    public static final PlaceholderKey<String> MIN_PING_SERIES = new PlaceholderKey<>(String.class, "minPingSeries");
    public static final PlaceholderKey<String> COUNTRY_CATEGORIES = CommonPlaceholderKeys.COUNTRY_CATEGORIES;
    public static final PlaceholderKey<String> COUNTRY_SERIES = CommonPlaceholderKeys.COUNTRY_SERIES;
    // Variables used only during analysis
    public static final Key<SessionsMutator> SESSIONS_MUTATOR = CommonKeys.SESSIONS_MUTATOR;
    public static final Key<TPSMutator> TPS_MUTATOR = CommonKeys.TPS_MUTATOR;
    public static final Key<PlayersMutator> PLAYERS_MUTATOR = CommonKeys.PLAYERS_MUTATOR;
    public static final Key<PlayersOnlineResolver> PLAYERS_ONLINE_RESOLVER = new Key<>(PlayersOnlineResolver.class, "PLAYERS_ONLINE_RESOLVER");
    public static final Key<Long> PLAYTIME_TOTAL = new Key<>(Long.class, "PLAYTIME_TOTAL");
    public static final Key<Long> ANALYSIS_TIME = new Key<>(Long.class, "ANALYSIS_TIME");
    public static final Key<Long> ANALYSIS_TIME_DAY_AGO = new Key<>(Long.class, "ANALYSIS_TIME_DAY_AGO");
    public static final Key<Long> ANALYSIS_TIME_WEEK_AGO = new Key<>(Long.class, "ANALYSIS_TIME_WEEK_AGO");
    public static final Key<Long> ANALYSIS_TIME_MONTH_AGO = new Key<>(Long.class, "ANALYSIS_TIME_MONTH_AGO");
    public static final Key<Map<UUID, String>> PLAYER_NAMES = new Key<>(new Type<Map<UUID, String>>() {
    }, "PLAYER_NAMES");
    public static final Key<TreeMap<Long, Map<String, Set<UUID>>>> ACTIVITY_DATA = CommonKeys.ACTIVITY_DATA;
    public static final Key<Set<UUID>> BAN_DATA = new Key<>(new Type<Set<UUID>>() {
    }, "BAN_DATA");
    public static final Key<TreeMap<Long, Integer>> UNIQUE_PLAYERS_PER_DAY = new Key<>(new Type<TreeMap<Long, Integer>>() {
    }, "UNIQUE_PLAYERS_PER_DAY");
    public static final Key<TreeMap<Long, Integer>> NEW_PLAYERS_PER_DAY = new Key<>(new Type<TreeMap<Long, Integer>>() {
    }, "NEW_PLAYERS_PER_DAY");


    private AnalysisKeys() {
        /* Static variable class */
    }
}