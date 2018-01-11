/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import java.util.List;

/**
 * PieSeries data creation utility class.
 *
 * @author Rsl1122
 */
public class PieSeries {
    private PieSeries() {
        throw new IllegalStateException("Utility Class");
    }

    public static String createSeries(List<PieSlice> slices) {
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
}