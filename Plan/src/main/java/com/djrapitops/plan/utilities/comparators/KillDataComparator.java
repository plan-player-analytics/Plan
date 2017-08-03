package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.KillData;

import java.util.Comparator;

/**
 * @author Fuzzlemann
 */
public class KillDataComparator implements Comparator<KillData> {

    @Override
    public int compare(KillData o1, KillData o2) {
        return Long.compare(o1.getDate(), o2.getDate());
    }

}
