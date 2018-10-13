/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.utilities.html.graphs.HighChart;
import org.apache.commons.text.TextStringBuilder;

import java.util.List;

/**
 * This is a PieChart for any set of PieSlices, thus it is Abstract.
 *
 * @author Rsl1122
 * @since 4.2.0
 */
public class Pie implements HighChart {

    protected final List<PieSlice> slices;

    public Pie(List<PieSlice> slices) {
        this.slices = slices;
    }

    @Override
    public String toHighChartsSeries() {
        TextStringBuilder series = new TextStringBuilder("[");
        series.appendWithSeparators(slices, ",");
        return series.append("]").toString();
    }
}
