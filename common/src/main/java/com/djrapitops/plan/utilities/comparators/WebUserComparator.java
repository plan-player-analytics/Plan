package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.WebUser;

import java.util.Comparator;

/**
 * Orders WebUsers in descending order by permission level.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUserComparator implements Comparator<WebUser> {

    @Override
    public int compare(WebUser o1, WebUser o2) {
        return Integer.compare(o2.getPermLevel(), o1.getPermLevel());
    }

}
