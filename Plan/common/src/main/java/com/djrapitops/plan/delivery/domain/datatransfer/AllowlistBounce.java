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

import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an event where player bounced off the whitelist.
 *
 * @author AuroraLS3
 */
public class AllowlistBounce {

    private final UUID playerUUID;
    @Untrusted
    private final String playerName;
    private final int count;
    private final long lastTime;

    public AllowlistBounce(UUID playerUUID, String playerName, int count, long lastTime) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.count = count;
        this.lastTime = lastTime;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getCount() {
        return count;
    }

    public long getLastTime() {
        return lastTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllowlistBounce bounce = (AllowlistBounce) o;
        return getCount() == bounce.getCount() && getLastTime() == bounce.getLastTime() && Objects.equals(getPlayerUUID(), bounce.getPlayerUUID()) && Objects.equals(getPlayerName(), bounce.getPlayerName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerUUID(), getPlayerName(), getCount(), getLastTime());
    }

    @Override
    public String toString() {
        return "AllowlistBounce{" +
                "playerUUID=" + playerUUID +
                ", playerName='" + playerName + '\'' +
                ", count=" + count +
                ", lastTime=" + lastTime +
                '}';
    }
}
