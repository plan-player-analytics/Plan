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
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves /player/${name/uuid} URLs.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayerPageResolver implements Resolver {

    private final ResponseFactory responseFactory;
    private final UUIDUtility uuidUtility;

    @Inject
    public PlayerPageResolver(
            PlanConfig config,
            ResponseFactory responseFactory,
            UUIDUtility uuidUtility
    ) {
        this.responseFactory = responseFactory;
        this.uuidUtility = uuidUtility;
    }

    @Override
    public boolean canAccess(Request request) {
        URIPath path = request.getPath();
        WebUser user = request.getUser().orElse(new WebUser(""));
        boolean isOwnPage = isOwnPage(path, user);
        boolean raw = path.getPart(2).map("raw"::equalsIgnoreCase).orElse(false);
        boolean canSeeNormalPage = user.hasPermission(WebPermission.ACCESS_PLAYER)
                || user.hasPermission(WebPermission.ACCESS_PLAYER_SELF) && isOwnPage;
        return canSeeNormalPage && !raw || user.hasPermission(WebPermission.ACCESS_RAW_PLAYER_DATA);
    }

    @NotNull
    private Boolean isOwnPage(@Untrusted URIPath path, WebUser user) {
        return path.getPart(1).map(nameOrUUID -> {
            if (user.getName().equalsIgnoreCase(nameOrUUID)) return true; // name matches user
            return uuidUtility.getNameOf(nameOrUUID).map(user.getName()::equalsIgnoreCase) // uuid matches user
                    .orElse(false); // uuid or name don't match
        }).orElse(true); // No name or UUID given
    }

    @Override
    public Optional<Response> resolve(Request request) {
        @Untrusted URIPath path = request.getPath();
        if (Strings.CS.containsAny(path.asString(), "/vendor/", "/js/", "/css/", "/img/", "/static/")) {
            return Optional.empty();
        }
        return path.getPart(1)
                .map(playerName -> getResponse(request, playerName));
    }

    private Response getResponse(@Untrusted Request request, @Untrusted String playerName) {
        @Untrusted URIPath path = request.getPath();
        UUID playerUUID = uuidUtility.getUUIDOf(playerName);
        if (playerUUID == null) return responseFactory.uuidNotFound404();

        boolean raw = path.getPart(2).map("raw"::equalsIgnoreCase).orElse(false);
        if (raw) {
            return responseFactory.rawPlayerPageResponse(playerUUID);
        }

        return responseFactory.playerPageResponse(request, playerUUID);
    }
}
