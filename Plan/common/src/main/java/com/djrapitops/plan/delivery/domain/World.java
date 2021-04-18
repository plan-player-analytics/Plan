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

/**
 * Represents a world stored in the Plan database.
 */
public class World {

    private final String worldName;
    private final ServerUUID serverUUID;

    public World(String worldName, ServerUUID serverUUID) {
        this.worldName = worldName;
        this.serverUUID = serverUUID;
    }

    public String getWorldName() {
        return worldName;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        World world = (World) o;
        return Objects.equals(getWorldName(), world.getWorldName()) && Objects.equals(getServerUUID(), world.getServerUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorldName(), getServerUUID());
    }

    @Override
    public String toString() {
        return "World{" +
                "worldName='" + worldName + '\'' +
                ", serverUUID=" + serverUUID +
                '}';
    }
}
