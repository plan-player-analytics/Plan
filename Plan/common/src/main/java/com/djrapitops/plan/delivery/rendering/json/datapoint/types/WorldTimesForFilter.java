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

import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * @author AuroraLS3
 */
@Singleton
public class WorldTimesForFilter {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public WorldTimesForFilter(DBSystem dbSystem, ServerInfo serverInfo) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    public WorldTimes getWorldTimes(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        Optional<UUID> filterPlayerUUID = filter.getPlayerUUID();
        if (filterPlayerUUID.isPresent()) {
            UUID playerUUID = filterPlayerUUID.get();
            WorldTimes worldTimes = db.query(WorldTimesQueries.fetchPlayerTotalWorldTimes(filter.getAfter(), filter.getBefore(), playerUUID, filter.getServerUUIDs()));
            if (filter.contains(serverInfo.getServerUUID())) {
                SessionCache.getCachedSession(playerUUID)
                        .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()))
                        .map(session -> session.getExtraData(WorldTimes.class).orElseGet(WorldTimes::new))
                        .ifPresent(worldTimes::add);
            }
            return worldTimes;
        } else {
            WorldTimes worldTimes = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
            if (filter.contains(serverInfo.getServerUUID())) {
                SessionCache.getActiveSessions().stream()
                        .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()))
                        .map(session -> session.getExtraData(WorldTimes.class).orElseGet(WorldTimes::new))
                        .forEach(worldTimes::add);
            }
            return worldTimes;
        }
    }
}
