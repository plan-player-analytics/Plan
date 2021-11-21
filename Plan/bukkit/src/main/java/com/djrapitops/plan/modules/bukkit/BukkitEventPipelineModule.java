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
package com.djrapitops.plan.modules.bukkit;

import com.djrapitops.plan.DataService;
import com.djrapitops.plan.exceptions.MissingPipelineException;
import com.djrapitops.plan.gathering.cache.JoinAddressCache;
import com.djrapitops.plan.gathering.domain.PlayerMetadata;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.identification.ServerUUID;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

@Module
public class BukkitEventPipelineModule {

    @Provides
    @Singleton
    @IntoSet
    DataService.Pipeline metadata(JoinAddressCache joinAddressCache) {
        return service -> service
                .registerMapper(UUID.class, Player.class, PlayerMetadata.class,
                        player -> getPlayerMetadata(player, service, joinAddressCache));
    }

    private PlayerMetadata getPlayerMetadata(Player player, DataService service, JoinAddressCache joinAddressCache) {
        return PlayerMetadata.builder()
                .playerName(player.getName())
                .displayName(player.getDisplayName())
                .world(player.getWorld().getName())
                .gameMode(Optional.ofNullable(player.getGameMode()).map(GameMode::name).orElse(null))
                .ipAddress(Optional.ofNullable(player.getAddress()).map(InetSocketAddress::getAddress).orElse(null))
                .joinAddress(service.pullWithoutId(JoinAddress.class).map(JoinAddress::getAddress).orElse(null))
                .build();
    }

    @Provides
    @Singleton
    @IntoSet
    DataService.Pipeline events() {
        return service -> service
                .registerDataServiceMapper(UUID.class, PlayerJoinEvent.class, PlayerJoin.class, this::mapToPlayerJoin)
                .registerDataServiceMapper(UUID.class, PlayerQuitEvent.class, PlayerLeave.class, this::mapToPlayerLeave);
    }

    private PlayerJoin mapToPlayerJoin(DataService service, PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Optional<PlayerMetadata> metadata = service.map(playerUUID, event.getPlayer(), PlayerMetadata.class);
        return PlayerJoin.builder()
                .playerUUID(playerUUID)
                .serverUUID(service.pullWithoutId(ServerUUID.class).orElseThrow(MissingPipelineException::new))
                .playerMetadata(metadata.orElseThrow(MissingPipelineException::new))
                .time(System.currentTimeMillis())
                .build();
    }

    private PlayerLeave mapToPlayerLeave(DataService service, PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Optional<PlayerMetadata> metadata = service.map(playerUUID, event.getPlayer(), PlayerMetadata.class);
        return PlayerLeave.builder()
                .playerUUID(playerUUID)
                .serverUUID(service.pullWithoutId(ServerUUID.class).orElseThrow(MissingPipelineException::new))
                .playerMetadata(metadata.orElseThrow(MissingPipelineException::new))
                .time(System.currentTimeMillis())
                .build();
    }
}
