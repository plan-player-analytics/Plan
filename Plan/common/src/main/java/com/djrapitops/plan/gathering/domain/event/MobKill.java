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
package com.djrapitops.plan.gathering.domain.event;

import com.djrapitops.plan.gathering.domain.PlayerMetadata;

import java.util.UUID;

public class MobKill {
    private final UUID playerUUID;
    private final PlayerMetadata playerMetadata;
    private final String mobType;
    private final long time;

    public MobKill(UUID playerUUID, PlayerMetadata playerMetadata, String mobType, long time) {
        this.playerUUID = playerUUID;
        this.playerMetadata = playerMetadata;
        this.mobType = mobType;
        this.time = time;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public PlayerMetadata getPlayerMetadata() {
        return playerMetadata;
    }

    public String getMobType() {
        return mobType;
    }

    public long getTime() {
        return time;
    }


    public static final class Builder {
        private UUID playerUUID;
        private PlayerMetadata playerMetadata;
        private String mobType;
        private long time;

        private Builder() {}

        public static Builder aMobKill() {return new Builder();}

        public Builder playerUUID(UUID playerUUID) {
            this.playerUUID = playerUUID;
            return this;
        }

        public Builder playerMetadata(PlayerMetadata playerMetadata) {
            this.playerMetadata = playerMetadata;
            return this;
        }

        public Builder mobType(String mobType) {
            this.mobType = mobType;
            return this;
        }

        public Builder time(long time) {
            this.time = time;
            return this;
        }

        public MobKill build() {return new MobKill(playerUUID, playerMetadata, mobType, time);}
    }
}
