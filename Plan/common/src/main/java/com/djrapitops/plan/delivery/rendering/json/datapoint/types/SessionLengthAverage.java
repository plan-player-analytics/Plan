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
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.PlaytimeAndCount;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Datapoint for average session length.
 *
 * @author AuroraLS3
 */
@Singleton
public class SessionLengthAverage implements Datapoint<Long> {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public SessionLengthAverage(DBSystem dbSystem, ServerInfo serverInfo) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }


    @Override
    public DatapointType getType() {
        return DatapointType.SESSION_LENGTH_AVERAGE;
    }

    @Override
    public FormatType getFormatType() {
        return FormatType.TIME_AMOUNT;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_SESSION_LENGTH_AVERAGE;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_SESSION_LENGTH_AVERAGE;
        } else {
            return WebPermission.DATA_NETWORK_SESSION_LENGTH_AVERAGE;
        }
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        Optional<UUID> playerUUID = filter.getPlayerUUID();

        long playtime;
        long sessionCount;

        long now = System.currentTimeMillis();
        if (playerUUID.isPresent()) {
            PlaytimeAndCount dbResults = db.query(SessionQueries.playtimeAndCount(filter.getAfter(), filter.getBefore(), playerUUID.get(), filter.getServerUUIDs()));
            playtime = dbResults.getPlaytime();
            sessionCount = dbResults.getCount();

            if (filter.contains(serverInfo.getServerUUID())) {
                Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()));
                if (cachedSession.isPresent()) {
                    playtime += now - cachedSession.get().getStart();
                    sessionCount += 1;
                }
            }
        } else {
            PlaytimeAndCount dbResults = db.query(SessionQueries.playtimeAndCount(filter.getAfter(), filter.getBefore(), filter.getServerUUIDs()));
            playtime = dbResults.getPlaytime();
            sessionCount = dbResults.getCount();

            if (filter.contains(serverInfo.getServerUUID())) {
                Stream<ActiveSession> activeSessions = SessionCache.getActiveSessions().stream()
                        .filter(session -> session.isWithin(filter.getAfter(), filter.getBefore()));

                for (ActiveSession session : (Iterable<ActiveSession>) activeSessions::iterator) {
                    playtime += now - session.getStart();
                    sessionCount += 1;
                }
            }
        }

        if (sessionCount == 0) {
            return Optional.of(0L);
        }

        return Optional.of(playtime / sessionCount);
    }
}
