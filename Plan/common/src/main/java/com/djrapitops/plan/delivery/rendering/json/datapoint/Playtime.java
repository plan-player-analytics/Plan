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
package com.djrapitops.plan.delivery.rendering.json.datapoint;

import com.djrapitops.plan.delivery.domain.auth.WebPermission;
import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;

/**
 * @author AuroraLS3
 */
@Singleton
public class Playtime implements Datapoint<Long> {

    private final DBSystem dbSystem;

    @Inject
    public Playtime(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    @Override
    public DatapointType getType() {
        return DatapointType.PLAYTIME;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYTIME_PLAYER;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_PLAYTIME_SERVER;
        } else {
            return WebPermission.DATA_PLAYTIME_NETWORK;
        }
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        if (filter.getPlayerUUID().isPresent()) {
            return filter.getPlayerUUID().map(playerUUID -> db.query(
                            SessionQueries.playtimeOfPlayer(filter.getAfter(), filter.getBefore(), playerUUID)
                    ).entrySet().stream()
                    .filter(entry -> filter.contains(entry.getKey()))
                    .mapToLong(Map.Entry::getValue)
                    .sum());
        }
        if (!filter.getServerUUIDs().isEmpty()) {
            return Optional.of(db.query(SessionQueries.playtime(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs())));
        } else {
            return Optional.of(db.query(SessionQueries.playtime(filter.getAfter(), filter.getBefore())));
        }
    }
}
