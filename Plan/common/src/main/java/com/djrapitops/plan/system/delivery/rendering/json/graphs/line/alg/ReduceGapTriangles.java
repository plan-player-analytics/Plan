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

import com.djrapitops.plan.system.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.utilities.comparators.PointComparator;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Utility for reducing Points in LineGraphs.
 *
 * @author Rsl1122 (Refactored into this class by Fuzzlemann)
 */
public class ReduceGapTriangles {

    /**
     * Constructor used to hide the public constructor
     */
    private ReduceGapTriangles() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Point> reduce(List<Point> points) {
        Point lastPoint = null;

        Set<Point> toAdd = new HashSet<>();
        for (Point point : points) {
            if (!Verify.notNull(point, lastPoint)) {
                lastPoint = point;
                continue;
            }

            assert lastPoint != null;

            long date = (long) point.getX();
            long lastDate = (long) lastPoint.getX();
            double y = point.getY();
            double lastY = lastPoint.getY();

            if (Double.compare(y, lastY) != 0
                    && Math.abs(lastY - y) > 0.5
                    && lastDate < date - TimeUnit.MINUTES.toMillis(10L)) {
                toAdd.add(new Point(lastDate + 1, lastY));
                toAdd.add(new Point(date - 1, lastY));
            }

            lastPoint = point;
        }

        points.addAll(toAdd);
        points.sort(new PointComparator());

        return points;
    }
}
