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
package com.djrapitops.plan.system.webserver.pages.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
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
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.data.JSONResponse;
import com.djrapitops.plan.utilities.html.graphs.Graphs;
import com.djrapitops.plan.utilities.html.graphs.line.LineGraphFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * JSON handler for different graph data JSON requests.
 *
 * @author Rsl1122
 */
@Singleton
public class GraphsJSONHandler extends ServerParameterJSONHandler {

    private final Graphs graphs;
    private final TimeZone timeZone;

    @Inject
    public GraphsJSONHandler(
            PlanConfig config,
            DBSystem dbSystem,
            Graphs graphs
    ) {
        super(dbSystem);
        this.graphs = graphs;

        this.timeZone = config.get(TimeSettings.USE_SERVER_TIME) ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT");
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        UUID serverUUID = getServerUUID(target); // Can throw BadRequestException
        String type = target.getParameter("type")
                .orElseThrow(() -> new BadRequestException("'type' parameter was not defined."));
        String graphDataJSON = generateGraphDataJSONOfType(type, serverUUID);
        return new JSONResponse(graphDataJSON);
    }

    private String generateGraphDataJSONOfType(String type, UUID serverUUID) throws BadRequestException {
        Database db = dbSystem.getDatabase();
        LineGraphFactory lineGraphs = graphs.line();
        switch (type) {
            case "performance":
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
            case "uniqueAndNew":
                SessionsMutator sessionsMutator = new SessionsMutator(db.query(SessionQueries.fetchSessionsOfServerFlat(serverUUID)))
                        .filterSessionsBetween(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180L), System.currentTimeMillis());
                PlayersMutator playersMutator = new PlayersMutator(db.query(new ServerPlayerContainersQuery(serverUUID)))
                        .filterRegisteredBetween(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(180L), System.currentTimeMillis());

                return "{\"uniquePlayers\":" +
                        lineGraphs.lineGraph(MutatorFunctions.toPointsWithRemovedOffset(sessionsMutator.uniqueJoinsPerDay(timeZone), timeZone)).toHighChartsSeries() +
                        ",\"newPlayers\":" +
                        lineGraphs.lineGraph(MutatorFunctions.toPointsWithRemovedOffset(playersMutator.newPerDay(timeZone), timeZone)).toHighChartsSeries() +
                        '}';
            default:
                throw new BadRequestException("unknown 'type' parameter: " + type);
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}