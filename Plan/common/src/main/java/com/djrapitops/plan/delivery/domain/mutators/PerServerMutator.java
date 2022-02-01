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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.PerServerContainer;
import com.djrapitops.plan.delivery.domain.keys.PerServerKeys;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mutator for PerServerContainer object.
 *
 * @author AuroraLS3
 */
public class PerServerMutator {

    private final PerServerContainer data;

    public PerServerMutator(PerServerContainer data) {
        this.data = data;
    }

    public static PerServerMutator forContainer(DataContainer container) {
        return new PerServerMutator(container.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer()));
    }

    public List<FinishedSession> flatMapSessions() {
        return data.values().stream()
                .filter(container -> container.supports(PerServerKeys.SESSIONS))
                .map(container -> container.getValue(PerServerKeys.SESSIONS).orElse(Collections.emptyList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public WorldTimes flatMapWorldTimes() {
        WorldTimes total = new WorldTimes();

        for (DataContainer container : data.values()) {
            if (container.supports(PerServerKeys.WORLD_TIMES)) {
                WorldTimes worldTimes = container.getUnsafe(PerServerKeys.WORLD_TIMES);
                total.add(worldTimes);
            }
        }

        return total;
    }

    public Map<ServerUUID, WorldTimes> worldTimesPerServer() {
        Map<ServerUUID, WorldTimes> timesMap = new HashMap<>();
        for (Map.Entry<ServerUUID, DataContainer> entry : data.entrySet()) {
            DataContainer container = entry.getValue();
            timesMap.put(entry.getKey(), container.getValue(PerServerKeys.WORLD_TIMES).orElse(new WorldTimes()));
        }
        return timesMap;
    }

    public Optional<ServerUUID> favoriteServer() {
        long max = 0;
        ServerUUID maxServer = null;

        for (Map.Entry<ServerUUID, DataContainer> entry : data.entrySet()) {
            long total = SessionsMutator.forContainer(entry.getValue()).toPlaytime();
            if (total > max) {
                max = total;
                maxServer = entry.getKey();
            }
        }

        return Optional.ofNullable(maxServer);
    }

    public Map<ServerUUID, List<FinishedSession>> sessionsPerServer() {
        Map<ServerUUID, List<FinishedSession>> sessionMap = new HashMap<>();
        for (Map.Entry<ServerUUID, DataContainer> entry : data.entrySet()) {
            sessionMap.put(entry.getKey(), entry.getValue().getValue(PerServerKeys.SESSIONS).orElse(new ArrayList<>()));
        }
        return sessionMap;
    }

    public boolean isBanned() {
        for (DataContainer container : data.values()) {
            if (container.getValue(PlayerKeys.BANNED).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOperator() {
        for (DataContainer container : data.values()) {
            if (container.getValue(PlayerKeys.OPERATOR).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    public Optional<String> latestJoinAddress() {
        long latest = Long.MIN_VALUE;
        String latestJoinAddress = null;
        for (DataContainer value : data.values()) {
            long registerDate = value.getValue(PerServerKeys.REGISTERED).orElse(Long.MIN_VALUE);
            Optional<String> joinAddress = value.getValue(PerServerKeys.JOIN_ADDRESS);
            if (registerDate > latest && joinAddress.isPresent()) {
                latest = registerDate;
                latestJoinAddress = joinAddress.get();
            }
        }
        return Optional.ofNullable(latestJoinAddress);
    }
}