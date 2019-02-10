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

import com.djrapitops.plan.data.container.*;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.PerServerMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.objects.*;

import java.util.*;

/**
 * Used to get PlayerContainers of all players on a server, some limitations apply to DataContainer keys.
 * <p>
 * Limitations:
 * - PlayerContainers do not support: PlayerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_KILL_COUNT
 * - PlayerContainers PlayerKeys.PER_SERVER does not support: PerServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_KILL_COUNT
 * <p>
 * Blocking methods are not called until DataContainer getter methods are called.
 *
 * @author Rsl1122
 */
public class ServerPlayerContainersQuery implements Query<List<PlayerContainer>> {

    private final UUID serverUUID;

    public ServerPlayerContainersQuery(UUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public List<PlayerContainer> executeQuery(SQLDB db) {
        List<PlayerContainer> containers = new ArrayList<>();

        Collection<BaseUser> baseUsers = db.query(BaseUserQueries.serverBaseUsers(serverUUID));

        Map<UUID, List<GeoInfo>> geoInformation = db.query(GeoInfoQueries.fetchServerGeoInformation(serverUUID));
        Map<UUID, List<Nickname>> nicknames = db.query(NicknameQueries.fetchNicknameDataOfServer(serverUUID));
        Map<UUID, List<Ping>> pingData = db.query(PingQueries.fetchPingDataOfServer(serverUUID));

        // v ------------- Needs work
        Map<UUID, List<Session>> sessions = db.getSessionsTable().getSessionInfoOfServer(serverUUID);
        Map<UUID, Map<UUID, List<Session>>> map = new HashMap<>();
        map.put(serverUUID, sessions);
        db.getKillsTable().addKillsToSessions(map); // TODO Optimize
        db.getWorldTimesTable().addWorldTimesToSessions(map); // TODO Optimize

        Map<UUID, List<UserInfo>> serverUserInfos = Collections.singletonMap(serverUUID, // TODO Optimize
                new ArrayList<>(db.query(UserInfoQueries.fetchUserInformationOfServer(serverUUID)).values())
        );
        Map<UUID, Map<UUID, List<Session>>> serverSessions = Collections.singletonMap(serverUUID, sessions);
        Map<UUID, PerServerContainer> perServerInfo = AllPlayerContainersQuery.getPerServerData(
                serverSessions, serverUserInfos, pingData
        );
        // ^ ------------- Needs work

        for (BaseUser user : baseUsers) {
            PlayerContainer container = new PlayerContainer();

            // BaseUser
            UUID uuid = user.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);
            container.putRawData(PlayerKeys.NAME, user.getName());
            container.putRawData(PlayerKeys.REGISTERED, user.getRegistered());
            container.putRawData(PlayerKeys.KICK_COUNT, user.getTimesKicked());

            // GeoInfo
            container.putRawData(PlayerKeys.GEO_INFO, geoInformation.getOrDefault(uuid, new ArrayList<>()));

            // Ping
            container.putRawData(PlayerKeys.PING, pingData.get(uuid));

            // Nickname, only used for the raw server JSON.
            container.putRawData(PlayerKeys.NICKNAMES, nicknames.get(uuid));

            // v ------------- Needs work
            container.putRawData(PlayerKeys.PER_SERVER, perServerInfo.get(uuid));
            container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                        List<Session> playerSessions = sessions.getOrDefault(uuid, new ArrayList<>());
                        container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            // Calculating getters
            container.putCachingSupplier(PlayerKeys.WORLD_TIMES, () -> {
                WorldTimes worldTimes = new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapWorldTimes();
                container.getValue(PlayerKeys.ACTIVE_SESSION)
                        .ifPresent(session -> worldTimes.add(
                                session.getValue(SessionKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>())))
                        );
                return worldTimes;
            });
            container.putSupplier(PlayerKeys.BANNED, () -> PerServerMutator.forContainer(container).isBanned());
            container.putSupplier(PlayerKeys.OPERATOR, () -> PerServerMutator.forContainer(container).isOperator());

            container.putSupplier(PlayerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PlayerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
            container.putSupplier(PlayerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PlayerKeys.PLAYER_KILLS).size());
            container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PlayerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());
            // ^ ------------- Needs work

            containers.add(container);
        }
        return containers;
    }
}