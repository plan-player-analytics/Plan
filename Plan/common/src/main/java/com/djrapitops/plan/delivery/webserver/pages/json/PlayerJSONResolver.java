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
package com.djrapitops.plan.delivery.webserver.pages.json;

import com.djrapitops.plan.delivery.domain.WebUser_old;
import com.djrapitops.plan.delivery.rendering.json.PlayerJSONCreator;
import com.djrapitops.plan.delivery.webserver.Request;
import com.djrapitops.plan.delivery.webserver.RequestTarget;
import com.djrapitops.plan.delivery.webserver.auth.Authentication;
import com.djrapitops.plan.delivery.webserver.pages.PageResolver;
import com.djrapitops.plan.delivery.webserver.response.Response_old;
import com.djrapitops.plan.delivery.webserver.response.data.JSONResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.WebException;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class PlayerJSONResolver implements PageResolver {

    private final Identifiers identifiers;
    private final PlayerJSONCreator jsonCreator;

    @Inject
    public PlayerJSONResolver(Identifiers identifiers, PlayerJSONCreator jsonCreator) {
        this.identifiers = identifiers;
        this.jsonCreator = jsonCreator;
    }

    @Override
    public Response_old resolve(Request request, RequestTarget target) throws WebException {
        UUID playerUUID = identifiers.getPlayerUUID(target); // Can throw BadRequestException
        return new JSONResponse(jsonCreator.createJSONAsMap(playerUUID));
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        WebUser_old webUser = auth.getWebUser();
        return webUser.getPermLevel() <= 1 || webUser.getName().equalsIgnoreCase(target.get(target.size() - 1));

    }
}
