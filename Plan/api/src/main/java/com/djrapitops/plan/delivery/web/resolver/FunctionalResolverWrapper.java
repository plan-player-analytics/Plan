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
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class for constructing a {@link Resolver} with Functional Interfaces.
 */
public class FunctionalResolverWrapper implements Resolver {

    private final Function<Request, Optional<Response>> resolver;
    private final Predicate<Request> accessCheck;

    /**
     * Default constructor.
     *
     * @param resolver    Function that solves the {@link Request} into an Optional {@link Response}.
     * @param accessCheck Predicate that checks if {@link Request} is allowed or if 403 Forbidden should be given.
     */
    public FunctionalResolverWrapper(Function<Request, Optional<Response>> resolver, Predicate<Request> accessCheck) {
        this.resolver = resolver;
        this.accessCheck = accessCheck;
    }

    @Override
    public boolean canAccess(Request request) {
        return accessCheck.test(request);
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return resolver.apply(request);
    }
}
