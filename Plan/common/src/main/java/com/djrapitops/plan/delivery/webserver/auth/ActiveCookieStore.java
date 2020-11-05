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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ActiveCookieStore {

    private static final Cache<String, User> USERS_BY_COOKIE = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();

    private ActiveCookieStore() {
        // Hide static cache constructor
    }

    public static Optional<User> checkCookie(String cookie) {
        return Optional.ofNullable(USERS_BY_COOKIE.getIfPresent(cookie));
    }

    public static String generateNewCookie(User user) {
        String cookie = DigestUtils.sha256Hex(user.getUsername() + UUID.randomUUID() + System.currentTimeMillis());
        USERS_BY_COOKIE.put(cookie, user);
        return cookie;
    }

    public static void removeCookie(String cookie) {
        USERS_BY_COOKIE.invalidate(cookie);
    }

    public static void removeCookie(User user) {
        USERS_BY_COOKIE.asMap().entrySet().stream().filter(entry -> entry.getValue().getUsername().equals(user.getUsername()))
                .findAny()
                .map(Map.Entry::getKey)
                .ifPresent(ActiveCookieStore::removeCookie);
    }
}