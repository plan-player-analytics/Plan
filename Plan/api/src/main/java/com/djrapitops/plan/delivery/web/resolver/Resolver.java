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

public interface Resolver {

    /**
     * Implement access control if authorization is enabled.
     * <p>
     * Is not called when access control is not active.
     *
     * @param permissions WebUser that is accessing this page.
     * @param target      Target that is being accessed, /example/target
     * @param query       Parameters in the URL, ?param=value etc.
     * @return true if allowed or invalid target, false if response should be 403 (forbidden)
     */
    boolean canAccess(WebUser permissions, URIPath target, URIQuery query);

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

    /**
     * Override this method with false to always allow using this resolver.
     * <p>
     * Use this when content/style is needed for displaying pages where authentication is not available/needed.
     *
     * @param target Target that is being accessed, /example/target
     * @param query  Parameters in the URL, ?param=value etc.
     * @return true by default. If false is returned {@link #canAccess(WebUser, URIPath, URIQuery)} will not be called.
     */
    default boolean requiresAuth(URIPath target, URIQuery query) {
        return true;
    }
}
