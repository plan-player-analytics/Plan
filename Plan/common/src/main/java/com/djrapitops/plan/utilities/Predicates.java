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
package com.djrapitops.plan.utilities;

import com.djrapitops.plan.delivery.domain.DateHolder;

import java.util.function.Predicate;

/**
 * Utility class for different Predicates used in the plugin.
 *
 * @author AuroraLS3
 */
public class Predicates {

    private Predicates() {
        /* static method class */
    }

    public static <T extends DateHolder> Predicate<T> within(long after, long before) {
        return holder -> {
            long date = holder.getDate();
            return after < date && date <= before;
        };
    }

    public static boolean pingInRange(double value) {
        return value > 0 && value <= 4000;
    }
}
