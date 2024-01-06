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
 * Wrapper for a ServerContainer.
 * <p>
 * The actual object is wrapped to avoid exposing too much API that might change.
 * <p>
 * The Keys might change in the future, but the Optional API should help dealing with those cases.
 *
 * @author AuroraLS3
 * @deprecated Plan API v4 has been deprecated, use the APIv5 instead (<a href="https://github.com/plan-player-analytics/Plan/wiki/APIv5">wiki</a>).
 */
@Deprecated(forRemoval = true, since = "5.0")
public class ServerContainer {

    public ServerContainer() {/*Empty constructor, no-op derpecated api class*/}

    public <T> Optional<T> getValue(Key<T> key) {
        return Optional.empty();
    }
}
