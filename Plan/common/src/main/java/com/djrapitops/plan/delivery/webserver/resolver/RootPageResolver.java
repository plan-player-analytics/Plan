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
package com.djrapitops.plan.delivery.webserver.resolver;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves '/' URL (Address Root).
 *
 * @author AuroraLS3
 */
@Singleton
public class RootPageResolver implements NoAuthResolver {

    private static final String NETWORK_PAGE = "network";

    private final ResponseFactory responseFactory;
    private final Lazy<WebServer> webServer;
    private final ServerInfo serverInfo;

    @Inject
    public RootPageResolver(ResponseFactory responseFactory, Lazy<WebServer> webServer, ServerInfo serverInfo) {
        this.responseFactory = responseFactory;
        this.webServer = webServer;
        this.serverInfo = serverInfo;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        Server server = serverInfo.getServer();
        if (!webServer.get().isAuthRequired()) {
            String redirectTo = server.isProxy() ? NETWORK_PAGE : "server/" + Html.encodeToURL(server.getIdentifiableName());
            return responseFactory.redirectResponse(redirectTo);
        }

        WebUser user = request.getUser()
                .orElseThrow(() -> new WebUserAuthException(FailReason.EXPIRED_COOKIE));

        if (server.isProxy() && user.hasPermission(WebPermission.ACCESS_NETWORK)) {
            return responseFactory.redirectResponse(NETWORK_PAGE);
        } else if (user.hasPermission(WebPermission.ACCESS_SERVER)) {
            return responseFactory.redirectResponse(server.isProxy() ? NETWORK_PAGE : "server/" + Html.encodeToURL(server.getIdentifiableName()));
        } else if (user.hasPermission(WebPermission.ACCESS_PLAYERS)) {
            return responseFactory.redirectResponse("players");
        } else if (user.hasPermission(WebPermission.ACCESS_PLAYER_SELF)) {
            return responseFactory.redirectResponse("player/" + user.getUUID().map(UUID::toString).orElseGet(user::getName));
        } else if (user.hasPermission(WebPermission.ACCESS_QUERY)) {
            return responseFactory.redirectResponse("query");
        } else if (user.hasPermission(WebPermission.MANAGE_GROUPS)) {
            return responseFactory.redirectResponse("manage");
        } else if (user.hasPermission(WebPermission.ACCESS_DOCS)) {
            return responseFactory.redirectResponse("docs");
        } else if (user.hasPermission(WebPermission.ACCESS_ERRORS)) {
            return responseFactory.redirectResponse("errors");
        } else {
            return responseFactory.forbidden403("User has insufficient permissions to be redirected to any page.");
        }
    }
}
