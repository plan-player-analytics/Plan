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

import java.util.Objects;

/**
 * Object containing webserver security user information.
 *
 * @author Rsl1122
 */
public class WebUser {

    private final String user;
    private final String saltedPassHash;
    private final int permLevel;

    public WebUser(String user, String saltedPassHash, int permLevel) {
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
        WebUser webUser = (WebUser) o;
        return permLevel == webUser.permLevel &&
                Objects.equals(user, webUser.user) &&
                Objects.equals(saltedPassHash, webUser.saltedPassHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, saltedPassHash, permLevel);
    }
}
