/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;

import static com.djrapitops.plan.data.store.keys.AnalysisKeys.*;

/**
 * Used for parsing a Html String out of AnalysisContainer and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPage implements Page {

    private final AnalysisContainer analysisContainer;
    private static final String DEBUG = "Analysis";

    public AnalysisPage(AnalysisContainer analysisContainer) {
        this.analysisContainer = analysisContainer;
    }

    public static String getRefreshingHtml() {
        ErrorResponse refreshPage = new ErrorResponse();
        refreshPage.setTitle("Analysis is being refreshed..");
        refreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"5\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
        refreshPage.replacePlaceholders();
        return refreshPage.getContent();
    }

    @Override
    public String toHtml() throws ParseException {
        Benchmark.start(DEBUG);
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                VERSION, SERVER_NAME, TIME_ZONE,
                FIRST_DAY, TPS_MEDIUM, TPS_HIGH,
                PLAYERS_MAX, PLAYERS_ONLINE, PLAYERS_TOTAL,

                WORLD_PIE_COLORS, GM_PIE_COLORS, ACTIVITY_PIE_COLORS,
                PLAYERS_GRAPH_COLOR, TPS_HIGH_COLOR, TPS_MEDIUM_COLOR,
                TPS_LOW_COLOR, WORLD_MAP_HIGH_COLOR, WORLD_MAP_LOW_COLOR
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
            return placeholderReplacer.apply(FileUtil.getStringFromResource("web/server.html"));
        } catch (IOException e) {
            throw new ParseException(e);
        } finally {
            Benchmark.stop(DEBUG, DEBUG);
            Log.logDebug(DEBUG);
        }
    }

    private void serverHealth(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Server Health");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                HEALTH_NOTES
        );
        Benchmark.stop(DEBUG, DEBUG + " Server Health");
    }

    private void sessionStructures(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Session Structures");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                SESSION_ACCORDION_HTML, SESSION_ACCORDION_FUNCTIONS,
                SESSION_TABLE, RECENT_LOGINS,
                COMMAND_USAGE_TABLE);
        Benchmark.stop(DEBUG, DEBUG + " Session Structures");
    }

    private void playersTable(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Players Table");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                PLAYERS_TABLE);
        Benchmark.stop(DEBUG, DEBUG + " Players Table");
    }

    private void pluginsTabs(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " 3rd Party");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                PLUGINS_TAB, PLUGINS_TAB_NAV
        );
        Benchmark.stop(DEBUG, DEBUG + " 3rd Party");
    }

    private void miscTotals(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Misc. totals");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                REFRESH_TIME_F, LAST_PEAK_TIME_F, ALL_TIME_PEAK_TIME_F,
                AVERAGE_SESSION_LENGTH_F, AVERAGE_PLAYTIME_F, PLAYTIME_F,

                PLAYERS_LAST_PEAK, PLAYERS_ALL_TIME_PEAK, OPERATORS,
                PLAYERS_REGULAR, SESSION_COUNT, DEATHS,
                MOB_KILL_COUNT, PLAYER_KILL_COUNT, HEALTH_INDEX,
                COMMAND_COUNT, COMMAND_COUNT_UNIQUE
        );
        Benchmark.stop(DEBUG, DEBUG + " Misc. totals");
    }

    private void playerActivityNumbers(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Online Activity Numbers");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                PLAYERS_DAY, PLAYERS_WEEK, PLAYERS_MONTH,
                PLAYERS_NEW_DAY, PLAYERS_NEW_WEEK, PLAYERS_NEW_MONTH,
                AVG_PLAYERS, AVG_PLAYERS_DAY, AVG_PLAYERS_WEEK,
                AVG_PLAYERS_MONTH, AVG_PLAYERS_NEW, AVG_PLAYERS_NEW_DAY,
                AVG_PLAYERS_NEW_WEEK, AVG_PLAYERS_NEW_MONTH, PLAYERS_RETAINED_DAY,
                PLAYERS_RETAINED_DAY_PERC, PLAYERS_RETAINED_WEEK, PLAYERS_RETAINED_WEEK_PERC,
                PLAYERS_RETAINED_MONTH, PLAYERS_RETAINED_MONTH_PERC
        );
        Benchmark.stop(DEBUG, DEBUG + " Online Activity Numbers");
    }

    private void performanceNumbers(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Performance Numbers");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                TPS_SPIKE_MONTH, TPS_SPIKE_WEEK, TPS_SPIKE_DAY
        );
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer, FormatUtils::cutDecimals,
                AVG_TPS_MONTH, AVG_TPS_WEEK, AVG_TPS_DAY,
                AVG_RAM_MONTH, AVG_RAM_WEEK, AVG_RAM_DAY,
                AVG_ENTITY_MONTH, AVG_ENTITY_WEEK, AVG_ENTITY_DAY,
                AVG_CHUNK_MONTH, AVG_CHUNK_WEEK, AVG_CHUNK_DAY
        );
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                value -> value != -1 ? FormatUtils.cutDecimals(value) : "Unavailable",
                AVG_CPU_MONTH, AVG_CPU_WEEK, AVG_CPU_DAY
        );
        Benchmark.stop(DEBUG, DEBUG + " Performance Numbers");
    }

    private void chartSeries(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Chart Series");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                WORLD_PIE_SERIES, GM_PIE_SERIES, PLAYERS_ONLINE_SERIES,
                TPS_SERIES, CPU_SERIES, RAM_SERIES,
                ENTITY_SERIES, CHUNK_SERIES, PUNCHCARD_SERIES,
                WORLD_MAP_SERIES, ACTIVITY_STACK_SERIES, ACTIVITY_STACK_CATEGORIES,
                ACTIVITY_PIE_SERIES, CALENDAR_SERIES,
                UNIQUE_PLAYERS_SERIES, NEW_PLAYERS_SERIES
        );
        Benchmark.stop(DEBUG, DEBUG + " Chart Series");
    }
}