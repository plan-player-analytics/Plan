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
package com.djrapitops.plan.system.delivery.webserver.pages.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.BadRequestException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.delivery.webserver.Request;
import com.djrapitops.plan.system.delivery.webserver.RequestTarget;
import com.djrapitops.plan.system.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.system.delivery.webserver.pages.PageHandler;
import com.djrapitops.plan.system.delivery.webserver.response.Response;
import com.djrapitops.plan.system.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.system.identification.Identifiers;
import com.djrapitops.plan.system.json.GraphJSONParser;

import javax.inject.Inject;
import javax.inject.Singleton;
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

        if (target.getParameter("server").isPresent()) {
            UUID serverUUID = identifiers.getServerUUID(target); // Can throw BadRequestException
            return generateGraphDataJSONOfType(type, serverUUID);
        }
        // Assume network
        return generateGraphDataJSONOfType(type);
    }

    private JSONResponse generateGraphDataJSONOfType(String type, UUID serverUUID) throws BadRequestException {
        switch (type) {
            case "performance":
                return new JSONResponse(graphJSON.performanceGraphJSON(serverUUID));
            case "playersOnline":
                return new JSONResponse(graphJSON.playersOnlineGraph(serverUUID));
            case "uniqueAndNew":
                return new JSONResponse(graphJSON.uniqueAndNewGraphJSON(serverUUID));
            case "serverCalendar":
                return new JSONResponse(graphJSON.serverCalendarJSON(serverUUID));
            case "worldPie":
                return new JSONResponse(graphJSON.serverWorldPieJSONAsMap(serverUUID));
            case "activity":
                return new JSONResponse(graphJSON.activityGraphsJSONAsMap(serverUUID));
            case "geolocation":
                return new JSONResponse(graphJSON.geolocationGraphsJSONAsMap(serverUUID));
            case "aggregatedPing":
                return new JSONResponse(graphJSON.pingGraphsJSON(serverUUID));
            case "punchCard":
                return new JSONResponse(graphJSON.punchCardJSONAsMap(serverUUID));
            default:
                throw new BadRequestException("unknown 'type' parameter: " + type);
        }
    }

    private JSONResponse generateGraphDataJSONOfType(String type) throws BadRequestException {
        switch (type) {
            case "activity":
                return new JSONResponse(graphJSON.activityGraphsJSONAsMap());
            case "uniqueAndNew":
                return new JSONResponse(graphJSON.uniqueAndNewGraphJSON());
            case "serverPie":
                return new JSONResponse(graphJSON.serverPreferencePieJSONAsMap());
            case "geolocation":
                return new JSONResponse(graphJSON.geolocationGraphsJSONAsMap());
            default:
                throw new BadRequestException("unknown 'type' parameter: " + type);
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}