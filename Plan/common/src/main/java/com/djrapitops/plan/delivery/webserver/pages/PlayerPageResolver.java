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

import com.djrapitops.plan.delivery.web.resolver.*;
import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;
import com.djrapitops.plan.identification.UUIDUtility;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves /player/${name/uuid} URLs.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerPageResolver implements Resolver {

    private final ResponseFactory responseFactory;
    private final UUIDUtility uuidUtility;

    @Inject
    public PlayerPageResolver(
            ResponseFactory responseFactory,
            UUIDUtility uuidUtility
    ) {
        this.responseFactory = responseFactory;
        this.uuidUtility = uuidUtility;
    }

    @Override
    public boolean canAccess(WebUser user, URIPath target, URIQuery query) {
        boolean isOwnPage = target.getPart(1).map(user.getName()::equalsIgnoreCase).orElse(true);
        return user.hasPermission("page.player.other") || (user.hasPermission("page.player.self") && isOwnPage);
    }

    @Override
    public Optional<Response> resolve(URIPath target, URIQuery query) {
        Optional<String> part = target.getPart(1);
        if (!part.isPresent()) return Optional.empty();

        String playerName = part.get();
        UUID playerUUID = uuidUtility.getUUIDOf(playerName);
        if (playerUUID == null) return Optional.of(responseFactory.uuidNotFound404());

        boolean raw = target.getPart(2).map("raw"::equalsIgnoreCase).orElse(false);
        return Optional.of(
                raw ? responseFactory.rawPlayerPageResponse(playerUUID)
                        : responseFactory.playerPageResponse(playerUUID)
        );
    }
}
