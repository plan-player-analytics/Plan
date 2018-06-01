package com.djrapitops.plan.data.store.objects;

import java.util.TreeMap;

/**
 * Basic TreeMap that uses Epoch MS as keys.
 *
 * @author Rsl1122
 */
public class DateMap<T> extends TreeMap<Long, T> {

    public DateMap() {
        super(Long::compareTo);
    }

    public boolean hasValuesBetween(long after, long before) {
        return countBetween(after, before) > 0;
    }

    public int countBetween(long after, long before) {
        return subMap(after, before).size();
    }
}