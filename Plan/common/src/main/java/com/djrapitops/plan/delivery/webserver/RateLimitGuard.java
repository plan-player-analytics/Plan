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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.utilities.dev.Untrusted;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Simple guard against DDoS attacks to single endpoint.
 * <p>
 * This only protects against a DDoS that doesn't follow redirects.
 *
 * @author AuroraLS3
 */
public class RateLimitGuard {

    private static final int ATTEMPT_LIMIT = 30;
    private final Cache<String, Integer> requests = Caffeine.newBuilder()
            .expireAfterWrite(120, TimeUnit.SECONDS)
            .build();
    private final Cache<String, String> lastRequestPath = Caffeine.newBuilder()
            .expireAfterWrite(120, TimeUnit.SECONDS)
            .build();

    public boolean shouldPreventRequest(@Untrusted String requestPath, @Untrusted String accessor) {
        String previous = lastRequestPath.getIfPresent(accessor);
        if (!Objects.equals(previous, requestPath)) {
            resetAttemptCount(accessor);
        }

        Integer attempts = requests.getIfPresent(accessor);
        if (attempts == null) {
            attempts = 0;
        }

        lastRequestPath.put(accessor, requestPath);
        requests.put(accessor, attempts + 1);

        // Too many attempts, forbid further attempts.
        return attempts + 1 >= ATTEMPT_LIMIT;
    }

    public void resetAttemptCount(@Untrusted String accessor) {
        // previous request changed
        requests.invalidate(accessor);
        requests.cleanUp();
    }

    public static class Disabled extends RateLimitGuard {
        @Override
        public boolean shouldPreventRequest(@Untrusted String requestedPath, String accessor) {
            return false;
        }

        @Override
        public void resetAttemptCount(String accessor) { /* Disabled */ }
    }

}
