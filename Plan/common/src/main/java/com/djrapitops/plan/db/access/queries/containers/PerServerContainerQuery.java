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

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.store.Key;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.SupplierDataContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.PerServerAggregateQueries;
import com.djrapitops.plan.db.access.queries.objects.SessionQueries;
import com.djrapitops.plan.db.access.queries.objects.UserInfoQueries;
import com.djrapitops.plan.db.access.queries.objects.WorldTimesQueries;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Used to get a PerServerContainer for a specific player.
 *
 * @author Rsl1122
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
        playerDeathCount(db, perServerContainer);
        mobKillCount(db, perServerContainer);
        totalDeathCount(db, perServerContainer);
        worldTimes(db, perServerContainer);

        // After-values that can be calculated without database.
        for (DataContainer serverContainer : perServerContainer.values()) {
            serverContainer.putSupplier(PerServerKeys.MOB_DEATH_COUNT, () ->
                    serverContainer.getUnsafe(PerServerKeys.DEATH_COUNT) - serverContainer.getUnsafe(PerServerKeys.PLAYER_DEATH_COUNT)
            );
        }

        Map<UUID, List<Session>> sessions = db.query(SessionQueries.fetchSessionsOfPlayer(playerUUID));
        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<Session> serverSessions = entry.getValue();

            DataContainer serverContainer = perServerContainer.getOrDefault(serverUUID, new SupplierDataContainer());
            serverContainer.putRawData(PerServerKeys.SESSIONS, serverSessions);

            serverContainer.putSupplier(PerServerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(serverContainer).toPlayerKillList());
            serverContainer.putSupplier(PerServerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(serverContainer).toPlayerDeathList());

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

    private void playerDeathCount(SQLDB db, PerServerContainer container) {
        matchingEntrySet(PerServerKeys.PLAYER_DEATH_COUNT, PerServerAggregateQueries.playerDeathCountOnServers(playerUUID), db, container);
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
        List<UserInfo> userInformation = db.query(UserInfoQueries.fetchUserInformationOfUser(playerUUID));
        container.putUserInfo(userInformation);
    }

    private <T> void matchingEntrySet(Key<T> key, Query<Map<UUID, T>> map, SQLDB db, PerServerContainer container) {
        for (Map.Entry<UUID, T> entry : db.query(map).entrySet()) {
            container.putToContainerOfServer(entry.getKey(), key, entry.getValue());
        }
    }
}