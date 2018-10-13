/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.utilities.html.graphs.HighChart;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a LineGraph for any set of Points, thus it is Abstract.
 *
 * @author Rsl1122
 * @since 4.2.0
 */
public class LineGraph implements HighChart {

    private final boolean displayGaps;
    private List<Point> points;

    public LineGraph(List<Point> points, boolean displayGaps) {
        this.points = points;
        this.displayGaps = displayGaps;
    }

    @Override
    public String toHighChartsSeries() {
        StringBuilder arrayBuilder = new StringBuilder("[");

        int size = points.size();
        Long lastX = null;
        for (int i = 0; i < size; i++) {
            Point point = points.get(i);
            Double y = point.getY();
            long date = (long) point.getX();

            if (displayGaps && lastX != null && date - lastX > TimeUnit.MINUTES.toMillis(3L)) {
                addMissingPoints(arrayBuilder, lastX, date);
            }
            lastX = date;

            arrayBuilder.append("[").append(date).append(",").append(y).append("]");
            if (i < size - 1) {
                arrayBuilder.append(",");
            }
        }

        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }

    private void addMissingPoints(StringBuilder arrayBuilder, Long lastX, long date) {
        long iterate = lastX + TimeUnit.MINUTES.toMillis(1L);
        while (iterate < date) {
            arrayBuilder.append("[").append(iterate).append(",null],");
            iterate += TimeUnit.MINUTES.toMillis(30L);
        }
    }
}
