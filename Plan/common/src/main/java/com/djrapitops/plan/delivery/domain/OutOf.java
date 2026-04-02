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
package com.djrapitops.plan.delivery.domain;

import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;

/**
 * @author AuroraLS3
 */
public class OutOf {
    private final long value;
    private final long max;
    private final double percentage;
    private final Datapoint.FormatType formatType;

    public OutOf(long value, long max, Datapoint.FormatType formatType) {
        this.value = value;
        this.max = max;
        this.formatType = formatType;
        this.percentage = max != 0L ? value * 1.0 / max : 0.0;
    }

    public long getValue() {
        return value;
    }

    public long getMax() {
        return max;
    }

    public double getPercentage() {
        return percentage;
    }

    public Datapoint.FormatType getFormatType() {
        return formatType;
    }
}
