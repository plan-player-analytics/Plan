package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.container.Session;

import java.util.Comparator;

/**
 * Comparator for Sessions in descending length order.
 *
 * @author Rsl1122
 */
public class SessionLengthComparator implements Comparator<Session> {

    @Override
    public int compare(Session s1, Session s2) {
        return -Long.compare(s1.getLength(), s2.getLength());
    }
}
