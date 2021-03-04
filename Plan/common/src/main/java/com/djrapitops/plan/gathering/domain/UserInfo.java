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
 * Represents user information stored in plan_user_info.
 * <p>
 * Unlike {@link BaseUser} one instance is stored per server for a single player.
 * Proxy servers are an exception, and UserInfo is not stored for them.
 *
 * @author Rsl1122
 */
public class UserInfo {

    private final UUID playerUUID;
    private final UUID serverUUID;
    private final long registered;
    private final boolean banned;
    private final boolean opped;
    private final String hostname;

    public UserInfo(UUID playerUUID, UUID serverUUID, long registered, boolean opped, String hostname, boolean banned) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.registered = registered;
        this.opped = opped;
        this.banned = banned;
        this.hostname = hostname;
    }

    public UUID getPlayerUuid() {
        return playerUUID;
    }

    public UUID getServerUUID() {
        return serverUUID;
    }

    public String getHostname() {
        return hostname;
    }

    public long getRegistered() {
        return registered;
    }

    public boolean isBanned() {
        return banned;
    }

    public boolean isOperator() {
        return opped;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;
        UserInfo userInfo = (UserInfo) o;
        return registered == userInfo.registered &&
                banned == userInfo.banned &&
                opped == userInfo.opped &&
                playerUUID.equals(userInfo.playerUUID) &&
                serverUUID.equals(userInfo.serverUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID, serverUUID, registered, banned, hostname, opped);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "playerUUID=" + playerUUID +
                ", serverUUID=" + serverUUID +
                ", registered=" + registered +
                ", banned=" + banned +
                ", opped=" + opped +
                ", hostname=" + hostname +
                '}';
    }
}