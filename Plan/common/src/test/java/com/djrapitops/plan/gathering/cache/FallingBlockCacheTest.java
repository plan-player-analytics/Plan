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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FallingBlockCacheTest {
    private final UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    private final String world = TestConstants.WORLD_ONE_NAME;

    private FallingBlockCache underTest;

    @BeforeEach
    void setUp() {
        underTest = new FallingBlockCache();
    }

    @Test
    void valueIsNotCached() {
        Optional<FallingBlockMetadata> result = underTest.getLandedBlock(world, 0, 64, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void placedBlockIsTrackedThroughFallingAndLanding() {
        UUID entityUUID = UUID.randomUUID();
        underTest.recordTrigger(world, 0, 70, 0, playerUUID);
        underTest.trackFallingBlock(entityUUID, world, 0, 70, 0, "SAND");
        underTest.markBlockLanded(entityUUID, world, 0, 64, 0);

        Optional<FallingBlockMetadata> result = underTest.getLandedBlock(world, 0, 64, 0);
        assertTrue(result.isPresent());
        assertEquals(playerUUID, result.get().playerUUID());
        assertEquals("SAND", result.get().blockName());
    }

    @Test
    void brokenSupportTriggersGravityBlockAbove() {
        UUID entityUUID = UUID.randomUUID();
        underTest.recordTrigger(world, 0, 69, 0, playerUUID);
        underTest.trackFallingBlock(entityUUID, world, 0, 70, 0, "GRAVEL");
        underTest.markBlockLanded(entityUUID, world, 0, 64, 0);

        Optional<FallingBlockMetadata> result = underTest.getLandedBlock(world, 0, 64, 0);
        assertTrue(result.isPresent());
        assertEquals(playerUUID, result.get().playerUUID());
        assertEquals("GRAVEL", result.get().blockName());
    }

    @Test
    void stackedGravityBlocksPropagateTriggerUpward() {
        UUID bottomEntity = UUID.randomUUID();
        UUID topEntity = UUID.randomUUID();

        // Support broken at y=69, gravity blocks stacked at y=70 and y=71
        underTest.recordTrigger(world, 0, 69, 0, playerUUID);
        underTest.trackFallingBlock(bottomEntity, world, 0, 70, 0, "SAND");
        underTest.trackFallingBlock(topEntity, world, 0, 71, 0, "RED_SAND");

        underTest.markBlockLanded(bottomEntity, world, 0, 64, 0);
        underTest.markBlockLanded(topEntity, world, 0, 65, 0);

        Optional<FallingBlockMetadata> bottomResult = underTest.getLandedBlock(world, 0, 64, 0);
        Optional<FallingBlockMetadata> topResult = underTest.getLandedBlock(world, 0, 65, 0);

        assertTrue(bottomResult.isPresent());
        assertEquals(playerUUID, bottomResult.get().playerUUID());
        assertEquals("SAND", bottomResult.get().blockName());

        assertTrue(topResult.isPresent());
        assertEquals(playerUUID, topResult.get().playerUUID());
        assertEquals("RED_SAND", topResult.get().blockName());
    }

    @Test
    void untriggeredFallingBlockIsNotTracked() {
        UUID entityUUID = UUID.randomUUID();
        underTest.trackFallingBlock(entityUUID, world, 0, 70, 0, "SAND");
        underTest.markBlockLanded(entityUUID, world, 0, 64, 0);

        Optional<FallingBlockMetadata> result = underTest.getLandedBlock(world, 0, 64, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void landedBlockIsNotFoundInDifferentWorld() {
        UUID entityUUID = UUID.randomUUID();
        underTest.recordTrigger(world, 0, 70, 0, playerUUID);
        underTest.trackFallingBlock(entityUUID, world, 0, 70, 0, "SAND");
        underTest.markBlockLanded(entityUUID, world, 0, 64, 0);

        Optional<FallingBlockMetadata> result = underTest.getLandedBlock("Other World", 0, 64, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void landedBlockIsRemovedWhenInvalidated() {
        UUID entityUUID = UUID.randomUUID();
        underTest.recordTrigger(world, 0, 70, 0, playerUUID);
        underTest.trackFallingBlock(entityUUID, world, 0, 70, 0, "ORANGE_CONCRETE_POWDER");
        underTest.markBlockLanded(entityUUID, world, 0, 64, 0);

        underTest.invalidateLandedBlock(world, 0, 64, 0);

        Optional<FallingBlockMetadata> result = underTest.getLandedBlock(world, 0, 64, 0);
        assertTrue(result.isEmpty());
    }

    @Test
    void fallingBlockIsConsumedOnLanding() {
        UUID entityUUID = UUID.randomUUID();
        underTest.recordTrigger(world, 0, 70, 0, playerUUID);
        underTest.trackFallingBlock(entityUUID, world, 0, 70, 0, "SAND");
        underTest.markBlockLanded(entityUUID, world, 0, 64, 0);

        // Second landing call with the same entity UUID should be ignored
        underTest.markBlockLanded(entityUUID, world, 0, 60, 0);

        assertTrue(underTest.getLandedBlock(world, 0, 64, 0).isPresent());
        assertTrue(underTest.getLandedBlock(world, 0, 60, 0).isEmpty());
    }
}
