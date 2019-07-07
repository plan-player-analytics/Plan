/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.json;

import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.db.access.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Parses JSON payload for /server-page Playerbase Overview tab.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerBaseOverviewJSONParser implements TabJSONParser<Map<String, Object>> {

    private PlanConfig config;
    private DBSystem dbSystem;

    private Formatter<Long> timeAmountFormatter;
    private Formatter<Double> percentageFormatter;

    @Inject
    public PlayerBaseOverviewJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        timeAmountFormatter = formatters.timeAmount();
        percentageFormatter = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap(UUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("trends", createTrendsMap(serverUUID));
        serverOverview.put("insights", createInsightsMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createTrendsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long twoMonthsAgo = now - TimeUnit.DAYS.toMillis(60L);
        Long playThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> trends = new HashMap<>();

        Integer playersBefore = db.query(PlayerCountQueries.uniquePlayerCount(0L, monthAgo, serverUUID));
        Integer playersAfter = db.query(PlayerCountQueries.uniquePlayerCount(0L, now, serverUUID));
        trends.put("total_players_then", playersBefore);
        trends.put("total_players_now", playersAfter);
        trends.put("total_players_trend", new Trend(playersBefore, playersAfter, false));

        Integer regularBefore = db.query(ActivityIndexQueries.fetchRegularPlayerCount(monthAgo, serverUUID, playThreshold));
        Integer regularAfter = db.query(ActivityIndexQueries.fetchRegularPlayerCount(now, serverUUID, playThreshold));
        trends.put("regular_players_then", regularBefore);
        trends.put("regular_players_now", regularAfter);
        trends.put("regular_players_trend", new Trend(regularBefore, regularAfter, false));

        // TODO
        trends.put("playtime_avg_then", "Not implemented");
        trends.put("playtime_avg_now", "Not implemented");
        trends.put("playtime_avg_trend", new Trend(0, 0, false, timeAmountFormatter));

        Long playtimeBefore = db.query(SessionQueries.playtime(twoMonthsAgo, monthAgo, serverUUID));
        Long playtimeAfter = db.query(SessionQueries.playtime(monthAgo, now, serverUUID));
        Long afkBefore = db.query(SessionQueries.afkTime(twoMonthsAgo, monthAgo, serverUUID));
        Long afkAfter = db.query(SessionQueries.afkTime(monthAgo, now, serverUUID));
        double afkPercBefore = playersBefore != 0 ? (double) afkBefore / playtimeBefore : 0;
        double afkPercAfter = playersBefore != 0 ? (double) afkAfter / playtimeAfter : 0;
        trends.put("afk_then", percentageFormatter.apply(afkPercBefore));
        trends.put("afk_now", percentageFormatter.apply(afkPercAfter));
        trends.put("afk_trend", new Trend(afkPercBefore, afkPercAfter, Trend.REVERSED, percentageFormatter));

        // TODO
        trends.put("regular_playtime_avg_then", "Not implemented");
        trends.put("regular_playtime_avg_now", "Not implemented");
        trends.put("regular_playtime_avg_trend", new Trend(0, 0, false, timeAmountFormatter));
        // TODO
        trends.put("regular_session_avg_then", "Not implemented");
        trends.put("regular_session_avg_now", "Not implemented");
        trends.put("regular_session_avg_trend", new Trend(0, 0, false, timeAmountFormatter));
        // TODO
        trends.put("regular_afk_avg_then", "Not implemented");
        trends.put("regular_afk_avg_now", "Not implemented");
        trends.put("regular_afk_avg_trend", new Trend(0, 0, Trend.REVERSED, percentageFormatter));

        return trends;
    }

    private Map<String, Object> createInsightsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        Long playThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> insights = new HashMap<>();

        int newToRegular = db.query(ActivityIndexQueries.countNewPlayersTurnedRegular(monthAgo, now, serverUUID, playThreshold));
        Integer newToRegularBefore = db.query(ActivityIndexQueries.countNewPlayersTurnedRegular(monthAgo, halfMonthAgo, serverUUID, playThreshold));
        Integer newToRegularAfter = db.query(ActivityIndexQueries.countNewPlayersTurnedRegular(halfMonthAgo, now, serverUUID, playThreshold));
        insights.put("new_to_regular", newToRegular);
        insights.put("new_to_regular_trend", new Trend(newToRegularBefore, newToRegularAfter, false));

        Integer regularToInactive = db.query(ActivityIndexQueries.countRegularPlayersTurnedInactive(monthAgo, now, serverUUID, playThreshold));
        Integer regularToInactiveBefore = db.query(ActivityIndexQueries.countRegularPlayersTurnedInactive(monthAgo, halfMonthAgo, serverUUID, playThreshold));
        Integer regularToInactiveAfter = db.query(ActivityIndexQueries.countRegularPlayersTurnedInactive(halfMonthAgo, now, serverUUID, playThreshold));
        insights.put("regular_to_inactive", regularToInactive);
        insights.put("regular_to_inactive_trend", new Trend(regularToInactiveBefore, regularToInactiveAfter, Trend.REVERSED));

        return insights;
    }
}