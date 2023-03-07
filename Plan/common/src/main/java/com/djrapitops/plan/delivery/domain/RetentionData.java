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

import java.util.Objects;
import java.util.UUID;

/**
 * Represents data that can be used to calculate player retention for a specific player.
 *
 * @author AuroraLS3
 */
public class RetentionData {

    private final UUID playerUUID;
    private final long registerDate;
    private final long lastSeenDate;
    private final long playtime;
    private final long timeDifference;

    public RetentionData(UUID playerUUID, long registerDate, long lastSeenDate, long playtime) {
        this.playerUUID = playerUUID;
        this.registerDate = registerDate;
        this.lastSeenDate = lastSeenDate;
        this.playtime = playtime;
        timeDifference = lastSeenDate - registerDate;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public long getRegisterDate() {
        return registerDate;
    }

    public long getLastSeenDate() {
        return lastSeenDate;
    }

    public long getTimeDifference() {
        return timeDifference;
    }

    public long getPlaytime() {
        return playtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetentionData that = (RetentionData) o;
        return getRegisterDate() == that.getRegisterDate() && getLastSeenDate() == that.getLastSeenDate() && getPlaytime() == that.getPlaytime() && getTimeDifference() == that.getTimeDifference() && Objects.equals(getPlayerUUID(), that.getPlayerUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerUUID(), getRegisterDate(), getLastSeenDate(), getPlaytime(), getTimeDifference());
    }

    @Override
    public String toString() {
        return "RetentionData{" +
                "playerUUID=" + playerUUID +
                ", registerDate=" + registerDate +
                ", lastSeenDate=" + lastSeenDate +
                ", playtime=" + playtime +
                ", timeDifference=" + timeDifference +
                '}';
    }
}
