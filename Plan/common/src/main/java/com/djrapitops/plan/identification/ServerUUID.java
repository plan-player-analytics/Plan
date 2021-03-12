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
package com.djrapitops.plan.identification;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ServerUUID implements Serializable {
    private final UUID uuid;

    protected ServerUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public static ServerUUID from(UUID uuid) {
        return new ServerUUID(uuid);
    }

    public static ServerUUID fromString(String name) {
        return new ServerUUID(UUID.fromString(name));
    }

    public static ServerUUID randomUUID() {
        return ServerUUID.from(UUID.randomUUID());
    }

    public UUID asUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        if (o.getClass().equals(UUID.class)) {
            return uuid.equals(o);
        }

        if (getClass() != o.getClass()) return false;
        ServerUUID that = (ServerUUID) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}