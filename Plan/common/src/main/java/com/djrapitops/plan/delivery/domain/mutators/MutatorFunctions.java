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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.rendering.json.graphs.line.LineGraph;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plugin.utilities.Verify;

import java.util.*;

public class MutatorFunctions {

    public static List<Point> toPoints(NavigableMap<Long, Integer> map) {
        return Lists.map(map.entrySet(), entry -> new Point(entry.getKey(), entry.getValue()));
    }

    public static NavigableMap<Long, Integer> addMissing(NavigableMap<Long, Integer> points, long accuracy, Integer replacement) {
        if (Verify.isEmpty(points)) return points;

        NavigableMap<Long, Integer> filled = new TreeMap<>();
        Long lastX = null;
        for (Map.Entry<Long, Integer> point : points.entrySet()) {
            long date = point.getKey();

            if (lastX != null && date - lastX > accuracy) {
                addMissing(lastX, date, filled, accuracy, replacement);
            }
            lastX = date;
            filled.put(point.getKey(), point.getValue());
        }

        long now = System.currentTimeMillis();
        if (lastX != null && now - lastX > accuracy) {
            addMissing(lastX, now, filled, accuracy, replacement);
        }

        return filled;
    }

    private static void addMissing(long from, long to, NavigableMap<Long, Integer> points, long accuracy, Integer replacement) {
        long iterate = from;
        while (iterate < to) {
            points.putIfAbsent(iterate, replacement);
            iterate += accuracy;
        }
    }

    public static List<Point> addMissing(List<Point> points, LineGraph.GapStrategy gapStrategy) {
        if (Verify.isEmpty(points)) return points;

        List<Point> filled = new ArrayList<>();
        Long lastX = null;
        for (Point point : points) {
            long date = (long) point.getX();

            if (lastX != null && date - lastX > gapStrategy.acceptableGapMs) {
                addMissing(lastX, date, filled, gapStrategy);
            }
            lastX = date;
            filled.add(point);
        }

        return filled;
    }

    private static void addMissing(long from, long to, List<Point> points, LineGraph.GapStrategy gapStrategy) {
        long iterate = from + gapStrategy.diffToFirstGapPointMs;
        while (iterate < to) {
            points.add(new Point(iterate, gapStrategy.fillWith));
            iterate += gapStrategy.fillFrequencyMs;
        }
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
