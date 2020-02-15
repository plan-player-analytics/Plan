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

import java.util.Optional;

/**
 * Special Resolver that gives responses without user authentication.
 *
 * @author Rsl1122
 */
public interface NoAuthResolver extends Resolver {

    default boolean canAccess(WebUser permissions, URIPath target, URIQuery query) {
        return true;
    }

    /**
     * Implement request resolution.
     *
     * @param target Target that is being accessed, /example/target
     * @param query  Parameters in the URL, ?param=value etc.
     * @return Response or empty if the response should be 404 (not found).
     * @see Response for return value
     */
    Optional<Response> resolve(URIPath target, URIQuery query);

    default ResponseBuilder newResponseBuilder() {
        return Response.builder();
    }

    default boolean requiresAuth(URIPath target, URIQuery query) {
        return false;
    }
}
