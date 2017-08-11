package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.Comparator;

/**
 * Compares Strings & sorts them by length
 *
 * @author Rsl1122
 * @since 3.6.2
 */
public class StringLengthComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return -Integer.compare(o1.length(), o2.length());
    }
}
