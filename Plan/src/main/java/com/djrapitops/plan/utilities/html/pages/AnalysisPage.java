/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.system.DebugChannels;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;
import com.djrapitops.plugin.benchmarking.Timings;

import java.io.IOException;

import static com.djrapitops.plan.data.store.keys.AnalysisKeys.*;

/**
 * Used for parsing a Html String out of AnalysisContainer and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPage implements Page {

    private static final String CHANNEL = DebugChannels.ANALYSIS;

    private final AnalysisContainer analysisContainer;

    private final PlanFiles files;
    private final Formatter<Double> decimalFormatter;
    private final Timings timings;

    AnalysisPage(
            AnalysisContainer analysisContainer,
            PlanFiles files,
            Formatter<Double> decimalFormatter,
            Timings timings
    ) {
        this.analysisContainer = analysisContainer;
        this.files = files;
        this.decimalFormatter = decimalFormatter;
        this.timings = timings;
    }

    @Override
    public String toHtml() throws ParseException {
        timings.start("Analysis");
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                VERSION, SERVER_NAME, TIME_ZONE,
                FIRST_DAY, TPS_MEDIUM, TPS_HIGH,
                PLAYERS_MAX, PLAYERS_ONLINE, PLAYERS_TOTAL,

                WORLD_PIE_COLORS, GM_PIE_COLORS, ACTIVITY_PIE_COLORS,
                PLAYERS_GRAPH_COLOR, TPS_HIGH_COLOR, TPS_MEDIUM_COLOR,
                TPS_LOW_COLOR, WORLD_MAP_HIGH_COLOR, WORLD_MAP_LOW_COLOR,
                AVG_PING_COLOR, MAX_PING_COLOR, MIN_PING_COLOR
        );
        playersTable(placeholderReplacer);
        sessionStructures(placeholderReplacer);
        serverHealth(placeholderReplacer);
        pluginsTabs(placeholderReplacer);
        miscTotals(placeholderReplacer);
        playerActivityNumbers(placeholderReplacer);
        chartSeries(placeholderReplacer);
        performanceNumbers(placeholderReplacer);

        try {
            return placeholderReplacer.apply(files.readCustomizableResourceFlat("web/server.html"));
        } catch (IOException e) {
            throw new ParseException(e);
        } finally {
            timings.end(CHANNEL, "Analysis");
        }
    }

    private void serverHealth(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Server Health");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                HEALTH_NOTES
        );
        timings.end(CHANNEL, CHANNEL + " Server Health");
    }

    private void sessionStructures(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Session Structures");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                SESSION_ACCORDION_HTML, SESSION_ACCORDION_FUNCTIONS,
                SESSION_TABLE, RECENT_LOGINS,
                COMMAND_USAGE_TABLE, PING_TABLE);
        timings.end(CHANNEL, CHANNEL + " Session Structures");
    }

    private void playersTable(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Players Table");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                PLAYERS_TABLE);
        timings.end(CHANNEL, CHANNEL + " Players Table");
    }

    private void pluginsTabs(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " 3rd Party");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                PLUGINS_TAB, PLUGINS_TAB_NAV
        );
        timings.end(CHANNEL, CHANNEL + " 3rd Party");
    }

    private void miscTotals(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Misc. totals");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                REFRESH_TIME_F, REFRESH_TIME_FULL_F, LAST_PEAK_TIME_F, ALL_TIME_PEAK_TIME_F,
                AVERAGE_SESSION_LENGTH_F, AVERAGE_PLAYTIME_F, PLAYTIME_F,

                PLAYERS_LAST_PEAK, PLAYERS_ALL_TIME_PEAK, OPERATORS,
                PLAYERS_REGULAR, SESSION_COUNT, DEATHS,
                MOB_KILL_COUNT, PLAYER_KILL_COUNT, HEALTH_INDEX,
                COMMAND_COUNT, COMMAND_COUNT_UNIQUE
        );
        timings.end(CHANNEL, CHANNEL + " Misc. totals");
    }

    private void playerActivityNumbers(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Online Activity Numbers");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                PLAYERS_DAY, PLAYERS_WEEK, PLAYERS_MONTH,
                PLAYERS_NEW_DAY, PLAYERS_NEW_WEEK, PLAYERS_NEW_MONTH,
                AVG_PLAYERS, AVG_PLAYERS_DAY, AVG_PLAYERS_WEEK,
                AVG_PLAYERS_MONTH, AVG_PLAYERS_NEW, AVG_PLAYERS_NEW_DAY,
                AVG_PLAYERS_NEW_WEEK, AVG_PLAYERS_NEW_MONTH, PLAYERS_RETAINED_DAY,
                PLAYERS_RETAINED_DAY_PERC, PLAYERS_RETAINED_WEEK, PLAYERS_RETAINED_WEEK_PERC,
                PLAYERS_RETAINED_MONTH, PLAYERS_RETAINED_MONTH_PERC
        );
        timings.end(CHANNEL, CHANNEL + " Online Activity Numbers");
    }

    private void performanceNumbers(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Performance Numbers");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                TPS_SPIKE_MONTH, TPS_SPIKE_WEEK, TPS_SPIKE_DAY
        );
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer, decimalFormatter,
                AVG_TPS_MONTH, AVG_TPS_WEEK, AVG_TPS_DAY,
                AVG_RAM_MONTH, AVG_RAM_WEEK, AVG_RAM_DAY,
                AVG_ENTITY_MONTH, AVG_ENTITY_WEEK, AVG_ENTITY_DAY,
                AVG_CHUNK_MONTH, AVG_CHUNK_WEEK, AVG_CHUNK_DAY
        );
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                value -> value != -1 ? decimalFormatter.apply(value) : "Unavailable",
                AVG_CPU_MONTH, AVG_CPU_WEEK, AVG_CPU_DAY
        );
        timings.end(CHANNEL, CHANNEL + " Performance Numbers");
    }

    private void chartSeries(PlaceholderReplacer placeholderReplacer) {
        timings.start(CHANNEL + " Chart Series");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                WORLD_PIE_SERIES, GM_PIE_SERIES, PLAYERS_ONLINE_SERIES,
                TPS_SERIES, CPU_SERIES, RAM_SERIES,
                ENTITY_SERIES, CHUNK_SERIES, PUNCHCARD_SERIES,
                WORLD_MAP_SERIES, ACTIVITY_STACK_SERIES, ACTIVITY_STACK_CATEGORIES,
                ACTIVITY_PIE_SERIES, CALENDAR_SERIES,
                UNIQUE_PLAYERS_SERIES, NEW_PLAYERS_SERIES,
                COUNTRY_CATEGORIES, COUNTRY_SERIES,
                AVG_PING_SERIES, MAX_PING_SERIES, MIN_PING_SERIES
        );
        timings.end(CHANNEL, CHANNEL + " Chart Series");
    }
}