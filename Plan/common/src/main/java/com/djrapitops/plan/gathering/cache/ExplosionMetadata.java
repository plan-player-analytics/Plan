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

import java.util.UUID;

/**
 * Metadata of a player interaction with an exploding block.
 */
public record ExplosionMetadata(
        String explosionBlockName,
        UUID playerUUID,
        String worldIdentifier,
        int x,
        int y,
        int z,
        long explosionTime
) {

    private static final double MAX_EXPLOSION_RADIUS_SQUARED = 144.0; // 12 blocks radius (12 * 12)
    private static final long MAX_EXPLOSION_DELAY_MS = 50L; // 1 server tick

    public boolean isWithinRadius(double targetX, double targetY, double targetZ) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        return (dx * dx + dy * dy + dz * dz) <= MAX_EXPLOSION_RADIUS_SQUARED;
    }

    public boolean isRecent(long time) {
        return (time - explosionTime) <= MAX_EXPLOSION_DELAY_MS;
    }
}
