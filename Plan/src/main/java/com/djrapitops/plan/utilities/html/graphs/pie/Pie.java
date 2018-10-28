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
