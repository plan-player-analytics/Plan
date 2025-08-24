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

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.ban.BanService;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class SpongePlayerData implements PlatformPlayerData {

    private final ServerPlayer player;

    public SpongePlayerData(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.uniqueId();
    }

    @Override
    public String getName() {
        return player.name();
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.of(LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName().get()));
    }

    @Override
    public Optional<Boolean> isBanned() {
        BanService banService = Sponge.server().serviceProvider().banService();
        boolean banned = banService.find(player.profile()).join().isPresent();
        return Optional.of(banned);
    }

    @Override
    public Optional<String> getJoinAddress() {
        String address = player.connection().virtualHost().getHostString();
        if (address.contains("\u0000")) {
            address = address.substring(0, address.indexOf('\u0000'));
        }
        return Optional.of(address);
    }

    @Override
    public Optional<String> getCurrentWorld() {
        return Optional.ofNullable(Sponge.game().server().worldManager().worldDirectory(player.world().key()))
                .map(path -> path.getFileName().toString());
    }

    @Override
    public Optional<String> getCurrentGameMode() {
        GameMode gameMode = player.gameMode().get();
        String gm = gameMode.key(RegistryTypes.GAME_MODE).value().toUpperCase();
        return Optional.of(gm);
    }

    @Override
    public Optional<Long> getRegisterDate() {
        return player.firstJoined().map(Value::get).map(Instant::toEpochMilli);
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return Optional.of(player.connection().address().getAddress());
    }
}
