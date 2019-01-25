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
package com.djrapitops.plan.db.sql.queries.containers;

import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.data.store.containers.ServerContainer;
import com.djrapitops.plan.data.store.keys.NetworkKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.sql.queries.LargeFetchQueries;
import com.djrapitops.plan.db.sql.queries.OptionalFetchQueries;
import com.djrapitops.plan.system.info.server.Server;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Used to get a NetworkContainer, some limitations apply to values returned by DataContainer keys.
 * <p>
 * Limitations:
 * - Bungee ServerContainer does not support: ServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_DEATHS, PLAYER_KILL_COUNT
 * - Bungee ServerContainer ServerKeys.TPS only contains playersOnline values
 * - NetworkKeys.PLAYERS PlayerContainers:
 * - do not support: PlayerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_DEATHS, PLAYER_KILL_COUNT
 * - PlayerKeys.PER_SERVER does not support: PerServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_DEATHS, PLAYER_KILL_COUNT
 * <p>
 * Blocking methods are not called until DataContainer getter methods are called.
 *
 * @author Rsl1122
 */
public class NetworkContainerQuery implements Query<NetworkContainer> {

    private static Query<ServerContainer> getBungeeServerContainer() {
        return db -> {
            Optional<Server> proxyInformation = db.query(OptionalFetchQueries.proxyServerInformation());
            if (!proxyInformation.isPresent()) {
                return new ServerContainer();
            }

            UUID serverUUID = proxyInformation.get().getUuid();
            ServerContainer container = db.query(ContainerFetchQueries.fetchServerContainer(serverUUID));
            container.putCachingSupplier(ServerKeys.PLAYERS, () -> db.query(ContainerFetchQueries.fetchAllPlayerContainers()));
            container.putCachingSupplier(ServerKeys.TPS, db.getTpsTable()::getNetworkOnlineData);
            container.putSupplier(ServerKeys.WORLD_TIMES, null); // Additional Session information not supported
            container.putSupplier(ServerKeys.PLAYER_KILLS, null);
            container.putSupplier(ServerKeys.PLAYER_KILL_COUNT, null);

            return container;
        };
    }

    @Override
    public NetworkContainer executeQuery(SQLDB db) {
        ServerContainer bungeeContainer = db.query(getBungeeServerContainer());
        NetworkContainer networkContainer = db.getNetworkContainerFactory().forBungeeContainer(bungeeContainer);
        networkContainer.putCachingSupplier(NetworkKeys.BUKKIT_SERVERS, () ->
                db.query(LargeFetchQueries.fetchPlanServerInformation()).values()
                        .stream().filter(Server::isNotProxy).collect(Collectors.toSet())
        );
        return networkContainer;
    }
}