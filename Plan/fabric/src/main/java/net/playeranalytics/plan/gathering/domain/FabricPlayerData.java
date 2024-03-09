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
package net.playeranalytics.plan.gathering.domain;

import com.djrapitops.plan.gathering.domain.PlatformPlayerData;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.unix.DomainSocketAddress;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.net.*;
import java.util.Optional;
import java.util.UUID;

public class FabricPlayerData implements PlatformPlayerData {

    private final ServerPlayerEntity player;
    private final MinecraftDedicatedServer server;
    private final String joinAddress;

    public FabricPlayerData(ServerPlayerEntity player, MinecraftDedicatedServer server, String joinAddress) {
        this.player = player;
        this.server = server;
        this.joinAddress = joinAddress;
    }

    @Override
    public UUID getUUID() {
        return player.getUuid();
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(player.getDisplayName()).map(Text::getString);
    }

    @Override
    public Optional<Boolean> isOperator() {
        return Optional.of(server.getPlayerManager().getOpList().get(player.getGameProfile()) != null);
    }

    @Override
    public Optional<String> getCurrentWorld() {
        return Optional.of(player.getWorld().getRegistryKey().getValue().toString());
    }

    @Override
    public Optional<String> getCurrentGameMode() {
        return Optional.of(player.interactionManager.getGameMode().name());
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return getIPFromSocketAddress();
    }

    private Optional<InetAddress> getIPFromSocketAddress() {
        try {
            SocketAddress socketAddress = player.networkHandler.getConnectionAddress();
            if (socketAddress instanceof InetSocketAddress inetSocketAddress) {
                return Optional.of(inetSocketAddress.getAddress());
            } else if (socketAddress instanceof UnixDomainSocketAddress || socketAddress instanceof LocalAddress) {
                // These connections come from the same physical machine
                return Optional.of(InetAddress.getLocalHost());
            } else if (socketAddress instanceof DomainSocketAddress domainSocketAddress) {
                return Optional.of(InetAddress.getByName(domainSocketAddress.path()));
            }
        } catch (NoSuchMethodError | UnknownHostException e) {
            // Ignored
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getJoinAddress() {
        return Optional.ofNullable(joinAddress);
    }
}
