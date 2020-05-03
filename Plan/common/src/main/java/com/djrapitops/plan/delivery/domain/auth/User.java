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
package com.djrapitops.plan.delivery.domain.auth;

import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.utilities.PassEncryptUtil;

import java.util.Collection;
import java.util.UUID;

/**
 * Represents a registered user in the database.
 *
 * @author Rsl1122
 */
public class User {

    private final String username;
    private final String linkedTo;
    private final UUID linkedToUUID; // null for 'console'
    private final String passwordHash;
    private final Collection<String> permissions;

    public User(String username, String linkedTo, UUID linkedToUUID, String passwordHash, Collection<String> permissions) {
        this.username = username;
        this.linkedTo = linkedTo;
        this.linkedToUUID = linkedToUUID;
        this.passwordHash = passwordHash;
        this.permissions = permissions;
    }

    public boolean doesPasswordMatch(String password) {
        return PassEncryptUtil.verifyPassword(password, passwordHash);
    }

    public WebUser toWebUser() {
        return new WebUser(linkedTo, username, permissions);
    }

    public String getUsername() {
        return username;
    }

    public String getLinkedTo() {
        return linkedTo;
    }

    public UUID getLinkedToUUID() {
        return linkedToUUID;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
