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

import java.net.InetAddress;
import java.util.Optional;

public class PlayerMetadata {

    private final String playerName;
    private final String displayName;

    private final String joinAddress;
    private final InetAddress ipAddress;

    private final String world;
    private final String gameMode;

    public PlayerMetadata(String playerName, String displayName, String joinAddress, InetAddress ipAddress, String world, String gameMode) {
        this.playerName = playerName;
        this.displayName = displayName;
        this.joinAddress = joinAddress;
        this.ipAddress = ipAddress;
        this.world = world;
        this.gameMode = gameMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Optional<String> getJoinAddress() {
        return Optional.ofNullable(joinAddress);
    }

    public Optional<InetAddress> getIpAddress() {
        return Optional.ofNullable(ipAddress);
    }

    public Optional<String> getWorld() {
        return Optional.ofNullable(world);
    }

    public Optional<String> getGameMode() {
        return Optional.ofNullable(gameMode);
    }

    public static final class Builder {
        private String playerName;
        private String displayName;
        private String joinAddress;
        private InetAddress ipAddress;
        private String world;
        private String gameMode;

        private Builder() {}

        public static Builder aPlayerMetadata() {return new Builder();}

        public Builder playerName(String playerName) {
            this.playerName = playerName;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder joinAddress(String joinAddress) {
            this.joinAddress = joinAddress;
            return this;
        }

        public Builder ipAddress(InetAddress ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder gameMode(String gameMode) {
            this.gameMode = gameMode;
            return this;
        }

        public PlayerMetadata build() {return new PlayerMetadata(playerName, displayName, joinAddress, ipAddress, world, gameMode);}
    }
}
