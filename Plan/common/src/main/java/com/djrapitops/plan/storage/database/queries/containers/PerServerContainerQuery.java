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

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.PerServerContainer;
import com.djrapitops.plan.delivery.domain.container.SupplierDataContainer;
import com.djrapitops.plan.delivery.domain.keys.Key;
import com.djrapitops.plan.delivery.domain.keys.PerServerKeys;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.PerServerAggregateQueries;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;
import com.djrapitops.plan.storage.database.queries.objects.UserInfoQueries;
import com.djrapitops.plan.storage.database.queries.objects.WorldTimesQueries;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Used to get a PerServerContainer for a specific player.
 *
 * @author AuroraLS3
 */
public class PerServerContainerQuery implements Query<PerServerContainer> {

    private final UUID playerUUID;

    public PerServerContainerQuery(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    @Override
    public PerServerContainer executeQuery(SQLDB db) {
        PerServerContainer perServerContainer = new PerServerContainer();

        userInformation(db, perServerContainer);
        lastSeen(db, perServerContainer);
        playerKillCount(db, perServerContainer);
        mobKillCount(db, perServerContainer);
        totalDeathCount(db, perServerContainer);
        worldTimes(db, perServerContainer);

        Map<ServerUUID, List<FinishedSession>> sessions = db.query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        for (Map.Entry<ServerUUID, List<FinishedSession>> entry : sessions.entrySet()) {
            ServerUUID serverUUID = entry.getKey();
            List<FinishedSession> serverSessions = entry.getValue();

            DataContainer serverContainer = perServerContainer.getOrDefault(serverUUID, new SupplierDataContainer());
            serverContainer.putRawData(PerServerKeys.SESSIONS, serverSessions);

            perServerContainer.put(serverUUID, serverContainer);
        }

        return perServerContainer;
    }

    private void totalDeathCount(SQLDB db, PerServerContainer container) {
        matchingEntrySet(PerServerKeys.DEATH_COUNT, PerServerAggregateQueries.totalDeathCountOnServers(playerUUID), db, container);
    }

    private void worldTimes(SQLDB db, PerServerContainer container) {
        matchingEntrySet(PerServerKeys.WORLD_TIMES, WorldTimesQueries.fetchPlayerWorldTimesOnServers(playerUUID), db, container);
    }

    private void mobKillCount(SQLDB db, PerServerContainer container) {
        matchingEntrySet(PerServerKeys.MOB_KILL_COUNT, PerServerAggregateQueries.mobKillCountOnServers(playerUUID), db, container);
    }

    private void playerKillCount(SQLDB db, PerServerContainer container) {
        matchingEntrySet(PerServerKeys.PLAYER_KILL_COUNT, PerServerAggregateQueries.playerKillCountOnServers(playerUUID), db, container);
    }

    private void lastSeen(SQLDB db, PerServerContainer container) {
        matchingEntrySet(PerServerKeys.LAST_SEEN, PerServerAggregateQueries.lastSeenOnServers(playerUUID), db, container);
    }

    private void userInformation(SQLDB db, PerServerContainer container) {
        Set<UserInfo> userInformation = db.query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        container.putUserInfo(userInformation);
    }

    private <T> void matchingEntrySet(Key<T> key, Query<Map<ServerUUID, T>> map, SQLDB db, PerServerContainer container) {
        for (Map.Entry<ServerUUID, T> entry : db.query(map).entrySet()) {
            container.putToContainerOfServer(entry.getKey(), key, entry.getValue());
        }
    }
}