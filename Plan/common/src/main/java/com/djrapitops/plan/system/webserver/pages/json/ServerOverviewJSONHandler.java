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
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.json.ServerOverviewJSONParser;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.data.JSONResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * JSON handler for different Player table JSON requests.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.json.PlayersTableJSONParser For JSON parsing of /server players table.
 */
@Singleton
public class ServerOverviewJSONHandler extends ServerParameterJSONHandler {

    private final ServerOverviewJSONParser serverOverviewJSON;

    @Inject
    public ServerOverviewJSONHandler(
            DBSystem dbSystem,
            ServerOverviewJSONParser serverOverviewJSON
    ) {
        super(dbSystem);
        this.serverOverviewJSON = serverOverviewJSON;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        UUID serverUUID = getServerUUID(target); // Can throw BadRequestException
        return new JSONResponse<>(serverOverviewJSON.createServerOverviewJSONAsMap(serverUUID));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}