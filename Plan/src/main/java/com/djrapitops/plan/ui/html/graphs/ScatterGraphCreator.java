/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.html.graphs;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.utilities.Verify;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.DouglasPeckerAlgorithm;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.comparators.PointComparator;

/**
 * Abstract scatter graph creator used by other graph creators.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ScatterGraphCreator {

    public static String scatterGraph(List<Point> points, boolean reduceGapTriangles) {
        StringBuilder arrayBuilder = new StringBuilder();
        arrayBuilder.append("[");

        points = DouglasPeckerAlgorithm.reducePoints(points, 0);

        if (reduceGapTriangles) {
            Point lastPoint = null;

            Set<Point> toAdd = new HashSet<>();
            for (Point point : points) {
                if (Verify.notNull(point, lastPoint)) {
                    long date = (long) point.getX();
                    long lastDate = (long) lastPoint.getX();
                    double y = point.getY();
                    double lastY = lastPoint.getY();
                    if (y != lastY && Math.abs(lastY - y) > 0.5) {
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
            arrayBuilder.append("{").append("x:").append(date).append(", y:").append(y).append("}");
            if (i < size - 1) {
                arrayBuilder.append(",");
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }
}
