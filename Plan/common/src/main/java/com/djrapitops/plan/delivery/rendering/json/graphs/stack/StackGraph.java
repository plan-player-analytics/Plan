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

import com.djrapitops.plan.delivery.rendering.json.graphs.HighChart;

/**
 * Utility for creating HighCharts Stack graphs.
 *
 * @author Rsl1122
 */
public class StackGraph implements HighChart {

    private final StackDataSet[] dataSets;
    private final String[] labels;

    public StackGraph(String[] labels, StackDataSet... dataSets) {
        this.dataSets = dataSets;
        this.labels = labels;
    }

    public String[] getLabels() {
        return labels;
    }

    public String toHighChartsLabels() {
        StringBuilder labelBuilder = new StringBuilder("[");

        int length = this.labels.length;
        int i = 0;
        for (String label : this.labels) {
            labelBuilder.append('"').append(label).append('"');

            if (i < length - 1) {
                labelBuilder.append(",");
            }
            i++;
        }

        return labelBuilder.append("]").toString();
    }

    public StackDataSet[] getDataSets() {
        return dataSets;
    }

    @Override
    public String toHighChartsSeries() {
        StringBuilder seriesBuilder = new StringBuilder("[");

        int size = dataSets.length;
        int i = 0;
        for (StackDataSet dataSet : dataSets) {
            seriesBuilder.append(dataSet.toSeriesObjectString());

            if (i < size - 1) {
                seriesBuilder.append(",");
            }
            i++;
        }

        return seriesBuilder.append("]").toString();
    }
}
