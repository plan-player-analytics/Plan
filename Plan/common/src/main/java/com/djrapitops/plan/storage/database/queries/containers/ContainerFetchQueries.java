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
package com.djrapitops.plan.storage.database.queries.containers;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.storage.database.queries.Query;

import java.util.UUID;

/**
 * Static method class for queries that return some kind of {@link DataContainer}.
 *
 * @author Rsl1122
 */
public class ContainerFetchQueries {

    private ContainerFetchQueries() {
        /* Static method class */
    }

    /**
     * Used to get a PlayerContainer of a specific player.
     * <p>
     * Blocking methods are not called until DataContainer getter methods are called.
     *
     * @param playerUUID UUID of the player.
     * @return a new PlayerContainer.
     * @see PlayerContainerQuery
     */
    public static Query<PlayerContainer> fetchPlayerContainer(UUID playerUUID) {
        return new PlayerContainerQuery(playerUUID);
    }

}