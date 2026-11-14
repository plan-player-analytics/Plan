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

import com.djrapitops.plan.delivery.domain.TopList;
import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.delivery.rendering.json.datapoint.Datapoint;
import com.djrapitops.plan.delivery.rendering.json.datapoint.DatapointType;
import com.djrapitops.plan.delivery.rendering.json.datapoint.SupportedFilters;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Datapoint for top weapons used in kills.
 */
@Singleton
public class TopWeapons implements Datapoint<TopList> {

    private static final int LIMIT = 3;

    private final DBSystem dbSystem;

    @Inject
    public TopWeapons(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.TOP_WEAPONS;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_TOP_WEAPONS;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_TOP_WEAPONS;
        } else {
            return WebPermission.DATA_NETWORK_TOP_WEAPONS;
        }
    }

    @Override
    public Optional<TopList> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        long after = filter.getAfter();
        long before = filter.getBefore();
        Optional<UUID> playerUUID = filter.getPlayerUUID();

        int limit = filter.getAdditionalParameter("limit", Integer::parseInt)
                .orElse(LIMIT);

        List<String> weapons;
        if (playerUUID.isPresent()) {
            weapons = db.query(KillQueries.topWeaponsOfPlayer(after, before, playerUUID.get(), limit));
        } else {
            weapons = db.query(KillQueries.topWeaponsOfServers(after, before, filter.getServerUUIDs(), limit));
        }
        return Optional.of(new TopList(weapons));
    }
}
