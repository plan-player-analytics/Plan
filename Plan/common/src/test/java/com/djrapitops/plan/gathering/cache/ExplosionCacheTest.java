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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExplosionCacheTest {

    private final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    private final String world = TestConstants.WORLD_ONE_NAME;

    private ExplosionCache underTest;

    @BeforeEach
    void setUp() {
        underTest = new ExplosionCache();
    }

    @Test
    void valueIsNotCached() {
        Optional<ExplosionMetadata> result = underTest.getRecentExplosion(world, 0.0, 64.0, 0.0, 1000L);
        assertTrue(result.isEmpty());
    }

    @Test
    void valueIsCached() {
        long now = 1000L;
        underTest.store(new ExplosionMetadata("RESPAWN_ANCHOR", playerUUID, world, 0, 64, 0, now));

        Optional<ExplosionMetadata> result = underTest.getRecentExplosion(world, 5.0, 64.0, 5.0, now + 20L);
        assertTrue(result.isPresent());
        assertEquals(playerUUID, result.get().playerUUID());
        assertEquals("RESPAWN_ANCHOR", result.get().explosionBlockName());
    }

    @Test
    void explosionIsNotFoundWhenTooFar() {
        long now = 1000L;
        underTest.store(new ExplosionMetadata("RESPAWN_ANCHOR", playerUUID, world, 0, 64, 0, now));

        // 13 blocks away on X axis (limit is 12 blocks / 144 distance squared)
        Optional<ExplosionMetadata> result = underTest.getRecentExplosion(world, 13.0, 64.0, 0.0, now);
        assertTrue(result.isEmpty());
    }

    @Test
    void explosionIsNotFoundWhenExpired() {
        long now = 1000L;
        underTest.store(new ExplosionMetadata("RESPAWN_ANCHOR", playerUUID, world, 0, 64, 0, now));

        // 51ms later (limit is 50ms / 1 server tick)
        Optional<ExplosionMetadata> result = underTest.getRecentExplosion(world, 1.0, 64.0, 1.0, now + 51L);
        assertTrue(result.isEmpty());
    }

    @Test
    void explosionIsNotFoundInDifferentWorld() {
        long now = 1000L;
        underTest.store(new ExplosionMetadata("RESPAWN_ANCHOR", playerUUID, world, 0, 64, 0, now));

        Optional<ExplosionMetadata> result = underTest.getRecentExplosion("Other World", 0.0, 64.0, 0.0, now);
        assertTrue(result.isEmpty());
    }
}