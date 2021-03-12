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
package com.djrapitops.plan.storage.database.queries.containers;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.container.PerServerContainer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.PerServerMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.objects.*;

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
 * @author AuroraLS3
 */
public class ServerPlayerContainersQuery implements Query<List<PlayerContainer>> {

    private final ServerUUID serverUUID;

    public ServerPlayerContainersQuery(ServerUUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public List<PlayerContainer> executeQuery(SQLDB db) {
        List<PlayerContainer> containers = new ArrayList<>();

        Collection<BaseUser> baseUsers = db.query(BaseUserQueries.fetchServerBaseUsers(serverUUID));

        Map<UUID, List<GeoInfo>> geoInformation = db.query(GeoInfoQueries.fetchServerGeoInformation(serverUUID));
        Map<UUID, List<Nickname>> nicknames = db.query(NicknameQueries.fetchNicknameDataOfServer(serverUUID));
        Map<UUID, List<Ping>> pingData = db.query(PingQueries.fetchPingDataOfServer(serverUUID));
        Map<UUID, List<FinishedSession>> sessions = db.query(SessionQueries.fetchSessionsOfServer(serverUUID));

        Map<UUID, UserInfo> userInformation = db.query(UserInfoQueries.fetchUserInformationOfServer(serverUUID));

        Map<UUID, PerServerContainer> perServerInfo = getPerServerData(
                userInformation,
                sessions,
                pingData
        );

        for (BaseUser user : baseUsers) {
            PlayerContainer container = new PlayerContainer();

            // BaseUser
            UUID uuid = user.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);
            container.putRawData(PlayerKeys.NAME, user.getName());
            container.putRawData(PlayerKeys.REGISTERED, user.getRegistered());
            container.putRawData(PlayerKeys.KICK_COUNT, user.getTimesKicked());

            // GeoInfo
            container.putRawData(PlayerKeys.GEO_INFO, geoInformation.getOrDefault(uuid, Collections.emptyList()));

            // Ping
            container.putRawData(PlayerKeys.PING, pingData.get(uuid));

            // Nickname, only used for the raw server JSON.
            container.putRawData(PlayerKeys.NICKNAMES, nicknames.get(uuid));

            // PerServerContainer
            container.putRawData(PlayerKeys.PER_SERVER, perServerInfo.get(uuid));

            container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                        List<FinishedSession> playerSessions = sessions.getOrDefault(uuid, new ArrayList<>());
                        container.getValue(PlayerKeys.ACTIVE_SESSION)
                                .map(ActiveSession::toFinishedSessionFromStillActive)
                                .ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            // Calculating getters
            container.putCachingSupplier(PlayerKeys.WORLD_TIMES, () -> {
                WorldTimes worldTimes = new PerServerMutator(container.getUnsafe(PlayerKeys.PER_SERVER)).flatMapWorldTimes();
                container.getValue(PlayerKeys.ACTIVE_SESSION)
                        .ifPresent(session -> worldTimes.add(
                                session.getExtraData(WorldTimes.class).orElseGet(WorldTimes::new))
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

            containers.add(container);
        }
        return containers;
    }

    /**
     * Create PerServerContainers for each player.
     *
     * @param userInformation Map: Player UUID - UserInfo of this server
     * @param sessions        Map: Player UUID - List of Sessions of this server
     * @param ping            Map: Player UUID - List of Ping data of this server
     * @return Map: Player UUID - PerServerContainer
     */
    private Map<UUID, PerServerContainer> getPerServerData(
            Map<UUID, UserInfo> userInformation,
            Map<UUID, List<FinishedSession>> sessions,
            Map<UUID, List<Ping>> ping
    ) {
        Map<UUID, PerServerContainer> perServerContainers = new HashMap<>();

        for (Map.Entry<UUID, UserInfo> entry : userInformation.entrySet()) {
            UUID playerUUID = entry.getKey();
            PerServerContainer perServerContainer = perServerContainers.getOrDefault(playerUUID, new PerServerContainer());

            perServerContainer.putUserInfo(entry.getValue());         // Information found withing UserInfo
            perServerContainer.putSessions(sessions.get(playerUUID)); // Session list
            perServerContainer.putPing(ping.get(playerUUID));         // Ping list
            perServerContainer.putCalculatingSuppliers();             // Derivative values

            perServerContainers.put(playerUUID, perServerContainer);
        }

        return perServerContainers;
    }
}