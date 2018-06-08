package com.djrapitops.plan.data.store.keys;

import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.PlaceholderKey;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.utilities.html.structure.SessionAccordion;

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
    public static final PlaceholderKey<String> VERSION = new PlaceholderKey<>(String.class, "version");
    public static final PlaceholderKey<String> SERVER_NAME = new PlaceholderKey<>(String.class, "serverName");
    public static final PlaceholderKey<Integer> TIME_ZONE = new PlaceholderKey<>(Integer.class, "timeZone");
    public static final PlaceholderKey<Integer> FIRST_DAY = new PlaceholderKey<>(Integer.class, "firstDay");
    public static final PlaceholderKey<Integer> TPS_MEDIUM = new PlaceholderKey<>(Integer.class, "tpsMedium");
    public static final PlaceholderKey<Integer> TPS_HIGH = new PlaceholderKey<>(Integer.class, "tpsHigh");
    public static final PlaceholderKey<Integer> PLAYERS_MAX = new PlaceholderKey<>(Integer.class, "playersMax");
    public static final PlaceholderKey<Integer> PLAYERS_ONLINE = new PlaceholderKey<>(Integer.class, "playersOnline");
    public static final PlaceholderKey<Integer> PLAYERS_TOTAL = new PlaceholderKey<>(Integer.class, "playersTotal");
    //
    public static final PlaceholderKey<String> WORLD_PIE_COLORS = new PlaceholderKey<>(String.class, "worldPieColors");
    public static final PlaceholderKey<String> GM_PIE_COLORS = new PlaceholderKey<>(String.class, "gm_pie_colors");
    public static final PlaceholderKey<String> ACTIVITY_PIE_COLORS = new PlaceholderKey<>(String.class, "activityPieColors");
    public static final PlaceholderKey<String> PLAYERS_GRAPH_COLOR = new PlaceholderKey<>(String.class, "playersGraphColor");
    public static final PlaceholderKey<String> TPS_HIGH_COLOR = new PlaceholderKey<>(String.class, "tpsHighColor");
    public static final PlaceholderKey<String> TPS_MEDIUM_COLOR = new PlaceholderKey<>(String.class, "tpsMediumColor");
    public static final PlaceholderKey<String> TPS_LOW_COLOR = new PlaceholderKey<>(String.class, "tpsLowColor");
    public static final PlaceholderKey<String> WORLD_MAP_HIGH_COLOR = new PlaceholderKey<>(String.class, "worldMapColHigh");
    public static final PlaceholderKey<String> WORLD_MAP_LOW_COLOR = new PlaceholderKey<>(String.class, "worldMapColLow");
    // Tables & other structures
    public static final PlaceholderKey<String> PLAYERS_TABLE = new PlaceholderKey<>(String.class, "tablePlayerlist");
    public static final PlaceholderKey<String> SESSION_ACCORDION_HTML = new PlaceholderKey<>(String.class, "accordionSessions");
    public static final PlaceholderKey<String> SESSION_ACCORDION_FUNCTIONS = new PlaceholderKey<>(String.class, "sessionTabGraphViewFunctions");
    public static final PlaceholderKey<String> SESSION_TABLE = new PlaceholderKey<>(String.class, "tableBodySessions");
    public static final PlaceholderKey<String> RECENT_LOGINS = new PlaceholderKey<>(String.class, "listRecentLogins");
    public static final PlaceholderKey<String> COMMAND_USAGE_TABLE = new PlaceholderKey<>(String.class, "tableBodyCommands");
    public static final PlaceholderKey<String> HEALTH_NOTES = new PlaceholderKey<>(String.class, "healthNotes");
    public static final PlaceholderKey<String> PLUGINS_TAB = new PlaceholderKey<>(String.class, "navPluginsTabs");
    public static final PlaceholderKey<String> PLUGINS_TAB_NAV = new PlaceholderKey<>(String.class, "tabsPlugins");
    // Formatted time values
    public static final PlaceholderKey<String> REFRESH_TIME_F = new PlaceholderKey<>(String.class, "refresh");
    public static final PlaceholderKey<String> LAST_PEAK_TIME_F = new PlaceholderKey<>(String.class, "lastPeakTime");
    public static final PlaceholderKey<String> ALL_TIME_PEAK_TIME_F = new PlaceholderKey<>(String.class, "bestPeakTime");
    public static final PlaceholderKey<String> AVERAGE_SESSION_LENGTH_F = new PlaceholderKey<>(String.class, "sessionAverage");
    public static final PlaceholderKey<String> AVERAGE_PLAYTIME_F = new PlaceholderKey<>(String.class, "playtimeAverage");
    public static final PlaceholderKey<String> PLAYTIME_F = new PlaceholderKey<>(String.class, "playtimeTotal");
    // Direct values, possibly formatted
    public static final PlaceholderKey<String> PLAYERS_LAST_PEAK = new PlaceholderKey<>(String.class, "playersLastPeak");
    public static final PlaceholderKey<String> PLAYERS_ALL_TIME_PEAK = new PlaceholderKey<>(String.class, "playersBestPeak");
    public static final PlaceholderKey<Integer> OPERATORS = new PlaceholderKey<>(Integer.class, "ops");
    public static final PlaceholderKey<Integer> PLAYERS_REGULAR = new PlaceholderKey<>(Integer.class, "playersRegular");
    public static final PlaceholderKey<Integer> SESSION_COUNT = new PlaceholderKey<>(Integer.class, "sessionCount");
    public static final PlaceholderKey<Integer> DEATHS = new PlaceholderKey<>(Integer.class, "deaths");
    public static final PlaceholderKey<Integer> MOB_KILL_COUNT = new PlaceholderKey<>(Integer.class, "mobKillCount");
    public static final PlaceholderKey<Integer> PLAYER_KILL_COUNT = new PlaceholderKey<>(Integer.class, "killCount");
    public static final PlaceholderKey<Double> HEALTH_INDEX = new PlaceholderKey<>(Double.class, "healthIndex");
    public static final PlaceholderKey<Integer> COMMAND_COUNT = new PlaceholderKey<>(Integer.class, "commandCount");
    public static final PlaceholderKey<Integer> COMMAND_COUNT_UNIQUE = new PlaceholderKey<>(Integer.class, "commandUniqueCount");
    //
    public static final PlaceholderKey<Integer> PLAYERS_DAY = new PlaceholderKey<>(Integer.class, "playersDay");
    public static final PlaceholderKey<Integer> PLAYERS_WEEK = new PlaceholderKey<>(Integer.class, "playersWeek");
    public static final PlaceholderKey<Integer> PLAYERS_MONTH = new PlaceholderKey<>(Integer.class, "playersMonth");
    public static final PlaceholderKey<Integer> PLAYERS_NEW_DAY = new PlaceholderKey<>(Integer.class, "playersNewDay");
    public static final PlaceholderKey<Integer> PLAYERS_NEW_WEEK = new PlaceholderKey<>(Integer.class, "playersNewWeek");
    public static final PlaceholderKey<Integer> PLAYERS_NEW_MONTH = new PlaceholderKey<>(Integer.class, "playersNewMonth");
    public static final PlaceholderKey<Integer> AVG_PLAYERS = new PlaceholderKey<>(Integer.class, "playersAverage");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_DAY = new PlaceholderKey<>(Integer.class, "playersAverageDay");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_WEEK = new PlaceholderKey<>(Integer.class, "playersAverageWeek");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_MONTH = new PlaceholderKey<>(Integer.class, "playersAverageMonth");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW = new PlaceholderKey<>(Integer.class, "playersNewAverage");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW_DAY = new PlaceholderKey<>(Integer.class, "playersNewAverageDay");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW_WEEK = new PlaceholderKey<>(Integer.class, "playersNewAverageWeek");
    public static final PlaceholderKey<Integer> AVG_PLAYERS_NEW_MONTH = new PlaceholderKey<>(Integer.class, "playersNewAverageMonth");
    public static final PlaceholderKey<Integer> PLAYERS_STUCK_DAY = new PlaceholderKey<>(Integer.class, "playersStuckDay");
    public static final PlaceholderKey<String> PLAYERS_STUCK_DAY_PERC = new PlaceholderKey<>(String.class, "playersStuckPercDay");
    public static final PlaceholderKey<Integer> PLAYERS_STUCK_WEEK = new PlaceholderKey<>(Integer.class, "playersStuckWeek");
    public static final PlaceholderKey<String> PLAYERS_STUCK_WEEK_PERC = new PlaceholderKey<>(String.class, "playersStuckPercWeek");
    public static final PlaceholderKey<Integer> PLAYERS_STUCK_MONTH = new PlaceholderKey<>(Integer.class, "playersStuckMonth");
    public static final PlaceholderKey<String> PLAYERS_STUCK_MONTH_PERC = new PlaceholderKey<>(String.class, "playersStuckPercMonth");
    //
    public static final PlaceholderKey<Integer> TPS_SPIKE_MONTH = new PlaceholderKey<>(Integer.class, "tpsSpikeMonth");
    public static final PlaceholderKey<Integer> TPS_SPIKE_WEEK = new PlaceholderKey<>(Integer.class, "tpsSpikeWeek");
    public static final PlaceholderKey<Integer> TPS_SPIKE_DAY = new PlaceholderKey<>(Integer.class, "tpsSpikeDay");
    public static final PlaceholderKey<Integer> AVG_TPS_MONTH = new PlaceholderKey<>(Integer.class, "tpsAverageMonth");
    public static final PlaceholderKey<Integer> AVG_TPS_WEEK = new PlaceholderKey<>(Integer.class, "tpsAverageWeek");
    public static final PlaceholderKey<Integer> AVG_TPS_DAY = new PlaceholderKey<>(Integer.class, "tpsAverageDay");
    public static final PlaceholderKey<Integer> AVG_CPU_MONTH = new PlaceholderKey<>(Integer.class, "cpuAverageMonth");
    public static final PlaceholderKey<Integer> AVG_CPU_WEEK = new PlaceholderKey<>(Integer.class, "cpuAverageWeek");
    public static final PlaceholderKey<Integer> AVG_CPU_DAY = new PlaceholderKey<>(Integer.class, "cpuAverageDay");
    public static final PlaceholderKey<Integer> AVG_RAM_MONTH = new PlaceholderKey<>(Integer.class, "ramAverageMonth");
    public static final PlaceholderKey<Integer> AVG_RAM_WEEK = new PlaceholderKey<>(Integer.class, "ramAverageWeek");
    public static final PlaceholderKey<Integer> AVG_RAM_DAY = new PlaceholderKey<>(Integer.class, "ramAverageDay");
    public static final PlaceholderKey<Integer> AVG_ENTITY_MONTH = new PlaceholderKey<>(Integer.class, "entityAverageMonth");
    public static final PlaceholderKey<Integer> AVG_ENTITY_WEEK = new PlaceholderKey<>(Integer.class, "entityAverageWeek");
    public static final PlaceholderKey<Integer> AVG_ENTITY_DAY = new PlaceholderKey<>(Integer.class, "entityAverageDay");
    public static final PlaceholderKey<Integer> AVG_CHUNK_MONTH = new PlaceholderKey<>(Integer.class, "chunkAverageMonth");
    public static final PlaceholderKey<Integer> AVG_CHUNK_WEEK = new PlaceholderKey<>(Integer.class, "chunkAverageWeek");
    public static final PlaceholderKey<Integer> AVG_CHUNK_DAY = new PlaceholderKey<>(Integer.class, "chunkAverageDay");
    // Data for Charts
    public static final PlaceholderKey<String> WORLD_PIE_SERIES = new PlaceholderKey<>(String.class, "worldSeries");
    public static final PlaceholderKey<String> GM_PIE_SERIES = new PlaceholderKey<>(String.class, "gmSeries");
    public static final PlaceholderKey<String> PLAYERS_ONLINE_SERIES = new PlaceholderKey<>(String.class, "playersOnlineSeries");
    public static final PlaceholderKey<String> TPS_SERIES = new PlaceholderKey<>(String.class, "tpsSeries");
    public static final PlaceholderKey<String> CPU_SERIES = new PlaceholderKey<>(String.class, "cpuSeries");
    public static final PlaceholderKey<String> RAM_SERIES = new PlaceholderKey<>(String.class, "ramSeries");
    public static final PlaceholderKey<String> ENTITY_SERIES = new PlaceholderKey<>(String.class, "entitySeries");
    public static final PlaceholderKey<String> CHUNK_SERIES = new PlaceholderKey<>(String.class, "chunkSeries");
    public static final PlaceholderKey<String> PUNCHCARD_SERIES = new PlaceholderKey<>(String.class, "punchCardSeries");
    public static final PlaceholderKey<String> WORLD_MAP_SERIES = new PlaceholderKey<>(String.class, "geoMapSeries");
    public static final PlaceholderKey<String> ACTIVITY_STACK_SERIES = new PlaceholderKey<>(String.class, "activityStackSeries");
    public static final PlaceholderKey<String> ACTIVITY_STACK_CATEGORIES = new PlaceholderKey<>(String.class, "activityStackCategories");
    public static final PlaceholderKey<String> ACTIVITY_PIE_SERIES = new PlaceholderKey<>(String.class, "activityPieSeries");
    public static final PlaceholderKey<String> CALENDAR_SERIES = new PlaceholderKey<>(String.class, "calendarSeries");
    // Variables used only during analysis
    public static final Key<SessionsMutator> SESSIONS_MUTATOR = new Key<>(SessionsMutator.class, "SESSIONS_MUTATOR");
    public static final Key<Long> PLAYTIME_TOTAL = new Key<>(Long.class, "PLAYTIME_TOTAL");
    public static final Key<Long> ANALYSIS_TIME = new Key<>(Long.class, "ANALYSIS_TIME");
    public static final Key<Long> ANALYSIS_TIME_DAY_AGO = new Key<>(Long.class, "ANALYSIS_TIME_DAY_AGO");
    public static final Key<Long> ANALYSIS_TIME_WEEK_AGO = new Key<>(Long.class, "ANALYSIS_TIME_WEEK_AGO");
    public static final Key<Long> ANALYSIS_TIME_MONTH_AGO = new Key<>(Long.class, "ANALYSIS_TIME_MONTH_AGO");
    public static final Key<SessionAccordion> SESSION_ACCORDION = new Key<>(SessionAccordion.class, "SESSION_ACCORDION");


    private AnalysisKeys() {
        /* Static variable class */
    }
}