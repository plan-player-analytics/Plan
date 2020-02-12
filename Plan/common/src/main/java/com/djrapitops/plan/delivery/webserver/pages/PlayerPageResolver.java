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
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Resolves /player/${name/uuid} URLs.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerPageResolver implements PageResolver {

    private final ResponseFactory responseFactory;
    private final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;

    @Inject
    public PlayerPageResolver(
            ResponseFactory responseFactory,
            DBSystem dbSystem,
            UUIDUtility uuidUtility
    ) {
        this.responseFactory = responseFactory;
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
    }

    @Override
    public Response_old resolve(Request request, RequestTarget target) throws WebException {
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
            return responseFactory.rawPlayerPageResponse(playerUUID);
        }
        return responseFactory.playerPageResponse(playerUUID);
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        WebUser_old webUser = auth.getWebUser();
        return webUser.getPermLevel() <= 1 || webUser.getName().equalsIgnoreCase(target.get(target.size() - 1));
    }
}
