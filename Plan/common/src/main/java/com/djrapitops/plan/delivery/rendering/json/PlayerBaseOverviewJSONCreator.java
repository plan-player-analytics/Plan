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
package com.djrapitops.plan.delivery.rendering.json;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.utilities.analysis.Percentage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /server-page Playerbase Overview tab.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayerBaseOverviewJSONCreator implements ServerTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final Formatter<Double> percentage;

    @Inject
    public PlayerBaseOverviewJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        percentage = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap(ServerUUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("trends", createTrendsMap(serverUUID));
        serverOverview.put("insights", createInsightsMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createTrendsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long twoMonthsAgo = now - TimeUnit.DAYS.toMillis(60L);
        Long playThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> trends = new HashMap<>();

        Integer playersBefore = db.query(PlayerCountQueries.newPlayerCount(0L, monthAgo, serverUUID));
        Integer playersAfter = db.query(PlayerCountQueries.newPlayerCount(0L, now, serverUUID));
        trends.put("total_players_then", playersBefore);
        trends.put("total_players_now", playersAfter);
        trends.put("total_players_trend", new Trend(playersBefore, playersAfter, false));

        Integer regularBefore = db.query(ActivityIndexQueries.fetchRegularPlayerCount(monthAgo, serverUUID, playThreshold));
        Integer regularAfter = db.query(ActivityIndexQueries.fetchRegularPlayerCount(now, serverUUID, playThreshold));
        trends.put("regular_players_then", regularBefore);
        trends.put("regular_players_now", regularAfter);
        trends.put("regular_players_trend", new Trend(regularBefore, regularAfter, false));

        Long avgPlaytimeBefore = db.query(SessionQueries.averagePlaytimePerPlayer(twoMonthsAgo, monthAgo, serverUUID));
        Long avgPlaytimeAfter = db.query(SessionQueries.averagePlaytimePerPlayer(monthAgo, now, serverUUID));
        trends.put("playtime_avg_then", avgPlaytimeBefore);
        trends.put("playtime_avg_now", avgPlaytimeAfter);
        trends.put("playtime_avg_trend", new Trend(avgPlaytimeBefore, avgPlaytimeAfter, false));

        Long avgAfkBefore = db.query(SessionQueries.averageAfkPerPlayer(twoMonthsAgo, monthAgo, serverUUID));
        Long avgAfkAfter = db.query(SessionQueries.averageAfkPerPlayer(monthAgo, now, serverUUID));
        double afkPercentageBefore = Percentage.calculate(avgAfkBefore, avgPlaytimeBefore);
        double afkPercentageAfter = Percentage.calculate(avgAfkAfter, avgPlaytimeAfter);
        trends.put("afk_then", percentage.apply(afkPercentageBefore));
        trends.put("afk_now", percentage.apply(afkPercentageAfter));
        trends.put("afk_trend", new Trend(afkPercentageBefore, afkPercentageAfter, Trend.REVERSED, percentage));

        Long avgRegularPlaytimeBefore = db.query(ActivityIndexQueries.averagePlaytimePerRegularPlayer(twoMonthsAgo, monthAgo, serverUUID, playThreshold));
        Long avgRegularPlaytimeAfter = db.query(ActivityIndexQueries.averagePlaytimePerRegularPlayer(monthAgo, now, serverUUID, playThreshold));
        trends.put("regular_playtime_avg_then", avgRegularPlaytimeBefore);
        trends.put("regular_playtime_avg_now", avgRegularPlaytimeAfter);
        trends.put("regular_playtime_avg_trend", new Trend(avgRegularPlaytimeBefore, avgRegularPlaytimeAfter, false));

        Long avgRegularSessionLengthBefore = db.query(ActivityIndexQueries.averageSessionLengthPerRegularPlayer(twoMonthsAgo, monthAgo, serverUUID, playThreshold));
        Long avgRegularSessionLengthAfter = db.query(ActivityIndexQueries.averageSessionLengthPerRegularPlayer(monthAgo, now, serverUUID, playThreshold));
        trends.put("regular_session_avg_then", avgRegularSessionLengthBefore);
        trends.put("regular_session_avg_now", avgRegularSessionLengthAfter);
        trends.put("regular_session_avg_trend", new Trend(avgRegularSessionLengthBefore, avgRegularSessionLengthAfter, false));

        Long avgRegularAfkBefore = db.query(ActivityIndexQueries.averageAFKPerRegularPlayer(twoMonthsAgo, monthAgo, serverUUID, playThreshold));
        Long avgRegularAfkAfter = db.query(ActivityIndexQueries.averageAFKPerRegularPlayer(monthAgo, now, serverUUID, playThreshold));
        double afkRegularPercentageBefore = Percentage.calculate(avgRegularAfkBefore, avgRegularPlaytimeBefore);
        double afkRegularPercentageAfter = Percentage.calculate(avgRegularAfkAfter, avgRegularPlaytimeAfter);
        trends.put("regular_afk_avg_then", percentage.apply(afkRegularPercentageBefore));
        trends.put("regular_afk_avg_now", percentage.apply(afkRegularPercentageAfter));
        trends.put("regular_afk_avg_trend", new Trend(afkRegularPercentageBefore, afkRegularPercentageAfter, Trend.REVERSED, percentage));

        return trends;
    }

    private Map<String, Object> createInsightsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(15L);
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