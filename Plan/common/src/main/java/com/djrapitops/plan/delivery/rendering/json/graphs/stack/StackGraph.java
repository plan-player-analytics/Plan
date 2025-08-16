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
package com.djrapitops.plan.delivery.rendering.json.graphs.stack;

import java.io.Serializable;

/**
 * Utility for creating HighCharts Stack graphs.
 *
 * @author AuroraLS3
 */
public class StackGraph {

    private final StackDataSet[] dataSets;
    private final Serializable[] labels;

    public StackGraph(Serializable[] labels, StackDataSet... dataSets) {
        this.dataSets = dataSets;
        this.labels = labels;
    }

    public Serializable[] getLabels() {
        return labels;
    }

    public StackDataSet[] getDataSets() {
        return dataSets;
    }

}
