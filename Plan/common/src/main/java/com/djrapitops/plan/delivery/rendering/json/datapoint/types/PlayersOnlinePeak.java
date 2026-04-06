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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;
import org.jspecify.annotations.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Datapoint for looking up maximum Player online count within the timeframe.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlayersOnlinePeak implements Datapoint<DateObj<Long>> {

    private final DBSystem dbSystem;
    private final ServerSensor<?> serverSensor;

    @Inject
    public PlayersOnlinePeak(DBSystem dbSystem, ServerSensor<?> serverSensor) {
        this.dbSystem = dbSystem;
        this.serverSensor = serverSensor;
    }

    @Override
    public Optional<DateObj<Long>> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("PLAYERS_ONLINE_PEAK does not support player parameter");
        }

        return Optional.of(getPeak(filter).orElse(new DateObj<>(0L, -1L)));
    }

    private @NonNull Optional<DateObj<Long>> getPeak(GenericFilter filter) {
        List<ServerUUID> serverUUIDs = filter.getServerUUIDs();
        if (serverUUIDs.isEmpty()) {
            return dbSystem.getDatabase().query(TPSQueries.fetchNetworkPeakPlayerCount(filter.getAfter(), filter.getBefore(), serverSensor.usingRedisBungee()))
                    .map(peak -> new DateObj<>(peak.getDate(), peak.getValue().longValue()));
        }

        return dbSystem.getDatabase().query(TPSQueries.fetchPeakPlayerCount(serverUUIDs, filter.getAfter(), filter.getBefore()))
                .map(peak -> new DateObj<>(peak.getDate(), peak.getValue().longValue()));
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_PLAYERS_ONLINE_PEAK;
        } else {
            return WebPermission.DATA_NETWORK_PLAYERS_ONLINE_PEAK;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.PLAYERS_ONLINE_PEAK;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.SPECIAL;
    }
}
