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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
     * Override this to tell Plan what web permissions this endpoint uses.
     * <p>
     * This allows:
     * <ul>
     *     <li>Plan to store these permissions in the permission list</li>
     *     <li>Users can grant/deny the permission for a group</li>
     *     <li>Plan can show what endpoints specific permission gives access to</li>
     * </ul>
     * <p>
     * Requires PAGE_EXTENSION_USER_PERMISSIONS capability
     *
     * @return Set of permissions eg. [plugin.custom.permission, plugin.custom.permission.child.node]
     * @see com.djrapitops.plan.capability.CapabilityService for Capability checks
     */
    default Set<String> usedWebPermissions() {
        return new HashSet<>();
    }

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

    /**
     * Creates a new {@link ResponseBuilder} for a {@link Response}.
     *
     * @return a new builder.
     */
    default ResponseBuilder newResponseBuilder() {
        return Response.builder();
    }

    /**
     * Used to check if the resolver requires authentication to be used.
     *
     * @param request Incoming request that you can use to figure out if authentication is required.
     * @return true if you want 401 to be given when user has not logged in.
     */
    default boolean requiresAuth(Request request) {
        return true;
    }
}
