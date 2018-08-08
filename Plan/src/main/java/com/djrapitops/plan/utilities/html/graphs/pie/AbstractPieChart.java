/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.utilities.html.graphs.HighChart;
import org.apache.commons.text.TextStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a PieChart for any set of PieSlices, thus it is Abstract.
 *
 * @author Rsl1122
 * @since 4.2.0
 */
public class AbstractPieChart implements HighChart {

    protected List<PieSlice> slices;

    public AbstractPieChart() {
        slices = new ArrayList<>();
    }

    public AbstractPieChart(List<PieSlice> slices) {
        this.slices = slices;
    }

    @Override
    public String toHighChartsSeries() {
        TextStringBuilder series = new TextStringBuilder("[");
        series.appendWithSeparators(slices, ",");
        return series.append("]").toString();
    }

    public void setSlices(List<PieSlice> slices) {
        this.slices = slices;
    }

    public void addSlices(List<PieSlice> slices) {
        this.slices.addAll(slices);
    }
}
