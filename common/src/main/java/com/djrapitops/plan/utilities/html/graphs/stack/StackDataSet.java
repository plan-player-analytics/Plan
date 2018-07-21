/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.stack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a value set for a Stack graph.
 * <p>
 * Stack graphs have labels that are defined separately and each StackDataSet represents a "line" in the stack graph.
 * <p>
 * Each StackDataSet can have a HTML color hex.
 *
 * @author Rsl1122
 */
public class StackDataSet extends ArrayList<Double> {

    private final String name;
    private final String color;

    public StackDataSet(List<Double> values, String name, String color) {
        super(values);
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
