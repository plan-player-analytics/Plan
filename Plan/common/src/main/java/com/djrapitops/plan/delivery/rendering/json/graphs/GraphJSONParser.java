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
package com.djrapitops.plan.delivery.rendering.json.graphs;

import com.djrapitops.plan.delivery.domain.DateMap;
import com.djrapitops.plan.delivery.domain.mutators.MutatorFunctions;
import com.djrapitops.plan.delivery.domain.mutators.PingMutator;
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.delivery.rendering.json.graphs.bar.BarGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.PingGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.Pie;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.delivery.rendering.json.graphs.special.WorldMap;
import com.djrapitops.plan.delivery.rendering.json.graphs.stack.StackGraph;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;
import com.djrapitops.plan.storage.database.queries.objects.*;
import com.djrapitops.plugin.api.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Perses Graph related Data JSON.
 *
 * @author Rsl1122
 */
@Singleton
public class GraphJSONParser {

    private final PlanConfig config;
    private final Theme theme;
    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final TimeZone timeZone;

    @Inject
    public GraphJSONParser(
            PlanConfig config,
            Theme theme,
            DBSystem dbSystem,
            Graphs graphs
    ) {
        this.config = config;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.graphs = graphs;
        this.timeZone = config.getTimeZone();
    }

    public String performanceGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(serverUUID)))
                .filterDataBetween(halfYearAgo, now);
        return '{' +
                "\"playersOnline\":" + lineGraphs.playersOnlineGraph(tpsMutator).toHighChartsSeries() +
                ",\"tps\":" + lineGraphs.tpsGraph(tpsMutator).toHighChartsSeries() +
                ",\"cpu\":" + lineGraphs.cpuGraph(tpsMutator).toHighChartsSeries() +
                ",\"ram\":" + lineGraphs.ramGraph(tpsMutator).toHighChartsSeries() +
                ",\"entities\":" + lineGraphs.entityGraph(tpsMutator).toHighChartsSeries() +
                ",\"chunks\":" + lineGraphs.chunkGraph(tpsMutator).toHighChartsSeries() +
                ",\"disk\":" + lineGraphs.diskGraph(tpsMutator).toHighChartsSeries() +
                '}';
    }

    public String playersOnlineGraph(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);

        List<Point> points = db.query(TPSQueries.fetchPlayersOnlineOfServer(halfYearAgo, now, serverUUID)).stream()
                .map(point -> new Point(point.getDate(), point.getValue()))
                .collect(Collectors.toList());
        return "{\"playersOnline\":" + graphs.line().lineGraph(points).toHighChartsSeries() + '}';
    }

    public String uniqueAndNewGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(halfYearAgo, now, timeZone.getOffset(now), serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(halfYearAgo, now, timeZone.getOffset(now), serverUUID)
        );

        return "{\"uniquePlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(uniquePerDay, TimeUnit.DAYS.toMillis(1L), 0)
                )).toHighChartsSeries() +
                ",\"newPlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(newPerDay, TimeUnit.DAYS.toMillis(1L), 0)
                )).toHighChartsSeries() +
                '}';
    }

    public String uniqueAndNewGraphJSON() {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        long now = System.currentTimeMillis();
        long halfYearAgo = now - TimeUnit.DAYS.toMillis(180L);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(halfYearAgo, now, timeZone.getOffset(now))
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(halfYearAgo, now, timeZone.getOffset(now))
        );

        return "{\"uniquePlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(uniquePerDay, TimeUnit.DAYS.toMillis(1L), 0)
                )).toHighChartsSeries() +
                ",\"newPlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPoints(
                        MutatorFunctions.addMissing(newPerDay, TimeUnit.DAYS.toMillis(1L), 0)
                )).toHighChartsSeries() +
                '}';
    }

    public String serverCalendarJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long twoYearsAgo = now - TimeUnit.DAYS.toMillis(730L);
        NavigableMap<Long, Integer> uniquePerDay = db.query(
                PlayerCountQueries.uniquePlayerCounts(twoYearsAgo, now, timeZone.getOffset(now), serverUUID)
        );
        NavigableMap<Long, Integer> newPerDay = db.query(
                PlayerCountQueries.newPlayerCounts(twoYearsAgo, now, timeZone.getOffset(now), serverUUID)
        );
        NavigableMap<Long, Long> playtimePerDay = db.query(
                SessionQueries.playtimePerDay(twoYearsAgo, now, timeZone.getOffset(now), serverUUID)
        );
        NavigableMap<Long, Integer> sessionsPerDay = db.query(
                SessionQueries.sessionCountPerDay(twoYearsAgo, now, timeZone.getOffset(now), serverUUID)
        );
        return "{\"data\":" +
                graphs.calendar().serverCalendar(
                        uniquePerDay,
                        newPerDay,
                        playtimePerDay,
                        sessionsPerDay
                ).toCalendarSeries() +
                ",\"firstDay\":" + 1 + '}';
    }

    public Map<String, Object> serverWorldPieJSONAsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        WorldTimes worldTimes = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID));
        WorldPie worldPie = graphs.pie().worldPie(worldTimes);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("world_series", worldPie.getSlices());
        dataMap.put("gm_series", worldPie.toHighChartsDrillDownMaps());
        return dataMap;
    }

    public Map<String, Object> activityGraphsJSONAsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long date = System.currentTimeMillis();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, db.query(ActivityIndexQueries.fetchActivityIndexGroupingsOn(time, serverUUID, threshold)));
        }

        Map.Entry<Long, Map<String, Integer>> lastActivityEntry = activityData.lastEntry();
        Pie activityPie = graphs.pie().activityPie(lastActivityEntry != null ? lastActivityEntry.getValue() : Collections.emptyMap());
        StackGraph activityStackGraph = graphs.stack().activityStackGraph(activityData);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("activity_series", activityStackGraph.getDataSets());
        dataMap.put("activity_labels", activityStackGraph.getLabels());
        dataMap.put("activity_pie_series", activityPie.getSlices());
        return dataMap;
    }

    public Map<String, Object> activityGraphsJSONAsMap() {
        Database db = dbSystem.getDatabase();
        long date = System.currentTimeMillis();
        Long threshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        DateMap<Map<String, Integer>> activityData = new DateMap<>();
        for (long time = date; time >= date - TimeAmount.MONTH.toMillis(2L); time -= TimeAmount.WEEK.toMillis(1L)) {
            activityData.put(time, db.query(NetworkActivityIndexQueries.fetchActivityIndexGroupingsOn(time, threshold)));
        }

        Map.Entry<Long, Map<String, Integer>> lastActivityEntry = activityData.lastEntry();
        Pie activityPie = graphs.pie().activityPie(lastActivityEntry != null ? lastActivityEntry.getValue() : Collections.emptyMap());
        StackGraph activityStackGraph = graphs.stack().activityStackGraph(activityData);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("activity_series", activityStackGraph.getDataSets());
        dataMap.put("activity_labels", activityStackGraph.getLabels());
        dataMap.put("activity_pie_series", activityPie.getSlices());
        return dataMap;
    }

    public Map<String, Object> geolocationGraphsJSONAsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        Map<String, Integer> geolocationCounts = db.query(GeoInfoQueries.serverGeolocationCounts(serverUUID));

        BarGraph geolocationBarGraph = graphs.bar().geolocationBarGraph(geolocationCounts);
        WorldMap worldMap = graphs.special().worldMap(geolocationCounts);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("geolocation_series", worldMap.getEntries());
        dataMap.put("geolocation_bar_series", geolocationBarGraph.getBars());
        return dataMap;
    }

    public Map<String, Object> geolocationGraphsJSONAsMap() {
        Database db = dbSystem.getDatabase();
        Map<String, Integer> geolocationCounts = db.query(GeoInfoQueries.networkGeolocationCounts());

        BarGraph geolocationBarGraph = graphs.bar().geolocationBarGraph(geolocationCounts);
        WorldMap worldMap = graphs.special().worldMap(geolocationCounts);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("geolocation_series", worldMap.getEntries());
        dataMap.put("geolocation_bar_series", geolocationBarGraph.getBars());
        return dataMap;
    }

    public String pingGraphsJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        List<Ping> pings = db.query(PingQueries.fetchPingDataOfServer(now - TimeUnit.DAYS.toMillis(180L), now, serverUUID));

        PingGraph pingGraph = graphs.line().pingGraph(new PingMutator(pings).mutateToByMinutePings().all());// TODO Optimize in query

        return "{\"min_ping_series\":" + pingGraph.getMinGraph().toHighChartsSeries() +
                ",\"avg_ping_series\":" + pingGraph.getAvgGraph().toHighChartsSeries() +
                ",\"max_ping_series\":" + pingGraph.getMaxGraph().toHighChartsSeries() + '}';
    }

    public Map<String, Object> punchCardJSONAsMap(UUID serverUUID) {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        List<Session> sessions = dbSystem.getDatabase().query(
                SessionQueries.fetchServerSessionsWithoutKillOrWorldData(monthAgo, now, serverUUID)
        );
        return Collections.singletonMap("punchCard", graphs.special().punchCard(sessions).getDots());
    }

    public Map<String, Object> serverPreferencePieJSONAsMap() {
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);
        String[] pieColors = theme.getPieColors(ThemeVal.GRAPH_WORLD_PIE);
        Map<String, Object> data = new HashMap<>();
        data.put("server_pie_colors", pieColors);

        Map<String, Long> serverPlaytimes = dbSystem.getDatabase().query(SessionQueries.playtimePerServer(now, monthAgo));
        data.put("server_pie_series_30d", graphs.pie().serverPreferencePie(serverPlaytimes).getSlices());
        return data;
    }
}