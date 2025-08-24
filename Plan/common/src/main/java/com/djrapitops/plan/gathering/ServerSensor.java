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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Allows sensing values from different server platforms.
 *
 * @param <W> Type of the class representing a minecraft world.
 * @author AuroraLS3
 */
public interface ServerSensor<W> {

    /**
     * Check if server platform provides TPS calculation.
     *
     * @return false if the server doesn't count TPS.
     */
    boolean supportsDirectTPS();

    int getOnlinePlayerCount();

    default double getTPS() {
        return -1;
    }

    /**
     * Get the worlds running on the server platform.
     *
     * @return Empty collection if the platform doesn't support worlds.
     */
    default Iterable<W> getWorlds() {
        return Collections.emptyList();
    }

    default int getChunkCount(W world) {
        return -1;
    }

    default int getEntityCount(W world) {
        return -1;
    }

    default List<String> getOnlinePlayerNames() {
        return Collections.emptyList();
    }

    default boolean usingRedisBungee() {
        return false;
    }

    default List<PluginMetadata> getInstalledPlugins() {
        return List.of();
    }

    default boolean supportsBans() {return false;}

    default boolean isBanned(UUID playerUUID) {return false;}

    default Optional<Double> getMsptAverage() {
        return Optional.empty();
    }
}
