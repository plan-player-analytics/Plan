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

import java.util.Objects;

public class ServerIdentifier {

    private final ServerUUID uuid;
    private final String name;

    public ServerIdentifier(ServerUUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public ServerIdentifier(ServerUUID serverUUID, ServerName serverName) {
        this(serverUUID, serverName.get());
    }

    public ServerUUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isSame(ServerIdentifier that) {
        return Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerIdentifier that = (ServerIdentifier) o;
        return Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getName());
    }

    @Override
    public String toString() {
        return "ServerIdentifier{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                '}';
    }

    public String toJson() {
        return "{\"name\": \"" + name + "\", \"uuid\": {\"uuid\": \"" + uuid + "\"}}";
    }
}
