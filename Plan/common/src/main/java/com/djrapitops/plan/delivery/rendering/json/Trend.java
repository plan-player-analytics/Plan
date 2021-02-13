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

import com.djrapitops.plan.delivery.formatting.Formatter;

/**
 * Represents a trend in the data - used for JSON format.
 *
 * @author AuroraLS3
 */
public class Trend {

    /**
     * When a trend is reversed increase is "bad" (red) and decrease is "good" (green)
     */
    public static final boolean REVERSED = true;

    private final String text;
    private final String direction;
    private final boolean reversed;

    public Trend(long before, long after, boolean reversed) {
        long difference = Math.abs(before - after);
        this.text = Long.toString(difference);
        this.direction = getDirection(before, after);
        this.reversed = reversed;
    }

    public Trend(long before, long after, boolean reversed, Formatter<Long> formatter) {
        long difference = Math.abs(before - after);
        this.text = formatter.apply(difference);
        this.direction = getDirection(before, after);
        this.reversed = reversed;
    }

    public Trend(double before, double after, boolean reversed, Formatter<Double> formatter) {
        double difference = Math.abs(before - after);
        this.text = formatter.apply(difference);
        this.direction = getDirection(before, after);
        this.reversed = reversed;
    }

    private String getDirection(double before, double after) {
        if (before < after) {
            return "+";
        } else if (before > after) {
            return "-";
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getDirection() {
        return direction;
    }

    public boolean isReversed() {
        return reversed;
    }
}