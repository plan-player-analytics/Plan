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
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Downtime datapoint.
 * <p>
 * Downtime is calculated by checking gaps between TPS data points,
 * if multiple servers are given it is assumed they act as fallback for each other, reducing downtime if one of them is up.
 * <p>
 * Giving no server parameter gives downtime for network, where same fallback rule applies for multi-proxy setups.
 *
 * @author AuroraLS3
 */
@Singleton
public class Downtime implements Datapoint<Long> {

    private final DBSystem dbSystem;

    @Inject
    public Downtime(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.noPlayer();
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("DOWNTIME does not support player parameter");
        }

        Database db = dbSystem.getDatabase();
        List<ServerUUID> serverUUIDs = filter.getServerUUIDs();
        if (filter.getServerUUIDs().isEmpty()) {
            serverUUIDs = db.query(ServerQueries.fetchProxyServerUUIDs());
        }

        return Optional.of(db.query(TPSQueries.downtime(filter.getAfter(), filter.getBefore(), serverUUIDs)));
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_DOWNTIME;
        } else {
            return WebPermission.DATA_NETWORK_DOWNTIME;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.DOWNTIME;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.TIME_AMOUNT;
    }
}
