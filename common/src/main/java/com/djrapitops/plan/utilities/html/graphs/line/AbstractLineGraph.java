/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.html.graphs.HighChart;
import com.djrapitops.plan.utilities.html.graphs.line.alg.DouglasPeuckerAlgorithm;
import com.djrapitops.plan.utilities.html.graphs.line.alg.ReduceGapTriangles;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is a LineGraph for any set of Points, thus it is Abstract.
 *
 * @author Rsl1122
 * @since 4.2.0
 */
public class AbstractLineGraph implements HighChart {

    protected List<Point> points;
    protected boolean reduceGapTriangles = false;
    protected boolean reducePoints = false;

    public AbstractLineGraph() {
        points = new ArrayList<>();
    }

    public AbstractLineGraph(List<Point> points) {
        this.points = points;
    }

    @Override
    public String toHighChartsSeries() {
        StringBuilder arrayBuilder = new StringBuilder("[");

        if (reducePoints) {
            points = DouglasPeuckerAlgorithm.reducePoints(points, 0);
        }
        if (reduceGapTriangles) {
            points = ReduceGapTriangles.reduce(points);
        }

        int size = points.size();
        Long lastX = null;
        boolean addMissingPoints = Settings.DISPLAY_GAPS_IN_GRAPH_DATA.isTrue();
        for (int i = 0; i < size; i++) {
            Point point = points.get(i);
            Double y = point.getY();
            long date = (long) point.getX();
            if (addMissingPoints && lastX != null && date - lastX > TimeAmount.MINUTE.ms() * 3L) {
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
        long iterate = lastX + TimeAmount.MINUTE.ms();
        while (iterate < date) {
            arrayBuilder.append("[").append(iterate).append(",null],");
            iterate += TimeAmount.MINUTE.ms() * 30L;
        }
    }

    public void reduceGapTriangles() {
        this.reduceGapTriangles = true;
    }

    public void reducePoints() {
        this.reducePoints = true;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void addPoints(Collection<Point> points) {
        this.points.addAll(points);
    }
}
