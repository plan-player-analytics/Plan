/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;


/**
 * Used for parsing a Html String out of AnalysisContainer and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPage implements Page {

    private static final String DEBUG = "Analysis";
    private final AnalysisContainer analysisContainer;

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
                AnalysisKeys.VERSION, AnalysisKeys.SERVER_NAME, AnalysisKeys.TIME_ZONE,
                AnalysisKeys.FIRST_DAY, AnalysisKeys.TPS_MEDIUM, AnalysisKeys.TPS_HIGH,
                AnalysisKeys.PLAYERS_MAX, AnalysisKeys.PLAYERS_ONLINE, AnalysisKeys.PLAYERS_TOTAL,

                AnalysisKeys.WORLD_PIE_COLORS, AnalysisKeys.GM_PIE_COLORS, AnalysisKeys.ACTIVITY_PIE_COLORS,
                AnalysisKeys.PLAYERS_GRAPH_COLOR, AnalysisKeys.TPS_HIGH_COLOR, AnalysisKeys.TPS_MEDIUM_COLOR,
                AnalysisKeys.TPS_LOW_COLOR, AnalysisKeys.WORLD_MAP_HIGH_COLOR, AnalysisKeys.WORLD_MAP_LOW_COLOR,
                AnalysisKeys.AVG_PING_COLOR, AnalysisKeys.MAX_PING_COLOR, AnalysisKeys.MIN_PING_COLOR
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
                AnalysisKeys.HEALTH_NOTES
        );
        Benchmark.stop(DEBUG, DEBUG + " Server Health");
    }

    private void sessionStructures(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Session Structures");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.SESSION_ACCORDION_HTML, AnalysisKeys.SESSION_ACCORDION_FUNCTIONS,
                AnalysisKeys.SESSION_TABLE, AnalysisKeys.RECENT_LOGINS,
                AnalysisKeys.COMMAND_USAGE_TABLE, AnalysisKeys.PING_TABLE);
        Benchmark.stop(DEBUG, DEBUG + " Session Structures");
    }

    private void playersTable(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Players Table");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.PLAYERS_TABLE);
        Benchmark.stop(DEBUG, DEBUG + " Players Table");
    }

    private void pluginsTabs(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " 3rd Party");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.PLUGINS_TAB, AnalysisKeys.PLUGINS_TAB_NAV
        );
        Benchmark.stop(DEBUG, DEBUG + " 3rd Party");
    }

    private void miscTotals(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Misc. totals");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.REFRESH_TIME_F, AnalysisKeys.LAST_PEAK_TIME_F, AnalysisKeys.ALL_TIME_PEAK_TIME_F,
                AnalysisKeys.AVERAGE_SESSION_LENGTH_F, AnalysisKeys.AVERAGE_PLAYTIME_F, AnalysisKeys.PLAYTIME_F,

                AnalysisKeys.PLAYERS_LAST_PEAK, AnalysisKeys.PLAYERS_ALL_TIME_PEAK, AnalysisKeys.OPERATORS,
                AnalysisKeys.PLAYERS_REGULAR, AnalysisKeys.SESSION_COUNT, AnalysisKeys.DEATHS,
                AnalysisKeys.MOB_KILL_COUNT, AnalysisKeys.PLAYER_KILL_COUNT, AnalysisKeys.HEALTH_INDEX,
                AnalysisKeys.COMMAND_COUNT, AnalysisKeys.COMMAND_COUNT_UNIQUE
        );
        Benchmark.stop(DEBUG, DEBUG + " Misc. totals");
    }

    private void playerActivityNumbers(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Online Activity Numbers");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.PLAYERS_DAY, AnalysisKeys.PLAYERS_WEEK, AnalysisKeys.PLAYERS_MONTH,
                AnalysisKeys.PLAYERS_NEW_DAY, AnalysisKeys.PLAYERS_NEW_WEEK, AnalysisKeys.PLAYERS_NEW_MONTH,
                AnalysisKeys.AVG_PLAYERS, AnalysisKeys.AVG_PLAYERS_DAY, AnalysisKeys.AVG_PLAYERS_WEEK,
                AnalysisKeys.AVG_PLAYERS_MONTH, AnalysisKeys.AVG_PLAYERS_NEW, AnalysisKeys.AVG_PLAYERS_NEW_DAY,
                AnalysisKeys.AVG_PLAYERS_NEW_WEEK, AnalysisKeys.AVG_PLAYERS_NEW_MONTH, AnalysisKeys.PLAYERS_RETAINED_DAY,
                AnalysisKeys.PLAYERS_RETAINED_DAY_PERC, AnalysisKeys.PLAYERS_RETAINED_WEEK, AnalysisKeys.PLAYERS_RETAINED_WEEK_PERC,
                AnalysisKeys.PLAYERS_RETAINED_MONTH, AnalysisKeys.PLAYERS_RETAINED_MONTH_PERC
        );
        Benchmark.stop(DEBUG, DEBUG + " Online Activity Numbers");
    }

    private void performanceNumbers(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Performance Numbers");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.TPS_SPIKE_MONTH, AnalysisKeys.TPS_SPIKE_WEEK, AnalysisKeys.TPS_SPIKE_DAY
        );
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer, FormatUtils::cutDecimals,
                AnalysisKeys.AVG_TPS_MONTH, AnalysisKeys.AVG_TPS_WEEK, AnalysisKeys.AVG_TPS_DAY,
                AnalysisKeys.AVG_RAM_MONTH, AnalysisKeys.AVG_RAM_WEEK, AnalysisKeys.AVG_RAM_DAY,
                AnalysisKeys.AVG_ENTITY_MONTH, AnalysisKeys.AVG_ENTITY_WEEK, AnalysisKeys.AVG_ENTITY_DAY,
                AnalysisKeys.AVG_CHUNK_MONTH, AnalysisKeys.AVG_CHUNK_WEEK, AnalysisKeys.AVG_CHUNK_DAY
        );
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                value -> value != -1 ? FormatUtils.cutDecimals(value) : "Unavailable",
                AnalysisKeys.AVG_CPU_MONTH, AnalysisKeys.AVG_CPU_WEEK, AnalysisKeys.AVG_CPU_DAY
        );
        Benchmark.stop(DEBUG, DEBUG + " Performance Numbers");
    }

    private void chartSeries(PlaceholderReplacer placeholderReplacer) {
        Benchmark.start(DEBUG + " Chart Series");
        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
                AnalysisKeys.WORLD_PIE_SERIES, AnalysisKeys.GM_PIE_SERIES, AnalysisKeys.PLAYERS_ONLINE_SERIES,
                AnalysisKeys.TPS_SERIES, AnalysisKeys.CPU_SERIES, AnalysisKeys.RAM_SERIES,
                AnalysisKeys.ENTITY_SERIES, AnalysisKeys.CHUNK_SERIES, AnalysisKeys.PUNCHCARD_SERIES,
                AnalysisKeys.WORLD_MAP_SERIES, AnalysisKeys.ACTIVITY_STACK_SERIES, AnalysisKeys.ACTIVITY_STACK_CATEGORIES,
                AnalysisKeys.ACTIVITY_PIE_SERIES, AnalysisKeys.CALENDAR_SERIES,
                AnalysisKeys.UNIQUE_PLAYERS_SERIES, AnalysisKeys.NEW_PLAYERS_SERIES,
                AnalysisKeys.COUNTRY_CATEGORIES, AnalysisKeys.COUNTRY_SERIES,
                AnalysisKeys.AVG_PING_SERIES, AnalysisKeys.MAX_PING_SERIES, AnalysisKeys.MIN_PING_SERIES
        );
        Benchmark.stop(DEBUG, DEBUG + " Chart Series");
    }
}