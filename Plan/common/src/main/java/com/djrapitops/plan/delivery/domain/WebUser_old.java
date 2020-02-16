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

import com.djrapitops.plan.delivery.web.resolver.request.WebUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Object containing webserver security user information.
 *
 * @author Rsl1122
 */
@Deprecated
public class WebUser_old {

    private final String user;
    private final String saltedPassHash;
    private final int permLevel;

    public WebUser_old(String user, String saltedPassHash, int permLevel) {
        this.user = user;
        this.saltedPassHash = saltedPassHash;
        this.permLevel = permLevel;
    }

    public String getName() {
        return user;
    }

    public String getSaltedPassHash() {
        return saltedPassHash;
    }

    public int getPermLevel() {
        return permLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebUser_old webUser = (WebUser_old) o;
        return permLevel == webUser.permLevel &&
                Objects.equals(user, webUser.user) &&
                Objects.equals(saltedPassHash, webUser.saltedPassHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, saltedPassHash, permLevel);
    }

    public WebUser toNewWebUser() {
        List<String> permissions = new ArrayList<>();
        if (permLevel <= 0) {
            permissions.add("page.network");
            permissions.add("page.server");
            permissions.add("page.debug");
            // TODO Add JSON Permissions
        }
        if (permLevel <= 1) {
            permissions.add("page.players");
            permissions.add("page.player.other");
        }
        if (permLevel <= 2) {
            permissions.add("page.player.self");
        }
        return new WebUser(
                user, permissions.toArray(new String[0])
        );
    }
}
