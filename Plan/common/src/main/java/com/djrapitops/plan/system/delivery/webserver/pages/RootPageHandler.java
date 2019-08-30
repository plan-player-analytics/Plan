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
package com.djrapitops.plan.system.delivery.webserver.pages;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.delivery.webserver.Request;
import com.djrapitops.plan.system.delivery.webserver.RequestTarget;
import com.djrapitops.plan.system.delivery.webserver.WebServer;
import com.djrapitops.plan.system.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.system.delivery.webserver.response.RedirectResponse;
import com.djrapitops.plan.system.delivery.webserver.response.Response;
import com.djrapitops.plan.system.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.identification.ServerInfo;

import java.util.Optional;

/**
 * PageHandler for / page (Address root).
 * <p>
 * Not Available if Authentication is not enabled.
 *
 * @author Rsl1122
 */
public class RootPageHandler implements PageHandler {

    private final ResponseFactory responseFactory;
    private final WebServer webServer;
    private final ServerInfo serverInfo;

    public RootPageHandler(ResponseFactory responseFactory, WebServer webServer, ServerInfo serverInfo) {
        this.responseFactory = responseFactory;
        this.webServer = webServer;
        this.serverInfo = serverInfo;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        if (!webServer.isAuthRequired()) {
            return responseFactory.redirectResponse(serverInfo.getServer().isProxy() ? "/network" : "/server");
        }

        Optional<Authentication> auth = request.getAuth();
        if (!auth.isPresent()) {
            return responseFactory.basicAuth();
        }

        WebUser webUser = auth.get().getWebUser();

        int permLevel = webUser.getPermLevel();
        switch (permLevel) {
            case 0:
                return new RedirectResponse(serverInfo.getServer().isProxy() ? "/network" : "/server");
            case 1:
                return new RedirectResponse("/players");
            case 2:
                return new RedirectResponse("/player/" + webUser.getName());
            default:
                return responseFactory.forbidden403();
        }
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) {
        return true;
    }
}
