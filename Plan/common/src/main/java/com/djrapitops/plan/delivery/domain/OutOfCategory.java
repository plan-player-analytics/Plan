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

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * @author AuroraLS3
 */
public class OutOfCategory {

    private final Map<String, Long> values;
    private final long max;
    @Nullable
    private String category;
    @Nullable
    private Double percentage;

    public OutOfCategory(Map<String, Long> values, long max) {
        this.values = values;
        this.max = max;
        long biggest = 0L;
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            if (entry.getValue() > biggest) {
                category = entry.getKey();
                biggest = entry.getValue();
                percentage = max != 0L ? biggest * 1.0 / max : 0.0;
            }
        }
    }

    public Map<String, Long> getValues() {
        return values;
    }

    public long getMax() {
        return max;
    }

    public @Nullable String getCategory() {
        return category;
    }

    public @Nullable Double getPercentage() {
        return percentage;
    }
}
