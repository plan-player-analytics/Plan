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
package com.djrapitops.plan.delivery.domain.container;

import com.djrapitops.plan.delivery.domain.keys.Key;
import com.djrapitops.plan.delivery.domain.keys.PerServerKeys;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Container for data about a player linked.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.delivery.domain.keys.PerServerKeys For Key objects.
 */
public class PerServerContainer extends HashMap<ServerUUID, DataContainer> {

    public <T> void putToContainerOfServer(ServerUUID serverUUID, Key<T> key, T value) {
        DataContainer container = getOrDefault(serverUUID, new DynamicDataContainer());
        container.putRawData(key, value);
        put(serverUUID, container);
    }

    public void putUserInfo(UserInfo userInfo) {
        ServerUUID serverUUID = userInfo.getServerUUID();
        putToContainerOfServer(serverUUID, PerServerKeys.REGISTERED, userInfo.getRegistered());
        putToContainerOfServer(serverUUID, PerServerKeys.BANNED, userInfo.isBanned());
        putToContainerOfServer(serverUUID, PerServerKeys.OPERATOR, userInfo.isOperator());
        putToContainerOfServer(serverUUID, PerServerKeys.JOIN_ADDRESS, userInfo.getJoinAddress());
    }

    public void putUserInfo(Collection<UserInfo> userInformation) {
        for (UserInfo userInfo : userInformation) {
            putUserInfo(userInfo);
        }
    }

    public void putCalculatingSuppliers() {
        for (DataContainer container : values()) {
            container.putSupplier(PerServerKeys.LAST_SEEN, () -> SessionsMutator.forContainer(container).toLastSeen());

            container.putSupplier(PerServerKeys.WORLD_TIMES, () -> SessionsMutator.forContainer(container).toTotalWorldTimes());
            container.putSupplier(PerServerKeys.PLAYER_KILL_COUNT, () -> SessionsMutator.forContainer(container).toPlayerKillCount());
            container.putSupplier(PerServerKeys.MOB_KILL_COUNT, () -> SessionsMutator.forContainer(container).toMobKillCount());
            container.putSupplier(PerServerKeys.DEATH_COUNT, () -> SessionsMutator.forContainer(container).toDeathCount());
        }
    }

    public void putSessions(Collection<FinishedSession> sessions) {
        if (sessions == null) {
            return;
        }

        for (FinishedSession session : sessions) {
            putSession(session);
        }
    }

    private void putSession(FinishedSession session) {
        if (session == null) {
            return;
        }

        ServerUUID serverUUID = session.getServerUUID();
        DataContainer container = getOrDefault(serverUUID, new DynamicDataContainer());
        if (!container.supports(PerServerKeys.SESSIONS)) {
            container.putRawData(PerServerKeys.SESSIONS, new ArrayList<>());
        }
        container.getUnsafe(PerServerKeys.SESSIONS).add(session);
        put(serverUUID, container);
    }

    public void putPing(List<Ping> pings) {
        if (pings == null) {
            return;
        }

        for (Ping ping : pings) {
            putPing(ping);
        }
    }

    private void putPing(Ping ping) {
        if (ping == null) {
            return;
        }

        ServerUUID serverUUID = ping.getServerUUID();
        DataContainer container = getOrDefault(serverUUID, new DynamicDataContainer());
        if (!container.supports(PerServerKeys.PING)) {
            container.putRawData(PerServerKeys.PING, new ArrayList<>());
        }
        container.getUnsafe(PerServerKeys.PING).add(ping);
        put(serverUUID, container);
    }
}