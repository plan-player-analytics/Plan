/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.utilities.analysis.DouglasPeuckerAlgorithm;
import com.djrapitops.plan.utilities.analysis.Point;
import com.djrapitops.plan.utilities.analysis.ReduceGapTriangles;
import com.djrapitops.plan.utilities.html.graphs.HighChart;

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
        for (int i = 0; i < size; i++) {
            Point point = points.get(i);
            double y = point.getY();
            long date = (long) point.getX();
            arrayBuilder.append("[").append(date).append(",").append(y).append("]");
            if (i < size - 1) {
                arrayBuilder.append(",");
            }
        }

        arrayBuilder.append("]");
        return arrayBuilder.toString();
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
