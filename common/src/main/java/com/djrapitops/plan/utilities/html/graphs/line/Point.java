package com.djrapitops.plan.utilities.html.graphs.line;

import java.util.Objects;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class Point {
    private final double x;
    private final Double y;

    public Point(double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y) {
        this(x, (Double) y);
    }

    public double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 &&
                Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x + ", " +
                "y=" + y + '}';
    }
}
