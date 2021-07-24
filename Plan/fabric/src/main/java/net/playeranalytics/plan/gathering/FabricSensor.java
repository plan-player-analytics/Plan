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
package net.playeranalytics.plan.gathering;

import com.djrapitops.plan.gathering.ServerSensor;
import net.minecraft.entity.Entity;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.world.ServerWorld;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class FabricSensor implements ServerSensor<ServerWorld> {

    private final MinecraftDedicatedServer server;

    @Inject
    public FabricSensor(
            MinecraftDedicatedServer server
    ) {
        this.server = server;
    }

    @Override
    public double getTPS() {
        //Returns the ticks per second of the last 100 ticks
        int length = server.lastTickLengths.length;
        double totalTickLength = 0;
        int count = 0;
        for (long tickLength : server.lastTickLengths) {
            if (tickLength == 0) continue; // Ignore uninitialized values in array
            totalTickLength += Math.max(tickLength, TimeUnit.MILLISECONDS.toNanos(50));
            count++;
        }
        double averageTickLength = totalTickLength / count;
        return count != 0 ? TimeUnit.SECONDS.toNanos(1) / averageTickLength : -1;
    }

    @Override
    public Iterable<ServerWorld> getWorlds() {
        return server.getWorlds();
    }

    @Override
    public int getEntityCount(ServerWorld world) {
        int entities = 0;
        for (Entity ignored : world.iterateEntities()) {
            entities++;
        }
        return entities;
    }

    @Override
    public int getChunkCount(ServerWorld world) {
        return world.getChunkManager().getLoadedChunkCount();
    }

    @Override
    public boolean supportsDirectTPS() {
        return true;
    }

    @Override
    public int getOnlinePlayerCount() {
        return server.getCurrentPlayerCount();
    }
}
