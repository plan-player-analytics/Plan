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
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.PieDrilldown;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.PieGraphFactory;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.PieSlice;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data point implementation for the World Pie Graph.
 *
 * @author AuroraLS3
 */
@Singleton
public class WorldPie implements Datapoint<WorldPie.Content> {

    private final DBSystem dbSystem;
    private final PieGraphFactory pieGraphFactory;

    @Inject
    public WorldPie(DBSystem dbSystem, PieGraphFactory pieGraphFactory) {
        this.dbSystem = dbSystem;
        this.pieGraphFactory = pieGraphFactory;
    }

    @Override
    public Optional<Content> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        if (filter.getPlayerUUID().isPresent()) {
            UUID playerUUID = filter.getPlayerUUID().get();
            WorldTimes worldTimes = db.query(WorldTimesQueries.fetchPlayerTotalWorldTimes(filter.getAfter(), filter.getBefore(), playerUUID, filter.getServerUUIDs()));
            SessionCache.getCachedSession(playerUUID)
                    .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()))
                    .map(session -> session.getExtraData(WorldTimes.class).orElseGet(WorldTimes::new))
                    .ifPresent(worldTimes::add);
            return Optional.of(new Content(pieGraphFactory.worldPie(worldTimes)));
        } else {
            WorldTimes worldTimes = db.query(WorldTimesQueries.fetchServerTotalWorldTimes(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
            return Optional.of(new Content(pieGraphFactory.worldPie(worldTimes)));
        }
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.PAGE_PLAYER_SESSIONS;
        }
        if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.PAGE_SERVER_SESSIONS_WORLD_PIE;
        }
        return WebPermission.PAGE_NETWORK_SESSIONS_WORLD_PIE;
    }

    @Override
    public DatapointType getType() {
        return DatapointType.WORLD_PIE;
    }

    public record Content(List<PieSlice> slices, List<PieDrilldown> drilldown) {
        public Content(com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie worldPie) {
            this(worldPie.getSlices(), worldPie.getDrilldown());
        }

    }

}
