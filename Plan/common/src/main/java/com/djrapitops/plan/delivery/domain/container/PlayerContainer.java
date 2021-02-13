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
package com.djrapitops.plan.delivery.domain.container;

import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;

import java.util.HashMap;
import java.util.Map;

/**
 * DataContainer about a Player.
 * <p>
 * Use {@code getValue(PlayerKeys.REGISTERED).isPresent()} to determine if Plan has data about the player.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.delivery.domain.keys.PlayerKeys For Key objects.
 */
public class PlayerContainer extends DynamicDataContainer {

    private final Map<Long, ActivityIndex> activityIndexCache;

    public PlayerContainer() {
        activityIndexCache = new HashMap<>();
    }

    public ActivityIndex getActivityIndex(long date, long playtimeMsThreshold) {
        return activityIndexCache.computeIfAbsent(date, time -> new ActivityIndex(this, time, playtimeMsThreshold));
    }

    public boolean playedBetween(long after, long before) {
        return SessionsMutator.forContainer(this).playedBetween(after, before);
    }

}