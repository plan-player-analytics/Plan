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

import java.util.List;
import java.util.Objects;

/**
 * Represents a list of web permissions.
 *
 * @author AuroraLS3
 */
public class WebPermissionList {

    private final List<String> permissions;

    public WebPermissionList(List<String> permissions) {
        this.permissions = permissions;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebPermissionList that = (WebPermissionList) o;
        return Objects.equals(getPermissions(), that.getPermissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPermissions());
    }

    @Override
    public String toString() {
        return "PermissionList{" +
                "permissions=" + permissions +
                '}';
    }
}
