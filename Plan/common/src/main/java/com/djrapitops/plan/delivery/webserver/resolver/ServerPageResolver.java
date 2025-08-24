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
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Resolves /network, /server and /server/${name/uuid} URLs.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerPageResolver implements Resolver {

    private static final String NETWORK_PAGE = "network";

    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public ServerPageResolver(
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public boolean canAccess(Request request) {
        @Untrusted String firstPart = request.getPath().getPart(0).orElse("");
        WebUser user = request.getUser().orElse(new WebUser(""));
        boolean forServerPage = "server".equalsIgnoreCase(firstPart) && user.hasPermission(WebPermission.ACCESS_SERVER);
        boolean forNetworkPage = NETWORK_PAGE.equalsIgnoreCase(firstPart) && user.hasPermission(WebPermission.ACCESS_NETWORK);
        return forServerPage || forNetworkPage;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return getServerUUID(request.getPath())
                .map(serverUUID -> getServerPage(serverUUID, request))
                .orElseGet(this::redirectToCurrentServer);
    }

    private Optional<Response> redirectToCurrentServer() {
        String directTo = serverInfo.getServer().isProxy()
                ? "/" + NETWORK_PAGE
                : "/server/" + Html.encodeToURL(serverInfo.getServer().getIdentifiableName());
        return Optional.of(responseFactory.redirectResponse(directTo));
    }

    private Optional<Response> getServerPage(ServerUUID serverUUID, @Untrusted Request request) {
        boolean toNetworkPage = serverInfo.getServer().isProxy() && serverInfo.getServerUUID().equals(serverUUID);
        if (toNetworkPage) {
            if (request.getPath().getPart(0).map(NETWORK_PAGE::equals).orElse(false)) {
                return Optional.of(responseFactory.reactPageResponse(request));
            } else {
                // Accessing /server/Server <Bungee ID> which should be redirected to /network
                return redirectToCurrentServer();
            }
        }
        return Optional.of(responseFactory.serverPageResponse(request, serverUUID));
    }

    private Optional<ServerUUID> getServerUUID(@Untrusted URIPath path) {
        if (serverInfo.getServer().isProxy()
                && path.getPart(0).map(NETWORK_PAGE::equals).orElse(false)
        ) {
            return Optional.of(serverInfo.getServerUUID());
        }
        return path.getPart(1).flatMap(serverName -> dbSystem.getDatabase()
                .query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                .map(Server::getUuid));
    }
}
