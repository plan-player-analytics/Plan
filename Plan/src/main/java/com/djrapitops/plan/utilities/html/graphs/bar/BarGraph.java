package com.djrapitops.plan.utilities.html.graphs.bar;

import com.djrapitops.plan.utilities.html.graphs.HighChart;

import java.util.List;

public class BarGraph implements HighChart {

    private final List<Bar> bars;

    public BarGraph(List<Bar> bars) {
        this.bars = bars;
    }

    public String toHighChartsCategories() {
        StringBuilder categories = new StringBuilder("[");

        int i = 0;
        int size = bars.size();
        for (Bar bar : bars) {
            categories.append("'").append(bar.getLabel()).append("'");
            if (i < size - 1) {
                categories.append(",");
            }
            i++;
        }

        return categories.append("]").toString();
    }

    @Override
    public String toHighChartsSeries() {
        StringBuilder series = new StringBuilder("[");

        int i = 0;
        int size = bars.size();
        for (Bar bar : bars) {
            series.append(bar.getValue());
            if (i < size - 1) {
                series.append(",");
            }
            i++;
        }

        return series.append("]").toString();
    }
}
