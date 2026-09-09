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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Interface for asynchronously resolving requests of Plan webserver.
 *
 * @author AuroraLS3
 */
public interface AsyncResolver extends Resolver {

    /**
     * Implement asynchronous request resolution.
     *
     * @param request HTTP request, contains all information necessary to resolve the request.
     * @return Future of Optional Response or empty if the response should be 404 (not found).
     * @see Response for return value
     * @see Request#getPath() for path /example/path etc
     * @see Request#getQuery() for parameters ?param=value etc
     */
    CompletableFuture<Optional<Response>> resolveAsync(Request request);

    @Override
    default Optional<Response> resolve(Request request) {
        try {
            return resolveAsync(request).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw e;
        }
    }
}
