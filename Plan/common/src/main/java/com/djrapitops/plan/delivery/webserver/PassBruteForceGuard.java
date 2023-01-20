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

import java.util.concurrent.TimeUnit;

/**
 * Guards against password brute-force break attempts.
 *
 * @author AuroraLS3
 */
public class PassBruteForceGuard {

    private static final int ATTEMPT_LIMIT = 3;
    private final Cache<String, Integer> failedLoginAttempts = Caffeine.newBuilder()
            .expireAfterWrite(90, TimeUnit.SECONDS)
            .build();

    public boolean shouldPreventRequest(@Untrusted String accessor) {
        Integer attempts = failedLoginAttempts.getIfPresent(accessor);
        if (attempts == null) return false;
        // Too many attempts, forbid further attempts.
        return attempts >= ATTEMPT_LIMIT;
    }

    // Don't call on first connection.
    public void increaseAttemptCountOnFailedLogin(@Untrusted String accessor) {
        // Authentication was attempted, but failed so new attempt is going to be given if not forbidden
        failedLoginAttempts.cleanUp();

        Integer attempts = failedLoginAttempts.getIfPresent(accessor);
        if (attempts == null) {
            attempts = 0;
        }

        // Too many attempts, forbid further attempts.
        if (attempts >= ATTEMPT_LIMIT) {
            // Attempts only increased if less than ATTEMPT_LIMIT attempts to prevent frustration from the cache timer resetting.
            return;
        }

        failedLoginAttempts.put(accessor, attempts + 1);
    }

    public void resetAttemptCount(@Untrusted String accessor) {
        // Successful login
        failedLoginAttempts.cleanUp();
        failedLoginAttempts.invalidate(accessor);
    }

    public static class Disabled extends PassBruteForceGuard {
        @Override
        public boolean shouldPreventRequest(String accessor) {
            return false;
        }

        @Override
        public void increaseAttemptCountOnFailedLogin(String accessor) { /* Disabled */ }

        @Override
        public void resetAttemptCount(String accessor) { /* Disabled */ }
    }

}
