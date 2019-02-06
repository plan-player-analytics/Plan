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
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.containers.SupplierDataContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.PerServerMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.LargeFetchQueries;

import java.util.*;

/**
 * Used to get PlayerContainers of all players on the network, some limitations apply to DataContainer keys.
 * <p>
 * Limitations:
 * - PlayerContainers do not support: PlayerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_KILL_COUNT
 * - PlayerContainers PlayerKeys.PER_SERVER does not support: PerServerKeys WORLD_TIMES, PLAYER_KILLS, PLAYER_KILL_COUNT
 * <p>
 * Blocking methods are not called until DataContainer getter methods are called.
 *
 * @author Rsl1122
 */
public class AllPlayerContainersQuery implements Query<List<PlayerContainer>> {

    static Map<UUID, PerServerContainer> getPerServerData(
            Map<UUID, Map<UUID, List<Session>>> sessions,
            Map<UUID, List<UserInfo>> allUserInfo,
            Map<UUID, List<Ping>> allPings
    ) {
        Map<UUID, PerServerContainer> perServerContainers = new HashMap<>();

        for (Map.Entry<UUID, List<UserInfo>> entry : allUserInfo.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<UserInfo> serverUserInfo = entry.getValue();

            for (UserInfo userInfo : serverUserInfo) {
                UUID uuid = userInfo.getPlayerUuid();
                if (uuid == null) {
                    continue;
                }
                PerServerContainer perServerContainer = perServerContainers.getOrDefault(uuid, new PerServerContainer());
                DataContainer container = perServerContainer.getOrDefault(serverUUID, new SupplierDataContainer());
                container.putRawData(PlayerKeys.REGISTERED, userInfo.getRegistered());
                container.putRawData(PlayerKeys.BANNED, userInfo.isBanned());
                container.putRawData(PlayerKeys.OPERATOR, userInfo.isOperator());
                perServerContainer.put(serverUUID, container);
                perServerContainers.put(uuid, perServerContainer);
            }
        }

        for (Map.Entry<UUID, Map<UUID, List<Session>>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            Map<UUID, List<Session>> serverUserSessions = entry.getValue();

            for (Map.Entry<UUID, List<Session>> sessionEntry : serverUserSessions.entrySet()) {
                UUID uuid = sessionEntry.getKey();
                PerServerContainer perServerContainer = perServerContainers.getOrDefault(uuid, new PerServerContainer());
                DataContainer container = perServerContainer.getOrDefault(serverUUID, new SupplierDataContainer());

                List<Session> serverSessions = sessionEntry.getValue();
                container.putRawData(PerServerKeys.SESSIONS, serverSessions);

                container.putSupplier(PerServerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

                container.putSupplier(PerServerKeys.WORLD_TIMES, () -> SessionsMutator.forContainer(container).toTotalWorldTimes());
                container.putSupplier(PerServerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(container).toPlayerDeathList());
                container.putSupplier(PerServerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
                container.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PerServerKeys.PLAYER_KILLS).size());
                container.putSupplier(PerServerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
                container.putSupplier(PerServerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());
                container.putSupplier(PerServerKeys.MOB_DEATH_COUNT, () ->
                        container.getUnsafe(PerServerKeys.DEATH_COUNT) - container.getUnsafe(PerServerKeys.PLAYER_DEATH_COUNT)
                );
                perServerContainer.put(serverUUID, container);
                perServerContainers.put(uuid, perServerContainer);
            }
        }

        for (Map.Entry<UUID, List<Ping>> entry : allPings.entrySet()) {
            UUID uuid = entry.getKey();
            for (Ping ping : entry.getValue()) {
                UUID serverUUID = ping.getServerUUID();
                PerServerContainer perServerContainer = perServerContainers.getOrDefault(uuid, new PerServerContainer());
                DataContainer container = perServerContainer.getOrDefault(serverUUID, new SupplierDataContainer());

                if (!container.supports(PerServerKeys.PING)) {
                    container.putRawData(PerServerKeys.PING, new ArrayList<>());
                }
                container.getUnsafe(PerServerKeys.PING).add(ping);

                perServerContainer.put(serverUUID, container);
                perServerContainers.put(uuid, perServerContainer);
            }
        }

        return perServerContainers;
    }

    @Override
    public List<PlayerContainer> executeQuery(SQLDB db) {
        List<PlayerContainer> containers = new ArrayList<>();

        Collection<BaseUser> users = db.query(LargeFetchQueries.fetchAllCommonUserInformation());
        Map<UUID, List<GeoInfo>> geoInfo = db.query(LargeFetchQueries.fetchAllGeoInformation());
        Map<UUID, List<Ping>> allPings = db.query(LargeFetchQueries.fetchAllPingData());
        Map<UUID, List<Nickname>> allNicknames = db.query(LargeFetchQueries.fetchAllNicknameDataByPlayerUUIDs());

        Map<UUID, Map<UUID, List<Session>>> sessions = db.query(LargeFetchQueries.fetchAllSessionsWithoutKillOrWorldData());
        Map<UUID, List<UserInfo>> allUserInfo = db.query(LargeFetchQueries.fetchPerServerUserInformation());
        Map<UUID, PerServerContainer> perServerInfo = getPerServerData(sessions, allUserInfo, allPings);

        for (BaseUser baseUser : users) {
            PlayerContainer container = new PlayerContainer();
            UUID uuid = baseUser.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);

            container.putRawData(PlayerKeys.REGISTERED, baseUser.getRegistered());
            container.putRawData(PlayerKeys.NAME, baseUser.getName());
            container.putRawData(PlayerKeys.KICK_COUNT, baseUser.getTimesKicked());
            container.putRawData(PlayerKeys.GEO_INFO, geoInfo.get(uuid));
            container.putRawData(PlayerKeys.PING, allPings.get(uuid));
            container.putRawData(PlayerKeys.NICKNAMES, allNicknames.get(uuid));
            container.putRawData(PlayerKeys.PER_SERVER, perServerInfo.get(uuid));

            container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                        List<Session> playerSessions = PerServerMutator.forContainer(container).flatMapSessions();
                        container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            // Calculating getters
            container.putSupplier(PlayerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PlayerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PlayerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

            containers.add(container);
        }
        return containers;
    }
}