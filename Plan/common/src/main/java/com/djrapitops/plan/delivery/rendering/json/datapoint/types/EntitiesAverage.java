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
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Datapoint for looking up Average ENTITIES usage within the timeframe.
 *
 * @author AuroraLS3
 */
@Singleton
public class EntitiesAverage implements Datapoint<Long> {

    private final DBSystem dbSystem;

    @Inject
    public EntitiesAverage(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("ENTITIES_AVERAGE does not support player parameter");
        }

        if (filter.getServerUUIDs().isEmpty()) {
            throw new BadRequestException("ENTITIES_AVERAGE is only available for servers");
        }

        long average = dbSystem.getDatabase().query(TPSQueries.averageEntities(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
        return average != -1L ? Optional.of(average) : Optional.empty();
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_ENTITIES_AVERAGE;
        } else {
            return WebPermission.DATA_NETWORK;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.ENTITIES_AVERAGE;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.NONE;
    }
}
