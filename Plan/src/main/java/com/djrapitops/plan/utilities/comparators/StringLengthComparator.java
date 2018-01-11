package com.djrapitops.plan.utilities.comparators;

import java.util.Comparator;

/**
 * Compares Strings and sorts them by length (Longest fist).
 *
 * @author Rsl1122
 * @since 3.6.2
 */
public class StringLengthComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return -Long.compare(o1.length(), o2.length());
    }
}
