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
package com.djrapitops.plan.gathering.cache;

import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class JoinAddressCache {

    private final Map<UUID, String> joinAddresses;

    @Inject
    public JoinAddressCache() {
        joinAddresses = new HashMap<>();
    }

    public void put(UUID playerUUID, JoinAddress joinAddress) {
        put(playerUUID, joinAddress.getAddress());
    }

    public void put(UUID playerUUID, String joinAddress) {
        joinAddresses.put(playerUUID, joinAddress);
    }

    public Optional<JoinAddress> get(UUID playerUUID) {
        return Optional.ofNullable(joinAddresses.get(playerUUID))
                .map(JoinAddress::new);
    }

    public void remove(UUID playerUUID, PlayerLeave leave) {
        remove(playerUUID);
    }

    public void remove(UUID playerUUID) {
        joinAddresses.remove(playerUUID);
    }
}
