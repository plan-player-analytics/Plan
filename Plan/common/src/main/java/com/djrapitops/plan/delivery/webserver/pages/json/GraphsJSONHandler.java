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
package com.djrapitops.plan.delivery.webserver.pages.json;

import com.djrapitops.plan.delivery.rendering.json.graphs.GraphJSONParser;
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.delivery.webserver.pages.PageHandler;
import com.djrapitops.plan.delivery.webserver.response.Response;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.BadRequestException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.UUID;

/**
 * JSON handler for different graph data JSON requests.
 *
 * @author Rsl1122
 */
@Singleton
public class GraphsJSONHandler implements PageHandler {

    private final Identifiers identifiers;
    private final GraphJSONParser graphJSON;

    @Inject
    public GraphsJSONHandler(
            Identifiers identifiers,
            GraphJSONParser graphJSON
    ) {
        this.identifiers = identifiers;
        this.graphJSON = graphJSON;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        String type = target.getParameter("type")
                .orElseThrow(() -> new BadRequestException("'type' parameter was not defined."));

        DataID dataID = getDataID(type);

        if (target.getParameter("server").isPresent()) {
            UUID serverUUID = identifiers.getServerUUID(target); // Can throw BadRequestException
            return JSONCache.getOrCache(dataID, serverUUID, () -> generateGraphDataJSONOfType(dataID, serverUUID));
        }
        // Assume network
        return JSONCache.getOrCache(dataID, () -> generateGraphDataJSONOfType(dataID));
    }

    private DataID getDataID(String type) throws BadRequestException {
        switch (type) {
            case "performance": return DataID.GRAPH_PERFORMANCE;
            case "playersOnline": return DataID.GRAPH_ONLINE;
            case "uniqueAndNew": return DataID.GRAPH_UNIQUE_NEW;
            case "serverCalendar": return DataID.GRAPH_CALENDAR;
            case "worldPie": return DataID.GRAPH_WORLD_PIE;
            case "activity": return DataID.GRAPH_ACTIVITY;
            case "geolocation": return DataID.GRAPH_WORLD_MAP;
            case "aggregatedPing": return DataID.GRAPH_PING;
            case "punchCard": return DataID.GRAPH_PUNCHCARD;
            case "serverPie": return DataID.GRAPH_SERVER_PIE;
            default: throw new BadRequestException("unknown 'type' parameter: " + type);
        }
    }

    private JSONResponse generateGraphDataJSONOfType(DataID id, UUID serverUUID) {
        switch (id) {
            case GRAPH_PERFORMANCE:
                return new JSONResponse(graphJSON.performanceGraphJSON(serverUUID));
            case GRAPH_ONLINE:
                return new JSONResponse(graphJSON.playersOnlineGraph(serverUUID));
            case GRAPH_UNIQUE_NEW:
                return new JSONResponse(graphJSON.uniqueAndNewGraphJSON(serverUUID));
            case GRAPH_CALENDAR:
                return new JSONResponse(graphJSON.serverCalendarJSON(serverUUID));
            case GRAPH_WORLD_PIE:
                return new JSONResponse(graphJSON.serverWorldPieJSONAsMap(serverUUID));
            case GRAPH_ACTIVITY:
                return new JSONResponse(graphJSON.activityGraphsJSONAsMap(serverUUID));
            case GRAPH_WORLD_MAP:
                return new JSONResponse(graphJSON.geolocationGraphsJSONAsMap(serverUUID));
            case GRAPH_PING:
                return new JSONResponse(graphJSON.pingGraphsJSON(serverUUID));
            case GRAPH_PUNCHCARD:
                return new JSONResponse(graphJSON.punchCardJSONAsMap(serverUUID));
            default:
                return new JSONResponse(Collections.singletonMap("error", "Undefined ID: " + id.name()));
        }
    }

    private JSONResponse generateGraphDataJSONOfType(DataID id) {
        switch (id) {
            case GRAPH_ACTIVITY:
                return new JSONResponse(graphJSON.activityGraphsJSONAsMap());
            case GRAPH_UNIQUE_NEW:
                return new JSONResponse(graphJSON.uniqueAndNewGraphJSON());
            case GRAPH_SERVER_PIE:
                return new JSONResponse(graphJSON.serverPreferencePieJSONAsMap());
            case GRAPH_WORLD_MAP:
                return new JSONResponse(graphJSON.geolocationGraphsJSONAsMap());
            default:
                return new JSONResponse(Collections.singletonMap("error", "Undefined ID: " + id.name()));
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}