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

import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class BukkitPlayerData implements PlatformPlayerData {

    private final Player player;
    private final String joinAddress;

    public BukkitPlayerData(Player player, String joinAddress) {
        this.player = player;
        this.joinAddress = joinAddress;
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
    public Optional<String> getJoinAddress() {
        return Optional.ofNullable(joinAddress);
    }

    @Override
    public Optional<String> getCurrentWorld() {
        return Optional.of(player.getWorld().getName());
    }

    @Override
    public Optional<String> getCurrentGameMode() {
        return Optional.ofNullable(player.getGameMode()).map(Enum::name);
    }

    @Override
    public Optional<Long> getRegisterDate() {
        long firstPlayed = player.getFirstPlayed();
        return firstPlayed > 0 ? Optional.of(firstPlayed) : Optional.empty();
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return Optional.ofNullable(player.getAddress()).map(InetSocketAddress::getAddress);
    }
}
