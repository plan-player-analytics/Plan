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

import com.djrapitops.plan.delivery.domain.OutOfCategory;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.gathering.domain.WorldTimes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
@Singleton
public class MostActiveWorld implements Datapoint<OutOfCategory> {

    private final WorldTimesForFilter worldTimesForFilter;

    @Inject
    public MostActiveWorld(WorldTimesForFilter worldTimesForFilter) {
        this.worldTimesForFilter = worldTimesForFilter;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.SPECIAL;
    }

    @Override
    public DatapointType getType() {
        return DatapointType.MOST_ACTIVE_WORLD;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_MOST_ACTIVE_WORLD;
        }
        if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_MOST_ACTIVE_WORLD;
        }
        return WebPermission.DATA_NETWORK_MOST_ACTIVE_WORLD;
    }

    @Override
    public Optional<OutOfCategory> getValue(GenericFilter filter) {
        WorldTimes worldTimes = worldTimesForFilter.getWorldTimes(filter);
        return Optional.of(new OutOfCategory(worldTimes.computeWorldTimes(), worldTimes.getTotal()));
    }
}
