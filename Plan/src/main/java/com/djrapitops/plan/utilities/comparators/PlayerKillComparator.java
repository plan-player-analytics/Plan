package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.container.PlayerKill;

import java.util.Comparator;

/**
 * @author Fuzzlemann
 */
public class PlayerKillComparator implements Comparator<PlayerKill> {

    @Override
    public int compare(PlayerKill o1, PlayerKill o2) {
        return Long.compare(o1.getTime(), o2.getTime());
    }

}
