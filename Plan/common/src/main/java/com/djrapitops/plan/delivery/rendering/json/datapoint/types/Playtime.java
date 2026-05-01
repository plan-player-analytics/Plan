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
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * @author AuroraLS3
 */
@Singleton
public class Playtime implements Datapoint<Long> {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public Playtime(DBSystem dbSystem, ServerInfo serverInfo) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.PLAYTIME;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.TIME_AMOUNT;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_PLAYTIME;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_PLAYTIME;
        } else {
            return WebPermission.DATA_NETWORK_PLAYTIME;
        }
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        Optional<UUID> playerUUID = filter.getPlayerUUID();
        if (playerUUID.isPresent()) {
            Long playtime = db.query(SessionQueries.playtime(filter.getAfter(), filter.getBefore(), playerUUID.get(), filter.getServerUUIDs()));
            if (filter.contains(serverInfo.getServerUUID())) {
                playtime += SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()))
                        .map(session -> session.toFinishedSession(System.currentTimeMillis()).getLength())
                        .orElse(0L);
            }
            return Optional.of(playtime);
        }

        Long playtime = db.query(SessionQueries.playtime(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
        if (filter.contains(serverInfo.getServerUUID())) {
            playtime += SessionCache.getActiveSessions().stream()
                    .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()))
                    .mapToLong(session -> session.toFinishedSession(System.currentTimeMillis()).getLength())
                    .sum();
        }
        return Optional.of(playtime);
    }
}
