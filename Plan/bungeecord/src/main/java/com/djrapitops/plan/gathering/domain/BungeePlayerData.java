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

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.UUID;

public class BungeePlayerData implements PlatformPlayerData {

    private final ProxiedPlayer player;

    public BungeePlayerData(ProxiedPlayer player) {this.player = player;}

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        Optional<InetAddress> ip = getIPFromSocketAddress();
        if (ip.isPresent()) return ip;
        return getIpFromOldMethod();
    }

    @SuppressWarnings("deprecation") // ProxiedPlayer#getAddress is deprecated
    private Optional<InetAddress> getIpFromOldMethod() {
        try {
            return Optional.ofNullable(player.getAddress()).map(InetSocketAddress::getAddress);
        } catch (NoSuchMethodError e) {
            return Optional.empty();
        }
    }

    private Optional<InetAddress> getIPFromSocketAddress() {
        try {
            SocketAddress socketAddress = player.getSocketAddress();
            if (socketAddress instanceof InetSocketAddress) {
                return Optional.of(((InetSocketAddress) socketAddress).getAddress());
            }

            // Unix domain socket address requires Java 16 compatibility.
            // These connections come from the same physical machine
            Class<?> jdk16SocketAddressType = Class.forName("java.net.UnixDomainSocketAddress");
            if (jdk16SocketAddressType.isAssignableFrom(socketAddress.getClass())) {
                return Optional.of(InetAddress.getLocalHost());
            }
        } catch (NoSuchMethodError | ClassNotFoundException | UnknownHostException e) {
            // Ignored
        }
        return Optional.empty();
    }
}
