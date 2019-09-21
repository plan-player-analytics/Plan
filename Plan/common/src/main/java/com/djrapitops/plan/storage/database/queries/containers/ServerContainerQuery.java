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

import com.djrapitops.plan.delivery.domain.container.ServerContainer;
import com.djrapitops.plan.delivery.domain.keys.ServerKeys;
import com.djrapitops.plan.delivery.domain.mutators.PlayersMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Used to get a ServerContainer, some limitations apply to values returned by DataContainer keys.
 * <p>
 * Limitations:
 * - ServerKeys.PLAYERS PlayerContainers PlayerKeys.PER_SERVER only contains information about the queried server.
 * <p>
 * Blocking methods are not called until DataContainer getter methods are called.
 *
 * @author Rsl1122
 */
public class ServerContainerQuery implements Query<ServerContainer> {

    private final UUID serverUUID;

    public ServerContainerQuery(UUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public ServerContainer executeQuery(SQLDB db) {
        ServerContainer container = new ServerContainer();

        Optional<Server> serverInfo = db.query(ServerQueries.fetchServerMatchingIdentifier(serverUUID));
        if (!serverInfo.isPresent()) {
            return container;
        }

        container.putRawData(ServerKeys.SERVER_UUID, serverUUID);
        container.putRawData(ServerKeys.NAME, serverInfo.get().getName());
        container.putCachingSupplier(ServerKeys.PLAYERS, () -> db.query(new ServerPlayerContainersQuery(serverUUID)));
        container.putSupplier(ServerKeys.PLAYER_COUNT, () -> container.getValue(ServerKeys.PLAYERS).map(Collection::size).orElse(0));

        container.putCachingSupplier(ServerKeys.TPS, () -> db.query(TPSQueries.fetchTPSDataOfServer(serverUUID)));
        container.putCachingSupplier(ServerKeys.PING, () -> PlayersMutator.forContainer(container).pings());
        container.putCachingSupplier(ServerKeys.ALL_TIME_PEAK_PLAYERS, () ->
                db.query(TPSQueries.fetchAllTimePeakPlayerCount(serverUUID)).orElse(null)
        );
        container.putCachingSupplier(ServerKeys.RECENT_PEAK_PLAYERS, () -> {
            long twoDaysAgo = System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(2L));
            return db.query(TPSQueries.fetchPeakPlayerCount(serverUUID, twoDaysAgo)).orElse(null);
        });

        container.putCachingSupplier(ServerKeys.WORLD_TIMES, () -> db.query(WorldTimesQueries.fetchServerTotalWorldTimes(serverUUID)));

        // Calculating getters
        container.putCachingSupplier(ServerKeys.OPERATORS, () -> PlayersMutator.forContainer(container).operators());
        container.putCachingSupplier(ServerKeys.SESSIONS, () -> {
            List<Session> sessions = PlayersMutator.forContainer(container).getSessions();
            if (serverUUID.equals(serverInfo.get().getUuid())) {
                sessions.addAll(SessionCache.getActiveSessions().values());
            }
            return sessions;
        });
        container.putCachingSupplier(ServerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
        container.putCachingSupplier(ServerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(ServerKeys.PLAYER_KILLS).size());
        container.putCachingSupplier(ServerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
        container.putCachingSupplier(ServerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

        container.putCachingSupplier(ServerKeys.EXTENSION_DATA, () -> db.query(new ExtensionServerDataQuery(serverUUID)));

        return container;
    }
}