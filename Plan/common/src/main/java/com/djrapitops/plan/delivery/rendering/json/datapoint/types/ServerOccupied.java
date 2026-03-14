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

import com.djrapitops.plan.delivery.domain.OutOf;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
@Singleton
public class ServerOccupied implements Datapoint<OutOf> {

    private final DBSystem dbSystem;

    @Inject
    public ServerOccupied(DBSystem dbSystem) {this.dbSystem = dbSystem;}

    @Override
    public Optional<OutOf> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("SERVER_OCCUPIED does not support player parameter");
        }

        Map<Integer, Long> occupied = dbSystem.getDatabase().query(TPSQueries.occupiedTime(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
        Map<Integer, Long> uptime = dbSystem.getDatabase().query(TPSQueries.uptime(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));

        long occupiedTime = occupied.values().stream().mapToLong(l -> l).sum();
        long uptimeTime = uptime.values().stream().mapToLong(l -> l).sum();
        return Optional.of(new OutOf(occupiedTime, uptimeTime, FormatType.TIME_AMOUNT));
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("SERVER_OCCUPIED does not support player parameter");
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_OCCUPIED_SERVER;
        } else {
            return WebPermission.DATA_SERVER_OCCUPIED_NETWORK;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.SERVER_OCCUPIED;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.SPECIAL;
    }
}
