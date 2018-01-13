/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
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

    public PieSlice(String name, long y, String color, boolean drilldown) {
        this.name = name;
        this.y = y;
        this.color = color;
        this.drilldown = drilldown;
    }

    @Override
    public String toString() {
        return "{name:'" + name + "'," +
                "y:" + y + "," +
                "color:" + color
                + (drilldown ? "," + "drilldown: '" + name + "'" : "")
                + "}";
    }

    public long getY() {
        return y;
    }
}