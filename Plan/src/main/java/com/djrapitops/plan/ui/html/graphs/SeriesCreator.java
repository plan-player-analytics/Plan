/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.html.graphs;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.utilities.analysis.DouglasPeuckerAlgorithm;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.comparators.PointComparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract scatter graph creator used by other graph creators.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class SeriesCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private SeriesCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String seriesGraph(List<Point> points, boolean reduceGapTriangles) {
        return seriesGraph(points, reduceGapTriangles, true);
    }

    public static String seriesGraph(List<Point> points, boolean reduceGapTriangles, boolean reducePoints) {
        StringBuilder arrayBuilder = new StringBuilder("[");

        if (reducePoints) {
            points = DouglasPeuckerAlgorithm.reducePoints(points, 0);
        }

        if (reduceGapTriangles) {
            Point lastPoint = null;

            Set<Point> toAdd = new HashSet<>();
            for (Point point : points) {
                if (Verify.notNull(point, lastPoint)) {
                    long date = (long) point.getX();
                    long lastDate = (long) lastPoint.getX();
                    double y = point.getY();
                    double lastY = lastPoint.getY();
                    if (Double.compare(y, lastY) != 0 && Math.abs(lastY - y) > 0.5) {
                        if (lastDate < date - TimeAmount.MINUTE.ms() * 10L) {
                            toAdd.add(new Point(lastDate + 1, lastY));
                            toAdd.add(new Point(date - 1, lastY));
                        }
                    }
                }
                lastPoint = point;
            }
            points.addAll(toAdd);
            points.sort(new PointComparator());
        }

        int size = points.size();
        for (int i = 0; i < size; i++) {
            Point point = points.get(i);
            double y = point.getY();
            long date = (long) point.getX();
            arrayBuilder.append("{x:").append(date).append(",y:").append(y).append("}");
            if (i < size - 1) {
                arrayBuilder.append(",");
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }
}
