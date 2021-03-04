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
package com.djrapitops.plan.delivery.web.resolver;

import com.djrapitops.plan.delivery.web.resolver.request.Request;

import java.util.Optional;

/**
 * Special Resolver that gives responses without user authentication.
 *
 * @author AuroraLS3
 */
public interface NoAuthResolver extends Resolver {

    default boolean canAccess(Request request) {
        return true;
    }

    /**
     * Implement request resolution.
     *
     * @param request HTTP request, contains all information necessary to resolve the request.
     * @return Response or empty if the response should be 404 (not found).
     * @see Response for return value
     */
    Optional<Response> resolve(Request request);

    @Override
    default boolean requiresAuth(Request request) {
        return false;
    }
}
