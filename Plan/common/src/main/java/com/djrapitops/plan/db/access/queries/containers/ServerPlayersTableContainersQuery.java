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
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.objects.BaseUserQueries;
import com.djrapitops.plan.db.access.queries.objects.GeoInfoQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.UserInfoQueries;
import com.djrapitops.plan.system.json.PlayersTableJSONParser;

import java.util.*;

/**
 * Optimized version of {@link ServerPlayerContainersQuery} for /server page Players table.
 *
 * @author Rsl1122
 * @see PlayersTableJSONParser For what needs to be included.
 */
public class ServerPlayersTableContainersQuery implements Query<List<PlayerContainer>> {

    private final UUID serverUUID;

    public ServerPlayersTableContainersQuery(UUID serverUUID) {
        this.serverUUID = serverUUID;
    }

    @Override
    public List<PlayerContainer> executeQuery(SQLDB db) {
        List<PlayerContainer> containers = new ArrayList<>();

        Collection<BaseUser> baseUsers = db.query(BaseUserQueries.fetchServerBaseUsers(serverUUID));

        Map<UUID, List<GeoInfo>> geoInformation = db.query(GeoInfoQueries.fetchServerGeoInformation(serverUUID));
        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfServer(serverUUID));
        Set<UUID> bannedUsers = db.query(UserInfoQueries.fetchBannedUUIDsOfServer(serverUUID));

        for (BaseUser user : baseUsers) {
            PlayerContainer container = new PlayerContainer();

            // BaseUser
            UUID uuid = user.getUuid();
            container.putRawData(PlayerKeys.UUID, uuid);
            container.putRawData(PlayerKeys.NAME, user.getName());
            container.putRawData(PlayerKeys.REGISTERED, user.getRegistered());
            container.putRawData(PlayerKeys.BANNED, bannedUsers.contains(uuid));

            // GeoInfo
            container.putRawData(PlayerKeys.GEO_INFO, geoInformation.getOrDefault(uuid, new ArrayList<>()));

            container.putCachingSupplier(PlayerKeys.SESSIONS, () -> {
                        List<Session> playerSessions = sessions.getOrDefault(uuid, new ArrayList<>());
                        container.getValue(PlayerKeys.ACTIVE_SESSION).ifPresent(playerSessions::add);
                        return playerSessions;
                    }
            );

            containers.add(container);
        }
        return containers;
    }

}