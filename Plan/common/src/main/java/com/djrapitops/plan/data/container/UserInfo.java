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
package com.djrapitops.plan.data.container;

import java.util.Objects;
import java.util.UUID;

/**
 * Used for storing information of players after it has been fetched.
 *
 * @author Rsl1122
 */
public class UserInfo {

    private final UUID uuid;
    private String name;
    private long registered;
    private boolean banned;
    private boolean opped;

    public UserInfo(UUID uuid, String name, long registered, boolean opped, boolean banned) {
        this.uuid = uuid;
        this.name = name;
        this.registered = registered;
        this.opped = opped;
        this.banned = banned;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getRegistered() {
        return registered;
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isOperator() {
        return opped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return registered == userInfo.registered &&
                banned == userInfo.banned &&
                opped == userInfo.opped &&
                Objects.equals(uuid, userInfo.uuid) &&
                Objects.equals(name, userInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, registered, banned, opped);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", registered=" + registered +
                ", banned=" + banned +
                ", opped=" + opped +
                '}';
    }
}