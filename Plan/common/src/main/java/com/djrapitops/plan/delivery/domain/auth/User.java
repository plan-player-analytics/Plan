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
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a registered user in the database.
 *
 * @author AuroraLS3
 */
public class User implements Comparable<User> {

    @Untrusted
    private final String username;
    private final String linkedTo;
    private final UUID linkedToUUID; // null for 'console'
    private final String passwordHash;
    private String permissionGroup;
    private final Collection<String> permissions;

    public User(@Untrusted String username, String linkedTo, UUID linkedToUUID, String passwordHash, String permissionGroup, Collection<String> permissions) {
        this.username = username;
        this.linkedTo = linkedTo;
        this.linkedToUUID = linkedToUUID;
        this.passwordHash = passwordHash;
        this.permissionGroup = permissionGroup;
        this.permissions = permissions;
    }

    public boolean doesPasswordMatch(@Untrusted String password) {
        return PassEncryptUtil.verifyPassword(password, passwordHash);
    }

    public WebUser toWebUser() {
        return new WebUser(linkedTo, linkedToUUID, username, permissions);
    }

    @Untrusted
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

    public String getPermissionGroup() {
        return permissionGroup;
    }

    public void setPermissionGroup(String permissionGroup) {
        this.permissionGroup = permissionGroup;
    }

    public Collection<String> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", linkedTo='" + linkedTo + '\'' +
                ", linkedToUUID=" + linkedToUUID +
                ", passwordHash='" + passwordHash + '\'' +
                ", permissionGroup=" + permissionGroup +
                ", permissions=" + permissions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(linkedTo, user.linkedTo) &&
                Objects.equals(linkedToUUID, user.linkedToUUID) &&
                Objects.equals(passwordHash, user.passwordHash) &&
                Objects.equals(permissionGroup, user.permissionGroup) &&
                Objects.equals(permissions, user.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, linkedTo, linkedToUUID, passwordHash, permissionGroup, permissions);
    }

    @Override
    public int compareTo(User other) {
        int comparison = String.CASE_INSENSITIVE_ORDER.compare(this.permissionGroup, other.permissionGroup);
        if (comparison == 0) comparison = String.CASE_INSENSITIVE_ORDER.compare(this.username, other.username);
        return comparison;
    }
}
