/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.html.graphs.line;

/**
 * This math object is used in Ramer–Douglas–Peucker algorithm.
 * <p>
 * https://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm
 *
 * @author Rsl1122
 */
public class Line {

    private final Point start;
    private final Point end;
    private final double slope;
    private final double c;
    private final double crossPoint;

    public Line(Point one, Point two) {
        start = one;
        end = two;
        double x1 = one.getX();
        double x2 = two.getX();
        double y1 = one.getY();
        double y2 = two.getY();

        slope = (y2 - y1) / (x2 - x1);
        c = y1 - slope * x1;
        crossPoint = c / slope;
    }

    public double getA() {
        return getSlope();
    }

    public double getSlope() {
        return slope;
    }

    public double getC() {
        return c;
    }

    public double getCrossPoint() {
        return crossPoint;
    }

    public double getLength() {
        double x1 = start.getX();
        double x2 = end.getX();
        double y1 = start.getY();
        double y2 = end.getY();
        return Math.sqrt(Math.pow((x2 - x1), 2) + (Math.pow((y2 - y1), 2)));
    }

    public double getPerpendicularDistance(Point from) {
        double a = getA();
        double x = from.getX();
        double y = from.getY();
        return Math.abs(a * x - y + c) / Math.sqrt(Math.pow(a, 2) + 1);
    }
}
