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
package com.djrapitops.plan.delivery.rendering.json.datapoint;

/**
 * @author AuroraLS3
 */
public class DatapointValue {

    private final DatapointType type;
    private final Object value;
    private final Datapoint.FormatType formatType;
    private final long timestamp;

    public DatapointValue(DatapointType type, Object value, Datapoint.FormatType formatType) {
        this.type = type;
        this.value = value;
        this.formatType = formatType;
        this.timestamp = System.currentTimeMillis();
    }

    public DatapointType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Datapoint.FormatType getFormatType() {
        return formatType;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
