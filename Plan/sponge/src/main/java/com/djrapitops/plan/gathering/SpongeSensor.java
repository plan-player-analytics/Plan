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

import com.djrapitops.plan.PlanSponge;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;

@Singleton
public class SpongeSensor implements ServerSensor<World> {

    private final PlanSponge plugin;

    @Inject
    public SpongeSensor(PlanSponge plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supportsDirectTPS() {
        return true;
    }

    @Override
    public int getOnlinePlayerCount() {
        return plugin.getGame().getServer().getOnlinePlayers().size();
    }

    @Override
    public double getTPS() {
        return plugin.getGame().getServer().getTicksPerSecond();
    }

    @Override
    public Iterable<World> getWorlds() {
        return plugin.getGame().getServer().getWorlds();
    }

    @Override
    public int getChunkCount(World world) {
        return -1;
    }

    private int getLaggyChunkCount(World world) {
        Iterator<Chunk> chunks = world.getLoadedChunks().iterator();
        int count = 0;
        while (chunks.hasNext()) {
            chunks.next();
            count++;
        }
        return count;
    }

    @Override
    public int getEntityCount(World world) {
        return world.getEntities().size();
    }
}