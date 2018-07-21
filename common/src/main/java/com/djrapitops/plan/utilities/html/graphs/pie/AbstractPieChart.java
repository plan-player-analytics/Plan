/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.utilities.html.graphs.HighChart;

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
        StringBuilder seriesBuilder = new StringBuilder("[");
        int i = 0;
        int size = slices.size();
        for (PieSlice slice : slices) {
            seriesBuilder.append(slice.toString());
            if (i < size - 1) {
                seriesBuilder.append(",");
            }
            i++;
        }
        seriesBuilder.append("]");

        return seriesBuilder.toString();
    }

    public void setSlices(List<PieSlice> slices) {
        this.slices = slices;
    }

    public void addSlices(List<PieSlice> slices) {
        this.slices.addAll(slices);
    }
}
