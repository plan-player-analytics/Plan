package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.container.Action;

import java.util.Comparator;

/**
 * Comparator for comparing Actions so that latest is the first component.
 *
 * @author Rsl1122
 */
@Deprecated
public class ActionComparator implements Comparator<Action> {

    @Override
    public int compare(Action o1, Action o2) {
        return -Long.compare(o1.getDate(), o2.getDate());
    }

}
