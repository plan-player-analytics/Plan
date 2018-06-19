/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.AnalysisContainer;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.file.FileUtil;

import java.io.IOException;

/**
 * Used for parsing a Html String out of AnalysisData and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPage extends Page {

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
        PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();
//        placeholderReplacer.addAllPlaceholdersFrom(analysisContainer,
//                VERSION, SERVER_NAME, TIME_ZONE,
//                FIRST_DAY, TPS_MEDIUM, TPS_HIGH,
//                PLAYERS_MAX, PLAYERS_ONLINE, PLAYERS_TOTAL,
//
//                WORLD_PIE_COLORS, GM_PIE_COLORS, ACTIVITY_PIE_COLORS,
//                PLAYERS_GRAPH_COLOR, TPS_HIGH_COLOR, TPS_MEDIUM_COLOR,
//                TPS_LOW_COLOR, WORLD_MAP_HIGH_COLOR, WORLD_MAP_LOW_COLOR,
//
//                PLAYERS_TABLE, SESSION_ACCORDION_HTML, SESSION_ACCORDION_FUNCTIONS,
//                SESSION_TABLE, RECENT_LOGINS, COMMAND_USAGE_TABLE,
//                HEALTH_NOTES, PLUGINS_TAB, PLUGINS_TAB_NAV,
//
//                REFRESH_TIME_F, LAST_PEAK_TIME_F, ALL_TIME_PEAK_TIME_F,
//                AVERAGE_SESSION_LENGTH_F, AVERAGE_PLAYTIME_F, PLAYTIME_F,
//
//                PLAYERS_LAST_PEAK, PLAYERS_ALL_TIME_PEAK, OPERATORS,
//                PLAYERS_REGULAR, SESSION_COUNT, DEATHS,
//                MOB_KILL_COUNT, PLAYER_KILL_COUNT, HEALTH_INDEX,
//                COMMAND_COUNT, COMMAND_COUNT_UNIQUE,
//
//                PLAYERS_DAY, PLAYERS_WEEK, PLAYERS_MONTH,
//                PLAYERS_NEW_DAY, PLAYERS_NEW_WEEK, PLAYERS_NEW_MONTH,
//                AVG_PLAYERS, AVG_PLAYERS_DAY, AVG_PLAYERS_WEEK,
//                AVG_PLAYERS_MONTH, AVG_PLAYERS_NEW, AVG_PLAYERS_NEW_DAY,
//                AVG_PLAYERS_NEW_WEEK, AVG_PLAYERS_NEW_MONTH, PLAYERS_STUCK_DAY,
//                PLAYERS_STUCK_DAY_PERC, PLAYERS_STUCK_WEEK, PLAYERS_STUCK_WEEK_PERC,
//                PLAYERS_STUCK_MONTH, PLAYERS_STUCK_MONTH_PERC,
//
//                TPS_SPIKE_MONTH, TPS_SPIKE_WEEK, TPS_SPIKE_DAY,
//                AVG_TPS_MONTH, AVG_TPS_WEEK, AVG_TPS_DAY,
//                AVG_CPU_MONTH, AVG_CPU_WEEK, AVG_CPU_DAY,
//                AVG_RAM_MONTH, AVG_RAM_WEEK, AVG_RAM_DAY,
//                AVG_ENTITY_MONTH, AVG_ENTITY_WEEK, AVG_ENTITY_DAY,
//                AVG_CHUNK_MONTH, AVG_CHUNK_WEEK, AVG_CHUNK_DAY,
//
//                WORLD_PIE_SERIES, GM_PIE_SERIES, PLAYERS_ONLINE_SERIES,
//                TPS_SERIES, CPU_SERIES, RAM_SERIES,
//                ENTITY_SERIES, CHUNK_SERIES, PUNCHCARD_SERIES,
//                WORLD_MAP_SERIES, ACTIVITY_STACK_SERIES, ACTIVITY_STACK_CATEGORIES,
//                ACTIVITY_PIE_SERIES, CALENDAR_SERIES
//        );

        try {
            return placeholderReplacer.apply(FileUtil.getStringFromResource("web/server.html"));
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
}