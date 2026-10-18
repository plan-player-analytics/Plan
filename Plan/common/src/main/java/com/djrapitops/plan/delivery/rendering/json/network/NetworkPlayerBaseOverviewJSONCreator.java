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
package com.djrapitops.plan.delivery.rendering.json.network;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.json.Trend;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.utilities.analysis.Percentage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /network-page Playerbase Overview tab.
 *
 * @author AuroraLS3
 */
@Singleton
public class NetworkPlayerBaseOverviewJSONCreator implements NetworkTabJSONCreator<Map<String, Object>> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    private final Formatter<Double> percentage;

    @Inject
    public NetworkPlayerBaseOverviewJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        percentage = formatters.percentage();
    }

    public Map<String, Object> createJSONAsMap() {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("trends", createTrendsMap());
        serverOverview.put("insights", createInsightsMap());
        return serverOverview;
    }

    private Map<String, Object> createTrendsMap() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        long twoMonthsAgo = now - TimeUnit.DAYS.toMillis(60L);
        Long playThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> trends = new HashMap<>();

        int playersBefore = db.query(PlayerCountQueries.newPlayerCount(0L, monthAgo));
        int playersAfter = db.query(PlayerCountQueries.newPlayerCount(0L, now));
        trends.put("totalPlayersThen", playersBefore);
        trends.put("totalPlayersNow", playersAfter);
        trends.put("totalPlayersTrend", new Trend(playersBefore, playersAfter, false));

        int regularBefore = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(monthAgo, playThreshold));
        int regularAfter = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(now, playThreshold));
        trends.put("regularPlayersThen", regularBefore);
        trends.put("regularPlayersNow", regularAfter);
        trends.put("regularPlayersTrend", new Trend(regularBefore, regularAfter, false));

        long avgPlaytimeBefore = db.query(SessionQueries.averagePlaytimePerPlayer(twoMonthsAgo, monthAgo));
        long avgPlaytimeAfter = db.query(SessionQueries.averagePlaytimePerPlayer(monthAgo, now));
        trends.put("playtimeAvgThen", avgPlaytimeBefore);
        trends.put("playtimeAvgNow", avgPlaytimeAfter);
        trends.put("playtimeAvgTrend", new Trend(avgPlaytimeBefore, avgPlaytimeAfter, false));

        long avgAfkBefore = db.query(SessionQueries.averageAfkPerPlayer(twoMonthsAgo, monthAgo));
        long avgAfkAfter = db.query(SessionQueries.averageAfkPerPlayer(monthAgo, now));
        double afkPercentageBefore = Percentage.calculate(avgAfkBefore, avgPlaytimeBefore);
        double afkPercentageAfter = Percentage.calculate(avgAfkAfter, avgPlaytimeAfter);
        trends.put("afkThen", percentage.apply(afkPercentageBefore));
        trends.put("afkNow", percentage.apply(afkPercentageAfter));
        trends.put("afkTrend", new Trend(afkPercentageBefore, afkPercentageAfter, Trend.REVERSED, percentage));

        long avgRegularPlaytimeBefore = db.query(NetworkActivityIndexQueries.averagePlaytimePerRegularPlayer(twoMonthsAgo, monthAgo, playThreshold));
        long avgRegularPlaytimeAfter = db.query(NetworkActivityIndexQueries.averagePlaytimePerRegularPlayer(monthAgo, now, playThreshold));
        trends.put("regularPlaytimeAvgThen", avgRegularPlaytimeBefore);
        trends.put("regularPlaytimeAvgNow", avgRegularPlaytimeAfter);
        trends.put("regularPlaytimeAvgTrend", new Trend(avgRegularPlaytimeBefore, avgRegularPlaytimeAfter, false));

        long avgRegularSessionLengthBefore = db.query(NetworkActivityIndexQueries.averageSessionLengthPerRegularPlayer(twoMonthsAgo, monthAgo, playThreshold));
        long avgRegularSessionLengthAfter = db.query(NetworkActivityIndexQueries.averageSessionLengthPerRegularPlayer(monthAgo, now, playThreshold));
        trends.put("regularSessionAvgThen", avgRegularSessionLengthBefore);
        trends.put("regularSessionAvgNow", avgRegularSessionLengthAfter);
        trends.put("regularSessionAvgTrend", new Trend(avgRegularSessionLengthBefore, avgRegularSessionLengthAfter, false));

        long avgRegularAfkBefore = db.query(NetworkActivityIndexQueries.averageAFKPerRegularPlayer(twoMonthsAgo, monthAgo, playThreshold));
        long avgRegularAfkAfter = db.query(NetworkActivityIndexQueries.averageAFKPerRegularPlayer(monthAgo, now, playThreshold));
        double afkRegularPercentageBefore = Percentage.calculate(avgRegularAfkBefore, avgRegularPlaytimeBefore);
        double afkRegularPercentageAfter = Percentage.calculate(avgRegularAfkAfter, avgRegularPlaytimeAfter);
        trends.put("regularAfkAvgThen", percentage.apply(afkRegularPercentageBefore));
        trends.put("regularAfkAvgNow", percentage.apply(afkRegularPercentageAfter));
        trends.put("regularAfkAvgTrend", new Trend(afkRegularPercentageBefore, afkRegularPercentageAfter, Trend.REVERSED, percentage));

        return trends;
    }

    private Map<String, Object> createInsightsMap() {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfMonthAgo = now - TimeUnit.DAYS.toMillis(15L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        Long playThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        Map<String, Object> insights = new HashMap<>();

        int newToRegular = db.query(NetworkActivityIndexQueries.countNewPlayersTurnedRegular(monthAgo, now, playThreshold));
        Integer newToRegularBefore = db.query(NetworkActivityIndexQueries.countNewPlayersTurnedRegular(monthAgo, halfMonthAgo, playThreshold));
        Integer newToRegularAfter = db.query(NetworkActivityIndexQueries.countNewPlayersTurnedRegular(halfMonthAgo, now, playThreshold));
        insights.put("newToRegular", newToRegular);
        insights.put("newToRegularTrend", new Trend(newToRegularBefore, newToRegularAfter, false));

        Integer regularToInactive = db.query(NetworkActivityIndexQueries.countRegularPlayersTurnedInactive(monthAgo, now, playThreshold));
        Integer regularToInactiveBefore = db.query(NetworkActivityIndexQueries.countRegularPlayersTurnedInactive(monthAgo, halfMonthAgo, playThreshold));
        Integer regularToInactiveAfter = db.query(NetworkActivityIndexQueries.countRegularPlayersTurnedInactive(halfMonthAgo, now, playThreshold));
        insights.put("regularToInactive", regularToInactive);
        insights.put("regularToInactiveTrend", new Trend(regularToInactiveBefore, regularToInactiveAfter, Trend.REVERSED));

        return insights;
    }
}