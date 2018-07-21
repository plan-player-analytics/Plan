/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.stack;

import com.djrapitops.plan.utilities.html.graphs.HighChart;

/**
 * Utility for creating HighCharts Stack graphs.
 *
 * @author Rsl1122
 */
public class AbstractStackGraph implements HighChart {

    private final StackDataSet[] dataSets;
    private final String[] labels;

    public AbstractStackGraph(String[] labels, StackDataSet... dataSets) {
        this.dataSets = dataSets;
        this.labels = labels;
    }

    public String toHighChartsLabels() {
        StringBuilder labelBuilder = new StringBuilder("[");

        int length = this.labels.length;
        int i = 0;
        for (String label : this.labels) {
            labelBuilder.append("'").append(label).append("'");

            if (i < length - 1) {
                labelBuilder.append(",");
            }
            i++;
        }

        return labelBuilder.append("]").toString();
    }

    private String toSeries(StackDataSet dataSet) {
        StringBuilder dataSetBuilder = new StringBuilder("{name: '");

        dataSetBuilder.append(dataSet.getName()).append("',")
                .append("color:").append(dataSet.getColor())
                .append(",data: [");

        int size = dataSet.size();
        int i = 0;
        for (Double value : dataSet) {
            dataSetBuilder.append(value);
            if (i < size - 1) {
                dataSetBuilder.append(",");
            }
            i++;
        }

        return dataSetBuilder.append("]}").toString();
    }

    @Override
    public String toHighChartsSeries() {
        StringBuilder seriesBuilder = new StringBuilder("[");

        int size = dataSets.length;
        int i = 0;
        for (StackDataSet dataSet : dataSets) {
            seriesBuilder.append(toSeries(dataSet));

            if (i < size - 1) {
                seriesBuilder.append(",");
            }
            i++;
        }

        return seriesBuilder.append("]").toString();
    }
}
