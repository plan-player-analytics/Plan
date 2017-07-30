package main.java.com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.utilities.comparators.PointComparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Fuzzlemann on 30.07.2017.
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
