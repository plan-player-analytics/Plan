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
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.Collections;
import java.util.UUID;

/**
 * @author AuroraLS3
 */
public class IncompleteRegistration {
    @Untrusted
    private final String username;
    private final String passwordHash;
    private final String code;
    private final long expiresAfter;

    public IncompleteRegistration(@Untrusted String username, String passwordHash, String code, long expiresAfter) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.code = code;
        this.expiresAfter = expiresAfter;
    }

    public User toUser(UUID linkedToUUID) {
        return new User(username, null, linkedToUUID, passwordHash, null, Collections.emptyList());
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getCode() {
        return code;
    }

    public long getExpiresAfter() {
        return expiresAfter;
    }
}
