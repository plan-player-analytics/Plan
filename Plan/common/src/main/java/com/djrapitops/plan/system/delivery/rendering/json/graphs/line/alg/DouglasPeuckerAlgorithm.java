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
package com.djrapitops.plan.system.delivery.rendering.json.graphs.line.alg;

import com.djrapitops.plan.system.delivery.rendering.json.graphs.line.Line;
import com.djrapitops.plan.system.delivery.rendering.json.graphs.line.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Ramer-Douglas-Peucker Point Reduction Algorithm Implementation for reducing points from graphs.
 *
 * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 *
 * @author Rsl1122
 */
public class DouglasPeuckerAlgorithm {

    /**
     * Constructor used to hide the public constructor
     */
    private DouglasPeuckerAlgorithm() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Point> reducePoints(List<Point> points, double epsilon) {
        if (points.isEmpty()) {
            return points;
        }

        if (Double.compare(epsilon, -1) == 0) {
            epsilon = 0.002;
        }

        int size = points.size();
        final int lastIndex = size - 1;
        final Point start = points.get(0);
        final Point end = points.get(lastIndex);

        // Max distance and it's index.
        double dMax = 0;
        int index = 0;
        for (int i = 1; i < size; i++) {
            double d = perpendicularDistance(points.get(i), new Line(start, end));
            if (d > dMax) {
                dMax = d;
                index = i;
            }
        }

        List<Point> results;
        if (dMax > epsilon) {
            List<Point> results1 = reducePoints(points.subList(0, index), epsilon);
            List<Point> results2 = reducePoints(points.subList(index, lastIndex), epsilon);

            results = new ArrayList<>();
            results.addAll(results1.subList(0, results1.size() - 1));
            results.addAll(results2);
        } else {
            return Arrays.asList(points.get(0), points.get(lastIndex));
        }
        return results;
    }

    private static double perpendicularDistance(Point point, Line line) {
        return line.getPerpendicularDistance(point);
    }
}
