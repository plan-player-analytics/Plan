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

import com.djrapitops.plan.data.store.mutators.MutatorFunctions;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.queries.containers.ServerPlayerContainersQuery;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.TPSQueries;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.line.LineGraphFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Perses Graph related Data JSON.
 *
 * @author Rsl1122
 */
@Singleton
public class GraphJSONParser {

    private final DBSystem dbSystem;
    private final Graphs graphs;
    private final TimeZone timeZone;

    @Inject
    public GraphJSONParser(
            PlanConfig config,
            DBSystem dbSystem,
            Graphs graphs
    ) {
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
}