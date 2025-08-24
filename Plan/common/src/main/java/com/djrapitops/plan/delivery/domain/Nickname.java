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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.Objects;

/**
 * Object storing nickname information.
 *
 * @author AuroraLS3
 */
public class Nickname implements DateHolder {

    @Untrusted
    private final String name;
    private final long date;
    private final ServerUUID serverUUID;

    public Nickname(String name, long date, ServerUUID serverUUID) {
        this.name = name;
        this.date = date;
        this.serverUUID = serverUUID;
    }

    @Untrusted
    public String getName() {
        return name.length() <= 75 ? name : name.substring(0, 74);
    }

    @Override
    public long getDate() {
        return date;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    @Override
    public String toString() {
        return "Nickname{" +
                "name='" + name + '\'' +
                ", date=" + date +
                ", serverUUID=" + serverUUID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nickname)) return false;
        Nickname nickname = (Nickname) o;
        return Objects.equals(getName(), nickname.getName()) &&
                Objects.equals(serverUUID, nickname.serverUUID);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, date, serverUUID);
    }
}