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

    private final Formatter<Long> timeAmount;
    private final Formatter<Double> percentage;

    @Inject
    public NetworkPlayerBaseOverviewJSONCreator(
            PlanConfig config,
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.config = config;
        this.dbSystem = dbSystem;

        timeAmount = formatters.timeAmount();
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

        Integer playersBefore = db.query(PlayerCountQueries.newPlayerCount(0L, monthAgo));
        Integer playersAfter = db.query(PlayerCountQueries.newPlayerCount(0L, now));
        trends.put("total_players_then", playersBefore);
        trends.put("total_players_now", playersAfter);
        trends.put("total_players_trend", new Trend(playersBefore, playersAfter, false));

        Integer regularBefore = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(monthAgo, playThreshold));
        Integer regularAfter = db.query(NetworkActivityIndexQueries.fetchRegularPlayerCount(now, playThreshold));
        trends.put("regular_players_then", regularBefore);
        trends.put("regular_players_now", regularAfter);
        trends.put("regular_players_trend", new Trend(regularBefore, regularAfter, false));

        Long avgPlaytimeBefore = db.query(SessionQueries.averagePlaytimePerPlayer(twoMonthsAgo, monthAgo));
        Long avgPlaytimeAfter = db.query(SessionQueries.averagePlaytimePerPlayer(monthAgo, now));
        trends.put("playtime_avg_then", timeAmount.apply(avgPlaytimeBefore));
        trends.put("playtime_avg_now", timeAmount.apply(avgPlaytimeAfter));
        trends.put("playtime_avg_trend", new Trend(avgPlaytimeBefore, avgPlaytimeAfter, false, timeAmount));

        Long avgAfkBefore = db.query(SessionQueries.averageAfkPerPlayer(twoMonthsAgo, monthAgo));
        Long avgAfkAfter = db.query(SessionQueries.averageAfkPerPlayer(monthAgo, now));
        double afkPercBefore = avgPlaytimeBefore != 0 ? (double) avgAfkBefore / avgPlaytimeBefore : 0;
        double afkPercAfter = avgPlaytimeAfter != 0 ? (double) avgAfkAfter / avgPlaytimeAfter : 0;
        trends.put("afk_then", percentage.apply(afkPercBefore));
        trends.put("afk_now", percentage.apply(afkPercAfter));
        trends.put("afk_trend", new Trend(afkPercBefore, afkPercAfter, Trend.REVERSED, percentage));

        Long avgRegularPlaytimeBefore = db.query(NetworkActivityIndexQueries.averagePlaytimePerRegularPlayer(twoMonthsAgo, monthAgo, playThreshold));
        Long avgRegularPlaytimeAfter = db.query(NetworkActivityIndexQueries.averagePlaytimePerRegularPlayer(monthAgo, now, playThreshold));
        trends.put("regular_playtime_avg_then", timeAmount.apply(avgRegularPlaytimeBefore));
        trends.put("regular_playtime_avg_now", timeAmount.apply(avgRegularPlaytimeAfter));
        trends.put("regular_playtime_avg_trend", new Trend(avgRegularPlaytimeBefore, avgRegularPlaytimeAfter, false, timeAmount));

        Long avgRegularSessionLengthBefore = db.query(NetworkActivityIndexQueries.averageSessionLengthPerRegularPlayer(twoMonthsAgo, monthAgo, playThreshold));
        Long avgRegularSessionLengthAfter = db.query(NetworkActivityIndexQueries.averageSessionLengthPerRegularPlayer(monthAgo, now, playThreshold));
        trends.put("regular_session_avg_then", timeAmount.apply(avgRegularSessionLengthBefore));
        trends.put("regular_session_avg_now", timeAmount.apply(avgRegularSessionLengthAfter));
        trends.put("regular_session_avg_trend", new Trend(avgRegularSessionLengthBefore, avgRegularSessionLengthAfter, false, timeAmount));

        Long avgRegularAfkBefore = db.query(NetworkActivityIndexQueries.averageAFKPerRegularPlayer(twoMonthsAgo, monthAgo, playThreshold));
        Long avgRegularAfkAfter = db.query(NetworkActivityIndexQueries.averageAFKPerRegularPlayer(monthAgo, now, playThreshold));
        double afkRegularPercBefore = avgRegularPlaytimeBefore != 0 ? (double) avgRegularAfkBefore / avgRegularPlaytimeBefore : 0;
        double afkRegularPercAfter = avgRegularPlaytimeAfter != 0 ? (double) avgRegularAfkAfter / avgRegularPlaytimeAfter : 0;
        trends.put("regular_afk_avg_then", percentage.apply(afkRegularPercBefore));
        trends.put("regular_afk_avg_now", percentage.apply(afkRegularPercAfter));
        trends.put("regular_afk_avg_trend", new Trend(afkRegularPercBefore, afkRegularPercAfter, Trend.REVERSED, percentage));

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
        insights.put("new_to_regular", newToRegular);
        insights.put("new_to_regular_trend", new Trend(newToRegularBefore, newToRegularAfter, false));

        Integer regularToInactive = db.query(NetworkActivityIndexQueries.countRegularPlayersTurnedInactive(monthAgo, now, playThreshold));
        Integer regularToInactiveBefore = db.query(NetworkActivityIndexQueries.countRegularPlayersTurnedInactive(monthAgo, halfMonthAgo, playThreshold));
        Integer regularToInactiveAfter = db.query(NetworkActivityIndexQueries.countRegularPlayersTurnedInactive(halfMonthAgo, now, playThreshold));
        insights.put("regular_to_inactive", regularToInactive);
        insights.put("regular_to_inactive_trend", new Trend(regularToInactiveBefore, regularToInactiveAfter, Trend.REVERSED));

        return insights;
    }
}