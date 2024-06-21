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

import com.djrapitops.plan.extension.annotation.GraphPointProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Represents series of values at point x.
 * <p>
 * x can be timestamp in milliseconds, time, or a value, depending on {@link XAxisType} chosen with {@link GraphPointProvider#xAxisType()}.
 * <p>
 * Values can be integers, longs or doubles, even though they're stored as doubles in the database.
 * <p>
 * Series are defined by index in the array. If you want more series, you need to add more data to the array.
 * <p>
 * nulls can be inserted in the middle if some series suddenly stops having data.
 * <p>
 * Requires capability DATA_EXTENSION_GRAPH_API.
 *
 * @author AuroraLS3
 */
public class DataPoint {

    private final long x;
    private final List<Double> values;

    public DataPoint(long x, Double... values) {
        this(x, Arrays.asList(values));
    }

    public DataPoint(long x, List<Double> values) {
        this.x = x;
        this.values = values;
    }

    public long getX() {
        return x;
    }

    public List<Double> getValues() {
        return values;
    }
}
