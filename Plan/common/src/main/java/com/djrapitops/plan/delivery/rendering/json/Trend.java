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
package com.djrapitops.plan.delivery.rendering.json;

/**
 * Represents a trend in the data - used for JSON format.
 *
 * @author AuroraLS3
 */
public record Trend(Number text, String direction, boolean reversed) {

    /**
     * When a trend is reversed increase is "bad" (red) and decrease is "good" (green)
     */
    @SuppressWarnings("java:S1845")
    public static final boolean REVERSED = true;

    public Trend(long before, long after, boolean reversed) {
        this(Math.abs(before - after), getDirection(before, after), reversed);
    }

    public Trend(double before, double after, boolean reversed) {
        this(Math.abs(before - after), getDirection(before, after), reversed);
    }

    private static String getDirection(double before, double after) {
        if (before < after) {
            return "+";
        } else if (before > after) {
            return "-";
        }
        return null;
    }

    private static String getDirection(long before, long after) {
        if (before < after) {
            return "+";
        } else if (before > after) {
            return "-";
        }
        return null;
    }
}