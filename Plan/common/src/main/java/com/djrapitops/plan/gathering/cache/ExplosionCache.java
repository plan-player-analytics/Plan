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
package com.djrapitops.plan.gathering.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Cache for tracking recent block explosions triggered by players.
 */
@Singleton
public class ExplosionCache {

    private final Cache<UUID, ExplosionMetadata> lastExplosions = Caffeine.newBuilder()
            .expireAfterWrite(100, TimeUnit.MILLISECONDS)
            .build();

    @Inject
    public ExplosionCache() {
        // Required for Dagger injection
    }

    public void store(ExplosionMetadata metadata) {
        lastExplosions.put(metadata.playerUUID(), metadata);
    }

    public Optional<ExplosionMetadata> getRecentExplosion(String worldIdentifier, double x, double y, double z, long time) {
        for (ExplosionMetadata metadata : lastExplosions.asMap().values()) {
            if (metadata.worldIdentifier().equals(worldIdentifier)
                    && metadata.isRecent(time)
                    && metadata.isWithinRadius(x, y, z)) {
                return Optional.of(metadata);
            }
        }
        return Optional.empty();
    }
}
