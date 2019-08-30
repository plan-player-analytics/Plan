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
package com.djrapitops.plan.system.webserver.pages;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.ResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * PageHandler for /server and /network pages.
 *
 * @author Rsl1122
 */
@Singleton
public class ServerPageHandler implements PageHandler {

    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public ServerPageHandler(
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            ServerInfo serverInfo
    ) {
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        UUID serverUUID = getServerUUID(target);

        Response response = ResponseCache.loadResponse(PageId.SERVER.of(serverUUID));

        if (response != null) {
            return response;
        } else {
            checkDBState();
            if (serverInfo.getServer().isProxy() && serverInfo.getServerUUID().equals(serverUUID)) {
                return ResponseCache.loadResponse(PageId.SERVER.of(serverUUID), responseFactory::networkPageResponse);
            }
            Response serverPageResponse = responseFactory.serverPageResponse(serverUUID);
            ResponseCache.cacheResponse(PageId.SERVER.of(serverUUID), serverPageResponse);
            return serverPageResponse;
        }
    }

    private void checkDBState() throws ForbiddenException {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new ForbiddenException("Database is " + dbState.name() + " - Please try again later. You can check database status with /plan info");
        }
    }

    private UUID getServerUUID(RequestTarget target) {
        // Default to current server's page
        UUID serverUUID = serverInfo.getServerUUID();

        if (!target.isEmpty()) {
            try {
                String serverName = target.get(0);
                Optional<UUID> serverUUIDOptional = dbSystem.getDatabase()
                        .query(ServerQueries.fetchServerMatchingIdentifier(serverName))
                        .map(Server::getUuid);
                if (serverUUIDOptional.isPresent()) {
                    serverUUID = serverUUIDOptional.get();
                }
            } catch (IllegalArgumentException ignore) {
                /*ignored*/
            }
        }
        return serverUUID;
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }
}
