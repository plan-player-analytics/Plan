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
package com.djrapitops.plan.api.data;

import com.djrapitops.plan.delivery.domain.keys.Key;

import java.util.Optional;

/**
 * Wrapper for a PlayerContainer.
 * <p>
 * The actual object is wrapped to avoid exposing too much API that might change.
 * See {@link com.djrapitops.plan.delivery.domain.keys.PlayerKeys} for Key objects.
 * <p>
 * The Keys might change in the future, but the Optional API should help dealing with those cases.
 *
 * @author AuroraLS3
 * @deprecated Plan API v4 has been deprecated, use the APIv5 instead (<a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5">wiki</a>).
 */
@Deprecated(since = "5.0")
public class PlayerContainer {

    private final com.djrapitops.plan.delivery.domain.container.PlayerContainer container;

    public PlayerContainer(com.djrapitops.plan.delivery.domain.container.PlayerContainer container) {
        this.container = container;
    }

    /**
     * @deprecated loginThreshold no longer used for activity index.
     */
    @Deprecated
    public double getActivityIndex(long date, long playtimeMsThreshold, int loginThreshold) {
        return getActivityIndex(date, playtimeMsThreshold);
    }

    public double getActivityIndex(long date, long playtimeMsThreshold) {
        return container.getActivityIndex(date, playtimeMsThreshold).getValue();
    }

    public boolean playedBetween(long after, long before) {
        return container.playedBetween(after, before);
    }

    public <T> Optional<T> getValue(Key<T> key) {
        return container.getValue(key);
    }
}
