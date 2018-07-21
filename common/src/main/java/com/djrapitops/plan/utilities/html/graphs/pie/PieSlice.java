/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

/**
 * Represents a slice of a pie.
 *
 * @author Rsl1122
 */
public class PieSlice {
    private final String name;
    private final long y;
    private final String color;
    private final boolean drilldown;

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
        this.drilldown = drilldown;
    }

    @Override
    public String toString() {
        return "{name:'" + name + "'," +
                "y:" + y
                + (color != null ? "," + "color:" + color : "")
                + (drilldown ? "," + "drilldown: '" + name + "'" : "")
                + "}";
    }

    public long getY() {
        return y;
    }
}
