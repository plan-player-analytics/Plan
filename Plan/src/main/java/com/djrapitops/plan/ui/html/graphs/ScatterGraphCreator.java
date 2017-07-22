/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.html.graphs;

import com.djrapitops.plugin.api.TimeAmount;
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

        if (reduceGapTriangles) {
            long lastDate = MiscUtils.getTime();
            double lastY = -1;
            Set<Point> toAdd = new HashSet<>();
            Iterator<Point> iterator = points.iterator();
            while (iterator.hasNext()) {
                Point point = iterator.next();
                double y = point.getY();
                long date = (long) point.getX();
                if (lastY > 0 || y > 0) {
                    if (lastDate < date - TimeAmount.MINUTE.ms() * 10L) {
                        toAdd.add(new Point(lastDate + 1, 0));
                        toAdd.add(new Point(date - 1, 0));
                    }
                }
                lastDate = date;
                lastY = y;
            }
            points.addAll(toAdd);
            Collections.sort(points, new PointComparator());
        }

//        points = DouglasPeckerAlgorithm.reducePoints(points, -1);
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
