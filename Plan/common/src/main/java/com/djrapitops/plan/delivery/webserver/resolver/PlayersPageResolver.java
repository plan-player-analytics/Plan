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
import com.djrapitops.plan.delivery.webserver.ResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Resolves /players URL.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayersPageResolver implements Resolver {

    private final ResponseFactory responseFactory;

    @Inject
    public PlayersPageResolver(
            ResponseFactory responseFactory
    ) {
        this.responseFactory = responseFactory;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().map(user -> user.hasPermission(WebPermission.ACCESS_PLAYERS)).orElse(false);
    }

    @Override
    public Optional<Response> resolve(Request request) {
        // Redirect /players/ to /players
        if (request.getPath().getPart(1).isPresent()) return Optional.of(responseFactory.redirectResponse("/players"));
        return Optional.of(responseFactory.reactPageResponse(request));
    }
}
