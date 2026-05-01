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
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Playtime per day average datapoint.
 * <p>
 * Playtime is grouped in buckets by date based on timeszone defined in config, and then averaged.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlaytimePerDayAverage implements Datapoint<Long> {

    private final PlanConfig config;
    private final DBSystem dbSystem;

    @Inject
    public PlaytimePerDayAverage(PlanConfig config, DBSystem dbSystem) {
        this.config = config;
        this.dbSystem = dbSystem;
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        Optional<UUID> player = filter.getPlayerUUID();
        if (player.isPresent()) {
            return Optional.of(dbSystem.getDatabase().query(SessionQueries.averagePlaytimePerDay(
                    filter.getAfter(), filter.getBefore(), config.getTimeZone().getOffset(filter.getBefore()), player.get(), filter.getServerUUIDs())
            ));
        }

        if (!filter.getServerUUIDs().isEmpty()) {
            return Optional.of(dbSystem.getDatabase().query(SessionQueries.averagePlaytimePerDay(
                    filter.getAfter(), filter.getBefore(), config.getTimeZone().getOffset(filter.getBefore()), filter.getServerUUIDs())
            ));
        }

        return Optional.of(dbSystem.getDatabase().query(SessionQueries.averagePlaytimePerDay(
                filter.getAfter(), filter.getBefore(), config.getTimeZone().getOffset(filter.getBefore()))
        ));
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.TIME_AMOUNT;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_PLAYTIME_PER_DAY_AVERAGE;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_PLAYTIME_PER_DAY_AVERAGE;
        } else {
            return WebPermission.DATA_NETWORK_PLAYTIME_PER_DAY_AVERAGE;
        }
    }

    @Override
    public DatapointType getType() {
        return DatapointType.PLAYTIME_PER_DAY_AVERAGE;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }
}
