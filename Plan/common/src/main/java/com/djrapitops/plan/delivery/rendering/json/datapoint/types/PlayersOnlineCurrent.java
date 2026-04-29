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
package com.djrapitops.plan.delivery.rendering.json.datapoint.types;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Datapoint for looking up current Players online count.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayersOnlineCurrent implements Datapoint<Integer> {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ServerSensor<?> serverSensor;

    @Inject
    public PlayersOnlineCurrent(DBSystem dbSystem, ServerInfo serverInfo, ServerSensor<?> serverSensor) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.serverSensor = serverSensor;
    }

    @Override
    public Optional<Integer> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("PLAYERS_ONLINE does not support player parameter");
        }

        List<ServerUUID> serverUUIDs = filter.getServerUUIDs();
        if (serverUUIDs.isEmpty()) {
            if (serverSensor.usingRedisBungee()) {
                return Optional.of(serverSensor.getOnlinePlayerCount());
            } else {
                serverUUIDs = dbSystem.getDatabase().query(ServerQueries.fetchProxyServerUUIDs());
            }
        }

        if (serverUUIDs.size() == 1) {
            ServerUUID serverUUID = serverUUIDs.get(0);
            if (serverUUID.equals(serverInfo.getServerUUID())) {
                return Optional.of(serverSensor.getOnlinePlayerCount());
            }
            return dbSystem.getDatabase().query(TPSQueries.fetchLatestTPSEntryForServer(serverUUID))
                    .filter(tps -> tps.getDate() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2))
                    .map(TPS::getPlayers);
        }

        // Multiple servers: sum of latest online counts
        int total = 0;
        boolean addedValues = false;
        for (ServerUUID serverUUID : serverUUIDs) {
            if (serverUUID.equals(serverInfo.getServerUUID())) {
                total += serverSensor.getOnlinePlayerCount();
                addedValues = true;
            } else {
                Optional<Integer> found = dbSystem.getDatabase().query(TPSQueries.fetchLatestTPSEntryForServer(serverUUID))
                        .filter(tps -> tps.getDate() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2))
                        .map(TPS::getPlayers);
                total += found.orElse(0);
                if (!addedValues) addedValues = found.isPresent();
            }
        }
        return addedValues ? Optional.of(total) : Optional.empty();
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_PLAYERS_ONLINE_CURRENT;
        } else {
            return WebPermission.DATA_NETWORK_PLAYERS_ONLINE_CURRENT;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.PLAYERS_ONLINE;
    }
}
