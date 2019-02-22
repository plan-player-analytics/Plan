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
package com.djrapitops.plan.db.access.queries.containers;

import com.djrapitops.plan.data.container.BaseUser;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.PerServerMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.objects.*;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Used to get a PlayerContainer of a specific player.
 * <p>
 * Blocking methods are not called until DataContainer getter methods are called.
 *
 * @author Rsl1122
 */
public class PlayerContainerQuery implements Query<PlayerContainer> {

    private final UUID uuid;

    public PlayerContainerQuery(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public PlayerContainer executeQuery(SQLDB db) {
        PlayerContainer container = new PlayerContainer();
        container.putRawData(PlayerKeys.UUID, uuid);

        Key<BaseUser> baseUserKey = new Key<>(BaseUser.class, "BASE_USER");
        container.putSupplier(baseUserKey, () -> db.query(BaseUserQueries.fetchBaseUserOfPlayer(uuid)).orElse(null));
        container.putSupplier(PlayerKeys.REGISTERED, () -> container.getValue(baseUserKey).map(BaseUser::getRegistered).orElse(null));
        container.putSupplier(PlayerKeys.NAME, () -> container.getValue(baseUserKey).map(BaseUser::getName).orElse(null));
        container.putSupplier(PlayerKeys.KICK_COUNT, () -> container.getValue(baseUserKey).map(BaseUser::getTimesKicked).orElse(null));

        container.putCachingSupplier(PlayerKeys.GEO_INFO, () -> db.query(GeoInfoQueries.fetchPlayerGeoInformation(uuid)));
        container.putCachingSupplier(PlayerKeys.PING, () -> db.query(PingQueries.fetchPingDataOfPlayer(uuid)));
        container.putCachingSupplier(PlayerKeys.NICKNAMES, () -> db.query(NicknameQueries.fetchNicknameDataOfPlayer(uuid)));
        container.putCachingSupplier(PlayerKeys.PER_SERVER, () -> db.query(new PerServerContainerQuery(uuid)));

        container.putSupplier(PlayerKeys.BANNED, () -> new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).isBanned());
        container.putSupplier(PlayerKeys.OPERATOR, () -> new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).isOperator());

        container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                    List<Session> sessions = new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapSessions();
                    container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(sessions::add);
                    return sessions;
                }
        );
        container.putCachingSupplier(PlayerKeys.WORLD_TIMES, () ->
        {
            WorldTimes worldTimes = db.query(WorldTimesQueries.fetchPlayerTotalWorldTimes(uuid));
            container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(session -> worldTimes.add(
                    session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>())))
            );
            return worldTimes;
        });

        container.putSupplier(PlayerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

        container.putSupplier(PlayerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
        container.putSupplier(PlayerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(container).toPlayerDeathList());
        container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PlayerKeys.PLAYER_KILLS).size());
        container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
        container.putSupplier(PlayerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

        return container;
    }
}