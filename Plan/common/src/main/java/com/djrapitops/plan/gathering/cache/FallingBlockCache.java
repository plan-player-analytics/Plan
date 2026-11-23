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
 * Tracks player-triggered gravity blocks (sand, gravel, concrete powder)
 * through their falling lifecycle to attribute suffocation kills.
 */
@Singleton
public class FallingBlockCache {
    // Bound cache size to prevent memory spikes during massive sand/gravel collapses
    private static final long MAX_CACHE_SIZE = 10_000L;

    private record BlockPosition(String worldIdentifier, int x, int y, int z) {
    }

    private final Cache<BlockPosition, UUID> recentTriggers = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    private final Cache<UUID, FallingBlockMetadata> fallingBlocks = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.SECONDS)
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    private final Cache<BlockPosition, FallingBlockMetadata> landedBlocks = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.SECONDS)
            .maximumSize(MAX_CACHE_SIZE)
            .build();

    @Inject
    public FallingBlockCache() {
        // Required for Dagger injection
    }

    public void recordTrigger(String worldIdentifier, int x, int y, int z, UUID playerUUID) {
        recentTriggers.put(new BlockPosition(worldIdentifier, x, y, z), playerUUID);
    }

    public void trackFallingBlock(UUID entityUUID, String worldIdentifier, int x, int y, int z, String blockName) {
        BlockPosition currentPos = new BlockPosition(worldIdentifier, x, y, z);
        UUID playerUUID = recentTriggers.getIfPresent(currentPos);
        if (playerUUID == null) {
            playerUUID = recentTriggers.getIfPresent(new BlockPosition(worldIdentifier, x, y - 1, z));
        }

        if (playerUUID != null) {
            fallingBlocks.put(entityUUID, new FallingBlockMetadata(blockName, playerUUID));
            // Propagate trigger upward so stacked gravity blocks inherit the same killer
            recentTriggers.put(currentPos, playerUUID);
        }
    }

    public void markBlockLanded(UUID entityUUID, String worldIdentifier, int x, int y, int z) {
        FallingBlockMetadata metadata = fallingBlocks.asMap().remove(entityUUID);
        if (metadata != null) {
            landedBlocks.put(new BlockPosition(worldIdentifier, x, y, z), metadata);
        }
    }

    public Optional<FallingBlockMetadata> getLandedBlock(String worldIdentifier, int x, int y, int z) {
        return Optional.ofNullable(landedBlocks.getIfPresent(new BlockPosition(worldIdentifier, x, y, z)));
    }

    public void invalidateLandedBlock(String worldIdentifier, int x, int y, int z) {
        landedBlocks.invalidate(new BlockPosition(worldIdentifier, x, y, z));
    }
}
