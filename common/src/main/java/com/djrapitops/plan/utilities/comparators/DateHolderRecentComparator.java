package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.store.objects.DateHolder;

import java.util.Comparator;

/**
 * Compares DateHolder objects so that most recent is first.
 *
 * @author Rsl1122
 */
public class DateHolderRecentComparator implements Comparator<DateHolder> {

    @Override
    public int compare(DateHolder one, DateHolder two) {
        return Long.compare(two.getDate(), one.getDate());
    }
}
