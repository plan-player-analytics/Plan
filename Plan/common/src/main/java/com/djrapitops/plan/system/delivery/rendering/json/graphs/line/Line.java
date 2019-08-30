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
package com.djrapitops.plan.system.delivery.rendering.json.graphs.line;

/**
 * This math object is used in Ramer–Douglas–Peucker algorithm.
 *
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
        return Math.sqrt(Math.pow(x2 - x1, 2) + (Math.pow(y2 - y1, 2)));
    }

    public double getPerpendicularDistance(Point from) {
        double a = getA();
        double x = from.getX();
        double y = from.getY();
        return Math.abs(a * x - y + c) / Math.sqrt(Math.pow(a, 2) + 1);
    }
}
