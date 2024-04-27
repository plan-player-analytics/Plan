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

import com.djrapitops.plan.utilities.dev.Untrusted;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

public interface PlatformPlayerData {

    UUID getUUID();

    @Untrusted
    String getName();

    @Untrusted
    default Optional<String> getDisplayName() {
        return Optional.empty();
    }

    default Optional<Boolean> isBanned() {
        return Optional.empty();
    }

    default Optional<Boolean> isOperator() {
        return Optional.empty();
    }

    @Untrusted
    default Optional<String> getJoinAddress() {
        return Optional.empty();
    }

    default Optional<String> getCurrentWorld() {
        return Optional.empty();
    }

    default Optional<String> getCurrentGameMode() {
        return Optional.empty();
    }

    default Optional<Long> getRegisterDate() {
        return Optional.empty();
    }

    default Optional<InetAddress> getIPAddress() {
        return Optional.empty();
    }

}
