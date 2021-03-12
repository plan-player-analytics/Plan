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

import com.djrapitops.plan.identification.ServerUUID;

import java.net.InetAddress;
import java.util.UUID;

public class PlayerJoin {

    private final UUID playerUUID;
    private final String playerName;
    private final String displayName;
    private final InetAddress ipAddress;

    private final ServerUUID serverUUID;
    private final String world;
    private final String gameMode;

    private final long time;

    public PlayerJoin(
            UUID playerUUID, String playerName, String displayName, InetAddress ipAddress,
            ServerUUID serverUUID, String world, String gameMode,
            long time
    ) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.displayName = displayName;
        this.ipAddress = ipAddress;
        this.serverUUID = serverUUID;
        this.world = world;
        this.gameMode = gameMode;
        this.time = time;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public String getWorld() {
        return world;
    }

    public String getGameMode() {
        return gameMode;
    }

    public long getTime() {
        return time;
    }
}
