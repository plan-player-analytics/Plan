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
package com.djrapitops.plan.utilities.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Utility for calculating nth percentile values, e.g. 95th percentile.
 *
 * @author AuroraLS3
 */
public class Distribution {

    private final List<Double> values;

    public Distribution() {
        values = new ArrayList<>();
    }

    public void add(double value) {
        values.add(value);
    }

    /**
     * Get the highest value within a percentile.
     *
     * @param percentile Percentage 0.0 to 1.0 of values to include
     * @return Highest value that matches percentile
     */
    public Optional<Double> getNthPercentile(double percentile) {
        values.sort(Double::compareTo);
        int count = values.size();
        int lastInPercentile = (int) Math.floor(count * percentile) - 1;
        if (lastInPercentile <= 0) return Optional.empty();
        return Optional.of(values.get(lastInPercentile));
    }

    public void reset() {
        values.clear();
    }

    public void addPositive(long[] values, UnaryOperator<Long> mappingFunction) {
        for (long value : values) {
            if (value > 0) add(mappingFunction.apply(value));
        }
    }
}
