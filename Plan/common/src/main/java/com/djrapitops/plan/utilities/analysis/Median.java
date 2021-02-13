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

import java.util.Collections;
import java.util.List;

/**
 * Math utility for calculating the median from Integer values.
 *
 * @param <T> a {@code Number} object which implements {@code Comparable} (In general every standard Java number)
 * @author AuroraLS3
 */
public class Median<T extends Number & Comparable<? super T>> {

    private final List<T> values;
    private final int size;

    private Median(List<T> values) {
        this.values = values;
        Collections.sort(values);
        size = values.size();
    }

    /**
     * Creates a Median instance
     *
     * @param list the input list
     * @return an instance of {@code Median} for the List given
     */
    public static <T extends Number & Comparable<? super T>> Median<T> forList(List<T> list) {
        return new Median<>(list);
    }

    public double calculate() {
        if (values.isEmpty()) {
            return -1;
        }
        if (size % 2 == 0) {
            return calculateEven();
        } else {
            return calculateOdd();
        }
    }

    private double calculateEven() {
        int half = size / 2;
        double x1 = values.get(half).doubleValue();
        double x2 = values.get(half - 1).doubleValue();
        return (x1 + x2) / 2;
    }

    private double calculateOdd() {
        int half = size / 2;
        return values.get(half).doubleValue();
    }
}