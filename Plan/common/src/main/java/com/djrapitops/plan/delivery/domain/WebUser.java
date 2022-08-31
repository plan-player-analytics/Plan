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
package com.djrapitops.plan.delivery.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Object containing webserver security user information.
 *
 * @author AuroraLS3
 * @deprecated Use {@link com.djrapitops.plan.delivery.domain.auth.User} instead
 * TODO Rewrite Authentication stuff
 */
@Deprecated(since = "2022-02-12, User.java")
public class WebUser {

    private final String username;
    private final String saltedPassHash;
    private final int permLevel;

    public WebUser(String username, String saltedPassHash, int permLevel) {
        this.username = username;
        this.saltedPassHash = saltedPassHash;
        this.permLevel = permLevel;
    }

    public static List<String> getPermissionsForLevel(int level) {
        List<String> permissions = new ArrayList<>();
        if (level <= 0) {
            permissions.add("page.network");
            permissions.add("page.server");
            permissions.add("page.debug");
            // TODO Add JSON Permissions
        }
        if (level <= 1) {
            permissions.add("page.players");
            permissions.add("page.player.other");
        }
        if (level <= 2) {
            permissions.add("page.player.self");
        }
        return permissions;
    }

    public String getSaltedPassHash() {
        return saltedPassHash;
    }

    public int getPermLevel() {
        return permLevel;
    }

    public String getName() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebUser webUser = (WebUser) o;
        return permLevel == webUser.permLevel &&
                Objects.equals(username, webUser.username) &&
                Objects.equals(saltedPassHash, webUser.saltedPassHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, saltedPassHash, permLevel);
    }

    public com.djrapitops.plan.delivery.web.resolver.request.WebUser toNewWebUser() {
        return new com.djrapitops.plan.delivery.web.resolver.request.WebUser(
                username, getPermissionsForLevel(permLevel).toArray(new String[0])
        );
    }
}
