package com.djrapitops.plan.data.store.containers;


import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;

import java.util.HashMap;
import java.util.Map;

/**
 * DataContainer about a Player.
 * <p>
 * Use {@code getValue(PlayerKeys.REGISTERED).isPresent()} to determine if Plan has data about the player.
 *
 * @author Rsl1122
 * @see PlayerKeys For Key objects.
 */
public class PlayerContainer extends DataContainer {

    private Map<Long, ActivityIndex> activityIndexCache;

    public PlayerContainer() {
        activityIndexCache = new HashMap<>();
    }

    public ActivityIndex getActivityIndex(long date) {
        ActivityIndex index = activityIndexCache.get(date);
        if (index == null) {
            index = new ActivityIndex(this, date);
            activityIndexCache.put(date, index);
        }
        return index;
    }

    public boolean playedBetween(long after, long before) {
        return SessionsMutator.forContainer(this).playedBetween(after, before);
    }

}