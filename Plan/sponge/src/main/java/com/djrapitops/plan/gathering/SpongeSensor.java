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

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.server.ServerWorld;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;
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
}
