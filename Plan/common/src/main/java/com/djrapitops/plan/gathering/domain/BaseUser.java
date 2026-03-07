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
package com.djrapitops.plan.gathering.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents user information stored in plan_users.
 * <p>
 * Only one per player exists unlike {@link UserInfo} which is available per server.
 *
 * @author AuroraLS3
 */
public class BaseUser {

    private final UUID uuid;
    private final String name;
    private final long registered;
    private final int timesKicked;
    private Integer id;

    public BaseUser(UUID uuid, String name, long registered, int timesKicked) {
        if (uuid == null) throw new IllegalArgumentException("'uuid' can not be null");
        if (name == null) throw new IllegalArgumentException("'name' can not be null");

        this.uuid = uuid;
        this.name = name;
        this.registered = registered;
        this.timesKicked = timesKicked;
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

    public int getTimesKicked() {
        return timesKicked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseUser)) return false;
        BaseUser baseUser = (BaseUser) o;
        return uuid.equals(baseUser.uuid) && name.equals(baseUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name);
    }

    @Override
    public String toString() {
        return "BaseUser{" +
                uuid +
                ", '" + name + '\'' +
                ", +" + registered +
                ", kick:" + timesKicked +
                '}';
    }
}