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

import com.djrapitops.plan.gathering.domain.PlatformPlayerData;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.UUID;

public class PlayerLeave {

    private final Server server;
    private final PlatformPlayerData player;

    private final long time;

    private PlayerLeave(Server server, PlatformPlayerData player, long time) {
        this.server = server;
        this.player = player;
        this.time = time;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getPlayerUUID() {
        return player.getUUID();
    }

    public String getPlayerName() {
        return player.getName();
    }

    public ServerUUID getServerUUID() {
        return server.getUuid();
    }

    public PlatformPlayerData getPlayer() {
        return player;
    }

    public long getTime() {
        return time;
    }

    public static final class Builder {
        private Server server;
        private PlatformPlayerData player;
        private long time;

        private Builder() {}

        public static Builder aPlayerLeave() {return new Builder();}

        public Builder player(PlatformPlayerData player) {
            this.player = player;
            return this;
        }

        public Builder server(Server server) {
            this.server = server;
            return this;
        }

        public Builder time(long time) {
            this.time = time;
            return this;
        }

        public PlayerLeave build() {return new PlayerLeave(server, player, time);}
    }
}
