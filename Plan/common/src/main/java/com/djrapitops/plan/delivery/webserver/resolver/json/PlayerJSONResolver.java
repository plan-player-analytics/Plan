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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.rendering.json.PlayerJSONCreator;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.identification.Identifiers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class PlayerJSONResolver implements Resolver {

    private final Identifiers identifiers;
    private final PlayerJSONCreator jsonCreator;

    @Inject
    public PlayerJSONResolver(Identifiers identifiers, PlayerJSONCreator jsonCreator) {
        this.identifiers = identifiers;
        this.jsonCreator = jsonCreator;
    }

    @Override
    public boolean canAccess(Request request) {
        WebUser user = request.getUser().orElse(new WebUser(""));
        if (user.hasPermission("page.player.other")) return true;
        if (user.hasPermission("page.player.self")) {
            try {
                UUID webUserUUID = identifiers.getPlayerUUID(user.getName());
                UUID playerUUID = identifiers.getPlayerUUID(request);
                return playerUUID.equals(webUserUUID);
            } catch (BadRequestException userDoesntExist) {
                return false; // Don't give away who has played on the server to someone with level 2 access
            }
        }
        return false;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        UUID playerUUID = identifiers.getPlayerUUID(request); // Can throw BadRequestException
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(jsonCreator.createJSONAsMap(playerUUID))
                .build();
    }
}
