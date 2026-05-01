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
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerCountQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * Datapoint for unique player count.
 * <p>
 * Unique player count is computed by checking if a player has a session within selected timeframe,
 * only counting them once even if they have multiple within the timeframe.
 *
 * @author AuroraLS3
 */
@Singleton
public class UniquePlayersCount implements Datapoint<Integer> {

    private final DBSystem dbSystem;

    @Inject
    public UniquePlayersCount(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.noPlayer();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.UNIQUE_PLAYERS_COUNT;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_UNIQUE_PLAYERS_COUNT;
        } else {
            return WebPermission.DATA_NETWORK_UNIQUE_PLAYERS_COUNT;
        }
    }

    @Override
    public Optional<Integer> getValue(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            throw new BadRequestException("UNIQUE_PLAYERS_COUNT does not support player parameter");
        }

        if (!filter.getServerUUIDs().isEmpty()) {
            return Optional.of(dbSystem.getDatabase().query(PlayerCountQueries.uniquePlayerCount(
                    filter.getAfter(), filter.getBefore(), filter.getServerUUIDs())
            ));
        }

        return Optional.of(dbSystem.getDatabase().query(PlayerCountQueries.uniquePlayerCount(
                filter.getAfter(), filter.getBefore())
        ));
    }
}
