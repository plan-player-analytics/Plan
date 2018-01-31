package com.djrapitops.plan.utilities.html.graphs.line.alg;

import com.djrapitops.plan.utilities.comparators.PointComparator;
import com.djrapitops.plan.utilities.html.graphs.line.Point;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                    && lastDate < date - TimeAmount.MINUTE.ms() * 10L) {
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
