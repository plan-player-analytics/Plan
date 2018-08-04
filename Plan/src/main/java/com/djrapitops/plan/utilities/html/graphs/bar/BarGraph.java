package com.djrapitops.plan.utilities.html.graphs.bar;

import com.djrapitops.plan.utilities.html.graphs.HighChart;
import org.apache.commons.text.TextStringBuilder;

import java.util.List;

public class BarGraph implements HighChart {

    private final List<Bar> bars;

    public BarGraph(List<Bar> bars) {
        this.bars = bars;
    }

    public String toHighChartsCategories() {
        TextStringBuilder categories = new TextStringBuilder("[");
        categories.appendWithSeparators(bars.stream().map(bar -> "'" + bar.getLabel() + "'").iterator(), ",");
        return categories.append("]").toString();
    }

    @Override
    public String toHighChartsSeries() {
        TextStringBuilder series = new TextStringBuilder("[");
        series.appendWithSeparators(bars.stream().map(Bar::getValue).iterator(), ",");
        return series.append("]").toString();
    }
}
