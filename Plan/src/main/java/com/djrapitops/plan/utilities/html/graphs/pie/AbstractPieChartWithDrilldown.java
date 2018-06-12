/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import java.util.List;

/**
 * PieChart with a Pie about each slice as well.
 *
 * @author Rsl1122
 */
public abstract class AbstractPieChartWithDrilldown extends AbstractPieChart {

    public AbstractPieChartWithDrilldown() {
    }

    public AbstractPieChartWithDrilldown(List<PieSlice> slices) {
        super(slices);
    }

    public abstract String toHighChartsDrilldown();

}
