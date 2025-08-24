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

import com.velocitypowered.api.proxy.Player;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlayerData implements PlatformPlayerData {

    private final Player player;

    public VelocityPlayerData(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return Optional.of(player.getRemoteAddress().getAddress());
    }

    @Override
    public Optional<String> getJoinAddress() {
        return player.getVirtualHost()
                .map(InetSocketAddress::getHostString)
                .map(this::removeExtra);
    }

    private String removeExtra(String address) {
        if (address.contains("\u0000")) {
            return address.substring(0, address.indexOf('\u0000'));
        }
        return address;
    }
}
