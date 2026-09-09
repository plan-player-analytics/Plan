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

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.UUID;

public class PlayerIdentifier implements Comparable<PlayerIdentifier> {
    private final UUID uuid;
    private final String name;

    public PlayerIdentifier(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isSame(PlayerIdentifier that) {
        return Objects.equals(this, that);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerIdentifier that = (PlayerIdentifier) o;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getName());
    }

    @Override
    public String toString() {
        return "PlayerIdentifier{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                '}';
    }

    public String toJson() {
        return "{\"name\": \"" + name + "\", \"uuid\": \"" + uuid + "\"}";
    }

    @Override
    public int compareTo(@NonNull PlayerIdentifier o) {
        return String.CASE_INSENSITIVE_ORDER.compare(name, o.name);
    }
}
