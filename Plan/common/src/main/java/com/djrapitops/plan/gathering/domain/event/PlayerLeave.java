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
import com.djrapitops.plan.identification.ServerUUID;

import java.util.UUID;

public class PlayerLeave {

    private final UUID playerUUID;
    private final ServerUUID serverUUID;
    private final PlayerMetadata playerMetadata;

    private final long time;

    public PlayerLeave(UUID playerUUID, ServerUUID serverUUID, PlayerMetadata playerMetadata, long time) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.playerMetadata = playerMetadata;
        this.time = time;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public PlayerMetadata getPlayerMetadata() {
        return playerMetadata;
    }

    public long getTime() {
        return time;
    }

    public static final class Builder {
        private UUID playerUUID;
        private ServerUUID serverUUID;
        private PlayerMetadata playerMetadata;
        private long time;

        private Builder() {}

        public static Builder aPlayerLeave() {return new Builder();}

        public Builder playerUUID(UUID playerUUID) {
            this.playerUUID = playerUUID;
            return this;
        }

        public Builder serverUUID(ServerUUID serverUUID) {
            this.serverUUID = serverUUID;
            return this;
        }

        public Builder playerMetadata(PlayerMetadata playerMetadata) {
            this.playerMetadata = playerMetadata;
            return this;
        }

        public Builder time(long time) {
            this.time = time;
            return this;
        }

        public PlayerLeave build() {return new PlayerLeave(playerUUID, serverUUID, playerMetadata, time);}
    }
}
