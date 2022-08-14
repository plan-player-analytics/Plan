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

import cn.nukkit.Player;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class NukkitPlayerData implements PlatformPlayerData {

    private final Player player;

    public NukkitPlayerData(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.of(player.getDisplayName());
    }

    @Override
    public Optional<Boolean> isBanned() {
        return Optional.of(player.isBanned());
    }

    @Override
    public Optional<Boolean> isOperator() {
        return Optional.of(player.isOp());
    }

    @Override
    public Optional<String> getCurrentWorld() {
        return Optional.of(player.getLevel().getName());
    }

    @Override
    public Optional<String> getCurrentGameMode() {
        return Optional.of(player.getGamemode()).map(GMTimes::magicNumberToGMName);
    }

    @Override
    public Optional<Long> getRegisterDate() {
        return Optional.of(TimeUnit.SECONDS.toMillis(player.getFirstPlayed()));
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return Optional.of(player.getSocketAddress()).map(InetSocketAddress::getAddress);
    }
}
