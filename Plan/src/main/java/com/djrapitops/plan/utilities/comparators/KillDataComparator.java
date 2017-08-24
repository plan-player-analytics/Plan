package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.PlayerKill;

import java.util.Comparator;

/**
 * @author Fuzzlemann
 */
public class KillDataComparator implements Comparator<PlayerKill> {

    @Override
    public int compare(PlayerKill o1, PlayerKill o2) {
        return Long.compare(o1.getTime(), o2.getTime());
    }

}
