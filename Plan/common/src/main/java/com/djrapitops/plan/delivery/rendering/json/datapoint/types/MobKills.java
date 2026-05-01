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
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.MobKillCounter;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Datapoint for mob kill counts.
 */
@Singleton
public class MobKills implements Datapoint<Long> {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public MobKills(DBSystem dbSystem, ServerInfo serverInfo) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.MOB_KILLS;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_MOB_KILLS;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_MOB_KILLS;
        } else {
            return WebPermission.DATA_NETWORK_MOB_KILLS;
        }
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        Optional<UUID> playerUUID = filter.getPlayerUUID();
        long after = filter.getAfter();
        long before = filter.getBefore();

        if (playerUUID.isPresent()) {
            Long count = db.query(KillQueries.mobKillCount(after, before, playerUUID.get(), filter.getServerUUIDs()));
            if (filter.contains(serverInfo.getServerUUID())) {
                count += SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(after, before))
                        .flatMap(session -> session.getExtraData(MobKillCounter.class))
                        .map(MobKillCounter::getCount)
                        .orElse(0);
            }
            return Optional.of(count);
        }

        Long count = db.query(KillQueries.mobKillCount(after, before, filter.getServerUUIDs()));
        if (filter.contains(serverInfo.getServerUUID())) {
            count += SessionCache.getActiveSessions().stream()
                    .filter(session -> session.isWithin(after, before))
                    .flatMap(session -> session.getExtraData(MobKillCounter.class).stream())
                    .mapToLong(MobKillCounter::getCount)
                    .sum();
        }
        return Optional.of(count);
    }
}
