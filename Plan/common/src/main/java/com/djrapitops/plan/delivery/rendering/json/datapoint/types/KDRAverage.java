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
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Datapoint for average kill/death ratio (KDR).
 */
@Singleton
public class KDRAverage implements Datapoint<Double> {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;

    @Inject
    public KDRAverage(DBSystem dbSystem, ServerInfo serverInfo) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
    }

    @Override
    public SupportedFilters[] getSupportedFilters() {
        return SupportedFilters.all();
    }

    @Override
    public DatapointType getType() {
        return DatapointType.KDR_AVERAGE;
    }

    @Override
    public WebPermission getPermission(GenericFilter filter) {
        if (filter.getPlayerUUID().isPresent()) {
            return WebPermission.DATA_PLAYER_KDR_AVERAGE;
        } else if (!filter.getServerUUIDs().isEmpty()) {
            return WebPermission.DATA_SERVER_KDR_AVERAGE;
        } else {
            return WebPermission.DATA_NETWORK_KDR_AVERAGE;
        }
    }

    @Override
    public Optional<Double> getValue(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        Optional<UUID> playerUUID = filter.getPlayerUUID();
        long after = filter.getAfter();
        long before = filter.getBefore();

        if (playerUUID.isPresent()) {
            long kills = db.query(KillQueries.playerKillCount(after, before, playerUUID.get(), filter.getServerUUIDs()));
            long deaths = db.query(KillQueries.deathCount(after, before, playerUUID.get(), filter.getServerUUIDs()));

            if (filter.contains(serverInfo.getServerUUID())) {
                long sessionKills = SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(after, before))
                        .flatMap(session -> session.getExtraData(com.djrapitops.plan.gathering.domain.PlayerKills.class))
                        .map(pk -> pk.asList().stream()
                                .filter(kill -> kill.date() >= after && kill.date() < before)
                                .count())
                        .orElse(0L);
                long sessionDeaths = SessionCache.getCachedSession(playerUUID.get())
                        .filter(session -> session.isWithin(after, before))
                        .flatMap(session -> session.getExtraData(DeathCounter.class))
                        .map(DeathCounter::getCount)
                        .orElse(0);
                kills += sessionKills;
                deaths += sessionDeaths;
            }
            return Optional.of(computeKDR(kills, deaths));
        }

        return Optional.of(db.query(KillQueries.averageKDR(after, before, filter.getServerUUIDs())));
    }

    private double computeKDR(long kills, long deaths) {
        return deaths > 0 ? (double) kills / deaths : kills;
    }
}
