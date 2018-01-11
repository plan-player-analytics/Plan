package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.container.TPS;

import java.util.Comparator;

/**
 * Compares TPS objects so that earliest is first.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPSComparator implements Comparator<TPS> {

    @Override
    public int compare(TPS o1, TPS o2) {
        return Long.compare(o1.getDate(), o2.getDate());
    }

}
