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
package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Holds registrations of users before they are confirmed.
 *
 * @author AuroraLS3
 */
public class RegistrationBin {

    private static final Cache<String, AwaitingForRegistration> REGISTRATION_BIN = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build();

    private RegistrationBin() {
        // Hide static cache constructor
    }

    public static String addInfoForRegistration(@Untrusted String username, @Untrusted String password) {
        String hash = PassEncryptUtil.createHash(password);
        String code = DigestUtils.sha256Hex(username + password + System.currentTimeMillis()).substring(0, 12);
        REGISTRATION_BIN.put(code, new AwaitingForRegistration(username, hash));
        return code;
    }

    public static Optional<User> register(@Untrusted String code, UUID linkedToUUID) {
        AwaitingForRegistration found = REGISTRATION_BIN.getIfPresent(code);
        if (found == null) return Optional.empty();
        REGISTRATION_BIN.invalidate(code);
        return Optional.of(found.toUser(linkedToUUID));
    }

    public static boolean contains(@Untrusted String code) {
        return REGISTRATION_BIN.getIfPresent(code) != null;
    }

    private static class AwaitingForRegistration {
        @Untrusted
        private final String username;
        private final String passwordHash;

        public AwaitingForRegistration(@Untrusted String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
        }

        public User toUser(UUID linkedToUUID) {
            return new User(username, null, linkedToUUID, passwordHash, null, Collections.emptyList());
        }
    }
}
