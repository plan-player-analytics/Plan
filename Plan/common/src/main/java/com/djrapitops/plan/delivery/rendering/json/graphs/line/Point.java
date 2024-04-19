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
package com.djrapitops.plan.delivery.rendering.json.graphs.line;

import com.djrapitops.plan.delivery.domain.DateObj;

import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class Point {
    private final double x;
    private Double y;

    public Point(double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public <V extends Number> Point(double x, V y) {
        this.x = x;
        this.y = y == null ? null : y.doubleValue();
    }

    public static <V extends Number> Point fromDateObj(DateObj<V> dateObj) {
        V value = dateObj.getValue();
        return new Point(dateObj.getDate(), value != null ? value.doubleValue() : null);
    }

    public double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
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

    public Number[] toArray() {
        return new Number[]{x, y};
    }
}
