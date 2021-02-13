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

import com.djrapitops.plan.delivery.rendering.html.Html;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIPath;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.identification.UUIDUtility;
import org.apache.commons.lang3.StringUtils;

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
        boolean isOwnPage = path.getPart(1).map(user.getName()::equalsIgnoreCase).orElse(true);
        return user.hasPermission("page.player.other") || (user.hasPermission("page.player.self") && isOwnPage);
    }

    @Override
    public Optional<Response> resolve(Request request) {
        URIPath path = request.getPath();
        if (StringUtils.containsAny(path.asString(), "/vendor/", "/js/", "/css/", "/img/")) {
            return Optional.empty();
        }
        return path.getPart(1)
                .map(playerName -> getResponse(request.getPath(), playerName));
    }

    private Response getResponse(URIPath path, String playerName) {
        UUID playerUUID = uuidUtility.getUUIDOf(playerName);
        if (playerUUID == null) return responseFactory.uuidNotFound404();

        boolean raw = path.getPart(2).map("raw"::equalsIgnoreCase).orElse(false);
        if (raw) {
            return responseFactory.rawPlayerPageResponse(playerUUID);
        }

        if (path.getPart(2).isPresent()) {
            // Redirect /player/Name/ to /player/Name
            return responseFactory.redirectResponse("../" + Html.encodeToURL(playerName));
        }
        return responseFactory.playerPageResponse(playerUUID);
    }
}
