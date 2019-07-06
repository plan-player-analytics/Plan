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
import com.djrapitops.plan.system.Identifiers;
import com.djrapitops.plan.system.json.GraphJSONParser;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.PageHandler;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.data.JSONResponse;

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
        UUID serverUUID = identifiers.getServerUUID(target); // Can throw BadRequestException
        String type = target.getParameter("type")
                .orElseThrow(() -> new BadRequestException("'type' parameter was not defined."));
        String graphDataJSON = generateGraphDataJSONOfType(type, serverUUID);
        return new JSONResponse(graphDataJSON);
    }

    private String generateGraphDataJSONOfType(String type, UUID serverUUID) throws BadRequestException {
        switch (type) {
            case "performance":
                return graphJSON.performanceGraphJSON(serverUUID);
            case "uniqueAndNew":
                return graphJSON.uniqueAndNewGraphJSON(serverUUID);
            case "serverCalendar":
                return graphJSON.serverCalendarJSON(serverUUID);
            default:
                throw new BadRequestException("unknown 'type' parameter: " + type);
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}