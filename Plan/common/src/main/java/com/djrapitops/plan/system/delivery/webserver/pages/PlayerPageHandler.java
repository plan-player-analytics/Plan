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

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.delivery.domain.WebUser;
import com.djrapitops.plan.system.delivery.webserver.Request;
import com.djrapitops.plan.system.delivery.webserver.RequestTarget;
import com.djrapitops.plan.system.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.system.delivery.webserver.cache.PageId;
import com.djrapitops.plan.system.delivery.webserver.cache.ResponseCache;
import com.djrapitops.plan.system.delivery.webserver.response.Response;
import com.djrapitops.plan.system.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.system.identification.UUIDUtility;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * PageHandler for /player/PlayerName pages.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerPageHandler implements PageHandler {

    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;

    @Inject
    public PlayerPageHandler(
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            UUIDUtility uuidUtility
    ) {
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
    }

    @Override
    public Response getResponse(Request request, RequestTarget target) throws WebException {
        if (target.isEmpty()) {
            return responseFactory.pageNotFound404();
        }

        String playerName = target.get(0);
        UUID playerUUID = uuidUtility.getUUIDOf(playerName);

        boolean raw = target.size() >= 2 && target.get(1).equalsIgnoreCase("raw");

        if (playerUUID == null) {
            return responseFactory.uuidNotFound404();
        }
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new ForbiddenException("Database is " + dbState.name() + " - Please try again later. You can check database status with /plan info");
        }
        if (raw) {
            return ResponseCache.loadResponse(PageId.RAW_PLAYER.of(playerUUID), () -> responseFactory.rawPlayerPageResponse(playerUUID));
        }
        return ResponseCache.loadResponse(PageId.PLAYER.of(playerUUID),
                () -> responseFactory.playerPageResponse(playerUUID));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        WebUser webUser = auth.getWebUser();
        return webUser.getPermLevel() <= 1 || webUser.getName().equalsIgnoreCase(target.get(target.size() - 1));
    }
}
