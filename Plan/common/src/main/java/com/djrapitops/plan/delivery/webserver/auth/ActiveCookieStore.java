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

import com.djrapitops.plan.delivery.domain.WebUser;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ActiveCookieStore {

    private static final Map<String, WebUser> USERS_BY_COOKIE = new HashMap<>();

    public static Optional<WebUser> checkCookie(String cookie) {
        return Optional.ofNullable(USERS_BY_COOKIE.get(cookie));
    }

    public static String generateNewCookie(WebUser user) {
        String cookie = DigestUtils.sha256Hex(user.getName() + UUID.randomUUID() + System.currentTimeMillis());
        USERS_BY_COOKIE.put(cookie, user);
        return cookie;
    }

    public static void removeCookie(String cookie) {
        USERS_BY_COOKIE.remove(cookie);
    }

    public static void removeCookie(WebUser user) {
        USERS_BY_COOKIE.entrySet().stream().filter(entry -> entry.getValue().getName().equals(user.getName()))
                .findAny()
                .map(Map.Entry::getKey)
                .ifPresent(ActiveCookieStore::removeCookie);
    }
}