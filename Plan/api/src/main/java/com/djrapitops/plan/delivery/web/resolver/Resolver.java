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
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;

import java.util.Optional;

/**
 * Interface for resolving requests of Plan webserver.
 *
 * @author AuroraLS3
 * @see NoAuthResolver if resource is always accessible regardless of user.
 */
public interface Resolver {

    /**
     * Implement access control if authorization is enabled.
     * <p>
     * Is not called when access control is not active.
     *
     * @param request HTTP request, contains all information necessary to check access.
     * @return true if allowed or invalid target, false if response should be 403 (forbidden)
     * @see Request#getUser() for {@link WebUser} that has access permissions.
     */
    boolean canAccess(Request request);

    /**
     * Implement request resolution.
     *
     * @param request HTTP request, contains all information necessary to resolve the request.
     * @return Response or empty if the response should be 404 (not found).
     * @see Response for return value
     * @see Request#getPath() for path /example/path etc
     * @see Request#getQuery() for parameters ?param=value etc
     */
    Optional<Response> resolve(Request request);

    default ResponseBuilder newResponseBuilder() {
        return Response.builder();
    }

    default boolean requiresAuth(Request request) {
        return true;
    }
}
