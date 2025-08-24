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
package com.djrapitops.plan.delivery.domain.datatransfer;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents data returned by {@link com.djrapitops.plan.delivery.webserver.resolver.json.PlayerJoinAddressJSONResolver}.
 *
 * @author AuroraLS3
 */
public class PlayerJoinAddresses {

    private final List<String> joinAddresses;
    private final Map<UUID, String> joinAddressByPlayer;

    public PlayerJoinAddresses(List<String> joinAddresses, Map<UUID, String> joinAddressByPlayer) {
        this.joinAddresses = joinAddresses;
        this.joinAddressByPlayer = joinAddressByPlayer;
    }

    public List<String> getJoinAddresses() {
        return joinAddresses;
    }

    public Map<UUID, String> getJoinAddressByPlayer() {
        return joinAddressByPlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerJoinAddresses that = (PlayerJoinAddresses) o;
        return Objects.equals(getJoinAddresses(), that.getJoinAddresses()) && Objects.equals(getJoinAddressByPlayer(), that.getJoinAddressByPlayer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJoinAddresses(), getJoinAddressByPlayer());
    }

    @Override
    public String toString() {
        return "PlayerJoinAddresses{" +
                "joinAddresses=" + joinAddresses +
                ", joinAddressByPlayer=" + joinAddressByPlayer +
                '}';
    }
}
