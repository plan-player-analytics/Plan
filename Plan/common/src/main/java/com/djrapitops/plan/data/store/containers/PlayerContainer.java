/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.data.store.containers;

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
 * @see com.djrapitops.plan.data.store.keys.PlayerKeys For Key objects.
 */
public class PlayerContainer extends DataContainer {

    private Map<Long, ActivityIndex> activityIndexCache;

    public PlayerContainer() {
        activityIndexCache = new HashMap<>();
    }

    public ActivityIndex getActivityIndex(long date, int minuteThreshold, int loginThreshold) {
        return activityIndexCache.computeIfAbsent(date, time -> new ActivityIndex(this, time, minuteThreshold, loginThreshold));
    }

    public boolean playedBetween(long after, long before) {
        return SessionsMutator.forContainer(this).playedBetween(after, before);
    }

}