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

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.mutators.*;
import com.djrapitops.plan.data.store.objects.DateMap;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.analysis.ActivityIndexQueries;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayerContainersQuery;
import com.djrapitops.plan.db.access.queries.objects.*;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.bar.BarGraph;
import com.djrapitops.plan.utilities.html.graphs.line.LineGraphFactory;
import com.djrapitops.plan.utilities.html.graphs.line.PingGraph;
import com.djrapitops.plan.utilities.html.graphs.pie.Pie;
import com.djrapitops.plan.utilities.html.graphs.pie.WorldPie;
import com.djrapitops.plan.utilities.html.graphs.special.WorldMap;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraph;
import com.djrapitops.plugin.api.TimeAmount;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Perses Graph related Data JSON.
 *
 * @author Rsl1122
 */
@Singleton
public class GraphJSONParser {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final TimeZone timeZone;

    @Inject
    public GraphJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            Graphs graphs
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.graphs = graphs;
        this.timeZone = config.get(TimeSettings.USE_SERVER_TIME) ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT");
    }

    public String performanceGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        TPSMutator tpsMutator = new TPSMutator(db.query(TPSQueries.fetchTPSDataOfServer(serverUUID)))
                .filterDataBetween(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180L), System.currentTimeMillis());
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

    public String uniqueAndNewGraphJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        SessionsMutator sessionsMutator = new SessionsMutator(db.query(SessionQueries.fetchSessionsOfServerFlat(serverUUID)))
                .filterSessionsBetween(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180L), System.currentTimeMillis());
        PlayersMutator playersMutator = new PlayersMutator(db.query(new ServerPlayerContainersQuery(serverUUID)))
                .filterRegisteredBetween(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180L), System.currentTimeMillis());

        return "{\"uniquePlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPointsWithRemovedOffset(sessionsMutator.uniqueJoinsPerDay(timeZone), timeZone)).toHighChartsSeries() +
                ",\"newPlayers\":" +
                lineGraphs.lineGraph(MutatorFunctions.toPointsWithRemovedOffset(playersMutator.newPerDay(timeZone), timeZone)).toHighChartsSeries() +
                '}';
    }

    public String serverCalendarJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        SessionsMutator sessionsMutator = new SessionsMutator(db.query(SessionQueries.fetchSessionsOfServerFlat(serverUUID)))
                .filterSessionsBetween(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(730L), System.currentTimeMillis());
        PlayersMutator playersMutator = new PlayersMutator(db.query(new ServerPlayerContainersQuery(serverUUID)));

        return "{\"data\":" +
                graphs.calendar().serverCalendar(
                        playersMutator,
                        sessionsMutator.uniqueJoinsPerDay(timeZone),
                        playersMutator.newPerDay(timeZone)
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

    public String pingGraphsJSON(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        List<Ping> pings = db.query(PingQueries.fetchPingDataOfServer(now - TimeUnit.DAYS.toMillis(180L), now, serverUUID));

        PingGraph pingGraph = graphs.line().pingGraph(new PingMutator(pings).mutateToByMinutePings().all());// TODO Optimize in query

        return "{\"min_ping_series\":" + pingGraph.getMinGraph().toHighChartsSeries() +
                ",\"avg_ping_series\":" + pingGraph.getAvgGraph().toHighChartsSeries() +
                ",\"max_ping_series\":" + pingGraph.getMaxGraph().toHighChartsSeries() + '}';
    }
}