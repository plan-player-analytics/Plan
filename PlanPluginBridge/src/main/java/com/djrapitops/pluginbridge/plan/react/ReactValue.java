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
package com.djrapitops.pluginbridge.plan.react;

import com.volmit.react.api.SampledType;

/**
 * Data container for React data points.
 *
 * @author Rsl1122
 */
public class ReactValue implements Comparable<ReactValue> {

    private final SampledType type;
    private final long date;
    private final double dataValue;

    public ReactValue(SampledType type, long date, double dataValue) {
        this.type = type;
        this.date = date;
        this.dataValue = dataValue;
    }

    public SampledType getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public double getDataValue() {
        return dataValue;
    }

    @Override
    public int compareTo(ReactValue o) {
        return Long.compare(this.date, o.date);
    }
}