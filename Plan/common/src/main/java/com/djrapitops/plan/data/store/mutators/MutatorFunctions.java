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
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.utilities.html.graphs.line.Point;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class MutatorFunctions {

    public static List<Point> toPoints(NavigableMap<Long, Integer> map) {
        return map.entrySet().stream()
                .map(entry -> new Point(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static List<Point> toPointsWithRemovedOffset(NavigableMap<Long, Integer> map, TimeZone timeZone) {
        return map.entrySet().stream()
                .map(entry -> new Point(entry.getKey() - timeZone.getOffset(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static int average(Map<Long, Integer> map) {
        return (int) map.values().stream()
                .mapToInt(i -> i)
                .average().orElse(0);
    }

    private MutatorFunctions() {
        // Static method class.
    }

}
