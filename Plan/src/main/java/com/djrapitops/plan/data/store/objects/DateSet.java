package com.djrapitops.plan.data.store.objects;

import java.util.TreeSet;

/**
 * Basic TreeSet with Epoch ms as values.
 *
 * @author Rsl1122
 */
public class DateSet extends TreeSet<Long> {

    public boolean hasValuesBetween(long after, long before) {
        return countBetween(after, before) > 0;
    }

    public int countBetween(long after, long before) {
        return subSet(after, before).size();
    }

}