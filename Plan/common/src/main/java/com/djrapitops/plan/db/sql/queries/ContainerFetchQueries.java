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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.sql.queries.containers.NetworkContainerQuery;
import com.djrapitops.plan.db.sql.queries.containers.ServerContainerQuery;

import java.util.UUID;

/**
 * Static method class for queries that return some kind of {@link com.djrapitops.plan.data.store.containers.DataContainer}.
 *
 * @author Rsl1122
 */
public class ContainerFetchQueries {

    private ContainerFetchQueries() {
        /* Static method class */
    }

    /**
     * Used to get a NetworkContainer, some limitations apply to values returned by DataContainer keys.
     *
     * @return a new NetworkContainer.
     * @see com.djrapitops.plan.db.sql.queries.containers.NetworkContainerQuery
     */
    public static Query<NetworkContainer> fetchNetworkContainer() {
        return new NetworkContainerQuery();
    }

    /**
     * Used to get a ServerContainer, some limitations apply to values returned by DataContainer keys.
     * <p>
     *
     * @param serverUUID UUID of the Server.
     * @return a new ServerContainer.
     * @see com.djrapitops.plan.db.sql.queries.containers.ServerContainerQuery
     */
    public static Query<ServerContainer> getServerContainer(UUID serverUUID) {
        return new ServerContainerQuery(serverUUID);
    }

}