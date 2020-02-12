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
package com.djrapitops.plan.delivery.webserver.pages;

import com.djrapitops.plan.delivery.domain.WebUser_old;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;

import java.util.Optional;

/**
 * Resolves '/' URL (Address Root).
 *
 * @author Rsl1122
 */
public class RootPageResolver implements PageResolver {

    private final ResponseFactory responseFactory;
    private final WebServer webServer;
    private final ServerInfo serverInfo;

    public RootPageResolver(ResponseFactory responseFactory, WebServer webServer, ServerInfo serverInfo) {
        this.responseFactory = responseFactory;
        this.webServer = webServer;
        this.serverInfo = serverInfo;
    }

    @Override
    public Response_old resolve(Request request, RequestTarget target) throws WebException {
        Server server = serverInfo.getServer();
        if (!webServer.isAuthRequired()) {
            return responseFactory.redirectResponse(server.isProxy() ? "network" : "server/" + Html.encodeToURL(server.getIdentifiableName()));
        }

        Optional<Authentication> auth = request.getAuth();
        if (!auth.isPresent()) {
            return responseFactory.basicAuth();
        }

        WebUser_old webUser = auth.get().getWebUser();

        int permLevel = webUser.getPermLevel();
        switch (permLevel) {
            case 0:
                return responseFactory.redirectResponse(server.isProxy() ? "network" : "server/" + Html.encodeToURL(server.getIdentifiableName()));
            case 1:
                return responseFactory.redirectResponse("players");
            case 2:
                return responseFactory.redirectResponse("player/" + Html.encodeToURL(webUser.getName()));
            default:
                return responseFactory.forbidden403();
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) {
        return true;
    }
}
