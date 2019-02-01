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
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.queries.PlayerFetchQueries;

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

        List<UserInfo> userInformation = db.query(PlayerFetchQueries.playerServerSpecificUserInformation(playerUUID));
        for (UserInfo userInfo : userInformation) {
            UUID serverUUID = userInfo.getServerUUID();

            DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());
            container.putRawData(PlayerKeys.REGISTERED, userInfo.getRegistered());
            container.putRawData(PlayerKeys.BANNED, userInfo.isBanned());
            container.putRawData(PlayerKeys.OPERATOR, userInfo.isOperator());
            perServerContainer.put(serverUUID, container);
        }

        Map<UUID, List<Session>> sessions = db.getSessionsTable().getSessions(playerUUID);
        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID serverUUID = entry.getKey();
            List<Session> serverSessions = entry.getValue();

            DataContainer container = perServerContainer.getOrDefault(serverUUID, new DataContainer());
            container.putRawData(PerServerKeys.SESSIONS, serverSessions);

            container.putSupplier(PerServerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PerServerKeys.WORLD_TIMES, () -> SessionsMutator.forContainer(container).toTotalWorldTimes());
            container.putSupplier(PerServerKeys.PLAYER_KILLS, () -> SessionsMutator.forContainer(container).toPlayerKillList());
            container.putSupplier(PerServerKeys.PLAYER_DEATHS, () -> SessionsMutator.forContainer(container).toPlayerDeathList());
            container.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, () -> container.getUnsafe(PerServerKeys.PLAYER_KILLS).size());
            container.putSupplier(PerServerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PerServerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());

            perServerContainer.put(serverUUID, container);
        }

        return perServerContainer;
    }
}