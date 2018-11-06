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
