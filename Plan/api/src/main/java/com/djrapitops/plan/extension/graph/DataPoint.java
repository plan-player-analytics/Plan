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

import com.djrapitops.plan.extension.annotation.GraphProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents series of values at point x.
 * <p>
 * x can be timestamp in milliseconds, time, or a value, depending on {@link XAxisType} chosen with {@link GraphProvider#xAxisType()}.
 * <p>
 * Values can be integers, longs or doubles, even though they're stored as doubles in the database.
 * VALUES OF SAME SERIES OF DATA NEED TO BE AT SAME INDEX ALL THE TIME.
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

    public DataPoint(long x, Number... values) {
        this(x, Arrays.asList(values));
    }

    public DataPoint(long x, List<Number> values) {
        this.x = x;
        this.values = values.stream().map(Number::doubleValue).collect(Collectors.toList());
    }

    public long getX() {
        return x;
    }

    public List<Double> getValues() {
        return values;
    }
}
