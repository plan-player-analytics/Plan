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
package com.djrapitops.plan.extension.graph;

import java.util.Optional;

/**
 * Format type for graph series.
 *
 * @author AuroraLS3
 */
public enum GraphFormatType {
    /**
     * Default - no formatting, just a double/int.
     */
    NONE,
    /**
     * Milliseconds of time, formatted same as Playtime.
     */
    TIME_AMOUNT,
    /**
     * Milliseconds of time, formatted same as Ping.
     */
    MILLISECONDS,
    /**
     * Percentage - values between 0.0 and 1.0.
     */
    PERCENTAGE,
    /**
     * Bytes - formatted same as RAM/Disk space: 1000 = 1 kB, 1000000 = 1 MB, etc.
     */
    BYTES,
    /**
     * Integer - any decimals are cut off.
     */
    INTEGER;

    /**
     * Get a format type by the enum name without exception.
     *
     * @param name GraphFormatType#name()
     * @return Optional if the format type is found by that name, empty if not found.
     */
    public static Optional<GraphFormatType> getByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
