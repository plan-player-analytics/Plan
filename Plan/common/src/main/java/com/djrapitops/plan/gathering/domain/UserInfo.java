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

import com.djrapitops.plan.identification.ServerUUID;

import java.util.UUID;

/**
 * Represents user information stored in plan_user_info.
 * <p>
 * Unlike {@link BaseUser} one instance is stored per server for a single player.
 * Proxy servers are an exception, and UserInfo is not stored for them.
 *
 * @author AuroraLS3
 */
public record UserInfo(UUID playerUUID,
                       ServerUUID serverUUID,
                       long registered,
                       boolean banned,
                       boolean isOperator,
                       @Deprecated(since = "5.4 build 1722") String joinAddress) {

    public UUID getPlayerUuid() {
        return playerUUID;
    }

    /**
     * @deprecated Join address is now stored in {@link FinishedSession#extraData()}, this method may become unreliable in the future.
     */
    @Override
    @Deprecated(since = "5.4 build 1722")
    public String joinAddress() {
        return joinAddress;
    }
}