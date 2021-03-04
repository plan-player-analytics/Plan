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
package com.djrapitops.plan.delivery.rendering.json.graphs.pie;

/**
 * Represents a slice of a pie.
 *
 * @author AuroraLS3
 */
public class PieSlice {
    private final String name;
    private final long y;
    private final String color;
    private final String drilldown;

    public PieSlice(String name, long y) {
        this(name, y, null, false);
    }

    public PieSlice(String name, long y, String color) {
        this(name, y, color, false);
    }

    public PieSlice(String name, long y, boolean drilldown) {
        this(name, y, null, drilldown);
    }

    public PieSlice(String name, long y, String color, boolean drilldown) {
        this.name = name;
        this.y = y;
        this.color = color;
        this.drilldown = drilldown ? name : null;
    }

    @Override
    public String toString() {
        return "{name:'" + name + "'," +
                "y:" + y
                + (color != null ? "," + "color:" + color : "")
                + (drilldown != null ? "," + "drilldown: '" + drilldown + "'" : "")
                + "}";
    }

    public long getY() {
        return y;
    }
}
