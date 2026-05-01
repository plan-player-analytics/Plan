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
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.analysis.MultiServerActivityIndexQueries;
import com.djrapitops.plan.storage.database.queries.analysis.NetworkActivityIndexQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Datapoint for regular player count.
 *
 * @author AuroraLS3
 */
@Singleton
public class RegularPlayers implements Datapoint<Integer> {

    private final DBSystem dbSystem;
    private final PlanConfig config;

    @Inject
    public RegularPlayers(DBSystem dbSystem, PlanConfig config) {
        this.dbSystem = dbSystem;
        this.config = config;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.noPlayer();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.REGULAR_PLAYERS;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_REGULAR_PLAYERS;
        } else {
            return WebPermission.DATA_NETWORK_REGULAR_PLAYERS;
        }
    }

    @Override
    public Optional<Integer> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("REGULAR_PLAYERS does not support player parameter");
        }

        long before = filter.getBefore();
        long now = System.currentTimeMillis();
        long calculationDate = Math.min(before, now);
        long playtimeThreshold = config.get(TimeSettings.ACTIVE_PLAY_THRESHOLD);

        if (!filter.getServerUUIDs().isEmpty()) {
            return Optional.of(dbSystem.getDatabase().query(MultiServerActivityIndexQueries.fetchActivityGroupCount(
                    calculationDate, filter.getServerUUIDs(), playtimeThreshold, ActivityIndex.REGULAR, 5.1)
            ));
        }

        return Optional.of(dbSystem.getDatabase().query(NetworkActivityIndexQueries.fetchRegularPlayerCount(
                calculationDate, playtimeThreshold)
        ));
    }
}
