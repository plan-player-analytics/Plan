package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.Comparator;
import main.java.com.djrapitops.plan.data.TPS;

/**
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
