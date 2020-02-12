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

import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.WebServer;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves /network, /server and /server/${name/uuid} URLs.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerPageResolver implements PageResolver {

    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Lazy<WebServer> webServer;

    @Inject
    public ServerPageResolver(
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Lazy<WebServer> webServer
    ) {
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.webServer = webServer;
    }

    @Override
    public Response_old resolve(Request request, RequestTarget target) throws WebException {
        Optional<UUID> serverUUID = getServerUUID(target);
        boolean proxy = serverInfo.getServer().isProxy();
        if (serverUUID.isPresent()) {
            checkDBState();
            if (proxy && serverInfo.getServerUUID().equals(serverUUID.get())) {
                return responseFactory.networkPageResponse();
            }
            return responseFactory.serverPageResponse(serverUUID.get());
        } else {
            // Redirect to base server page.
            String directTo = proxy ? "/network" : "/server/" + Html.encodeToURL(serverInfo.getServer().getIdentifiableName());
            return responseFactory.redirectResponse(webServer.get().getAccessAddress() + directTo);
        }
    }

    private void checkDBState() throws ForbiddenException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new ForbiddenException("Database is " + dbState.name() + " - Please try again later. You can check database status with /plan info");
        }
    }

    private Optional<UUID> getServerUUID(RequestTarget target) {
        if (!target.isEmpty()) {
            try {
                String serverName = target.get(0);
                return dbSystem.getDatabase()
                        .query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                        .map(Server::getUuid);
            } catch (IllegalArgumentException ignore) {
                /*ignored*/
            }
        }
        return Optional.of(serverInfo.getServer().getUuid());
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}
