/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.HasDate;

import java.util.Comparator;

/**
 * Comparator for HasDate interface Objects.
 *
 * @author Rsl1122
 */
public class HasDateComparator implements Comparator<HasDate> {

    private final boolean reversed;

    public HasDateComparator() {
        this(false);
    }

    public HasDateComparator(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public int compare(HasDate o1, HasDate o2) {
        return (reversed ? -1 : 1) * Long.compare(o1.getDate(), o2.getDate());
    }
}