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
package com.djrapitops.plan.delivery.rendering.json.datapoint.types.performance;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.TPSQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Datapoint for looking up Minimum Free Disk within the timeframe.
 *
 * @author AuroraLS3
 */
@Singleton
public class DiskMin implements Datapoint<Long> {

    private final DBSystem dbSystem;

    @Inject
    public DiskMin(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.onlyServer();
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        long min = dbSystem.getDatabase().query(TPSQueries.minFreeDisk(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
        return min != -1L ? Optional.of(min) : Optional.empty();
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_DISK_MIN;
        } else {
            return WebPermission.DATA_NETWORK;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.DISK_MIN;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.BYTES;
    }
}
