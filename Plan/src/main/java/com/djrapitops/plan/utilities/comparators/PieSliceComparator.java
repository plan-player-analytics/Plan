/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.utilities.html.graphs.pie.PieSlice;

import java.util.Comparator;

/**
 * Compares PieSlices to descending Percentage order.
 */
public class PieSliceComparator implements Comparator<PieSlice> {

    @Override
    public int compare(PieSlice o1, PieSlice o2) {
        return -Long.compare(o1.getY(), o2.getY());
    }
}