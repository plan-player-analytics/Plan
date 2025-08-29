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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.gathering.domain.PluginMetadata;
import com.djrapitops.plan.gathering.mixin.TickTimesMixin;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Singleton
public class SpongeSensor implements ServerSensor<ServerWorld> {

    private final Game game;

    @Inject
    public SpongeSensor(Game game) {
        this.game = game;
    }

    @Override
    public boolean supportsDirectTPS() {
        return true;
    }

    @Override
    public int getOnlinePlayerCount() {
        return game.server().onlinePlayers().size();
    }

    @Override
    public double getTPS() {
        return game.server().ticksPerSecond();
    }

    @Override
    public Iterable<ServerWorld> getWorlds() {
        return game.server().worldManager().worlds();
    }

    @Override
    public int getChunkCount(ServerWorld world) {
        return -1;
    }

    @SuppressWarnings("unused") // This remains here so that it is not enabled, it causes lag.
    private int getLaggyChunkCount(ServerWorld world) {
        Iterator<WorldChunk> chunks = world.loadedChunks().iterator();
        int count = 0;
        while (chunks.hasNext()) {
            chunks.next();
            count++;
        }
        return count;
    }

    @Override
    public int getEntityCount(ServerWorld world) {
        return world.entities().size();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return game.server().onlinePlayers().stream().map(ServerPlayer::name).collect(Collectors.toList());
    }

    @Override
    public List<PluginMetadata> getInstalledPlugins() {
        return game.pluginManager().plugins().stream()
                .map(PluginContainer::metadata)
                .map(metadata -> new PluginMetadata(
                        metadata.name().orElse(metadata.id()),
                        metadata.version().toString()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean supportsBans() {
        return true;
    }

    @Override
    public boolean isBanned(UUID playerUUID) {
        BanService banService = game.server().serviceProvider().banService();
        return game.server().gameProfileManager().cache().findById(playerUUID)
                .map(banService::find)
                .flatMap(CompletableFuture::join)
                .isPresent();
    }

    @Override
    public Optional<long[]> getMspt() {
        return Optional.ofNullable(((TickTimesMixin) game.server()).getTickTimesNanos());
    }
}
