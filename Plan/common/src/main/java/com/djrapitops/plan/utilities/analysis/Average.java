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

/**
 * Utility for averaging data.
 *
 * @author AuroraLS3
 */
public class Average {

    private double total;
    private int count;

    public Average() {
        total = 0.0;
        count = 0;
    }

    /**
     * Add a new entry and check if save should be done.
     *
     * @param value TPS value
     */
    public void add(double value) {
        total += value;
        count++;
    }

    public double getAverageAndReset() {
        if (count == 0) return -1;
        double average = total / count;
        total = 0.0;
        count = 0;
        return average;
    }

    public void addNonNull(Double value) {
        if (value != null && !value.isNaN()) {
            add(value);
        }
    }

    public void addPositive(long[] values) {
        for (long value : values) {
            if (value > 0) add(value);
        }
    }
}