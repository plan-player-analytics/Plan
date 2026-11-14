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
import com.djrapitops.plan.gathering.domain.DeathCounter;
import com.djrapitops.plan.gathering.domain.PlayerKills;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Datapoint for mob death count (deaths minus player-caused deaths).
 */
@Singleton
public class MobDeaths implements Datapoint<Long> {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public MobDeaths(DBSystem dbSystem, ServerInfo serverInfo) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.MOB_DEATHS;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_MOB_DEATHS;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_MOB_DEATHS;
        } else {
            return WebPermission.DATA_NETWORK_MOB_DEATHS;
        }
    }

    @Override
    public Optional<Long> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        Optional<UUID> playerUUID = filter.getPlayerUUID();
        long after = filter.getAfter();
        long before = filter.getBefore();

        if (playerUUID.isPresent()) {
            long deaths = db.query(KillQueries.deathCount(after, before, playerUUID.get(), filter.getServerUUIDs()));
            long playerDeaths = db.query(KillQueries.playerKillCount(after, before, playerUUID.get(), filter.getServerUUIDs()));

            if (filter.contains(serverInfo.getServerUUID())) {
                long sessionDeaths = SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(after, before))
                        .flatMap(session -> session.getExtraData(DeathCounter.class))
                        .map(DeathCounter::getCount)
                        .orElse(0);
                long sessionPlayerDeaths = SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(after, before))
                        .flatMap(session -> session.getExtraData(PlayerKills.class))
                        .map(pk -> pk.asList().stream()
                                .filter(kill -> kill.date() >= after && kill.date() < before)
                                .count())
                        .orElse(0L);
                deaths += sessionDeaths;
                playerDeaths += sessionPlayerDeaths;
            }
            return Optional.of(deaths - playerDeaths);
        }

        Long value = db.query(KillQueries.mobDeathCount(after, before, filter.getServerUUIDs()));
        if (filter.contains(serverInfo.getServerUUID())) {
            long sessionDeaths = SessionCache.getActiveSessions().stream()
                    .filter(session -> session.isWithin(after, before))
                    .flatMap(session -> session.getExtraData(DeathCounter.class).stream())
                    .mapToLong(DeathCounter::getCount)
                    .sum();
            long sessionPlayerDeaths = SessionCache.getActiveSessions().stream()
                    .filter(session -> session.isWithin(after, before))
                    .flatMap(session -> session.getExtraData(PlayerKills.class).stream())
                    .flatMap(pk -> pk.asList().stream()
                            .filter(kill -> kill.date() >= after && kill.date() < before))
                    .count();
            value += sessionDeaths - sessionPlayerDeaths;
        }
        return Optional.of(value);
    }
}
