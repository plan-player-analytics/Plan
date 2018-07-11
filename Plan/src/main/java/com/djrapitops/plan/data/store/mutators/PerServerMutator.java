package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.keys.PerServerKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.time.WorldTimes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mutator for PerServerContainer object.
 *
 * @author Rsl1122
 */
public class PerServerMutator {

    private final PerServerContainer data;

    public PerServerMutator(PerServerContainer data) {
        this.data = data;
    }

    public static PerServerMutator forContainer(DataContainer container) {
        return new PerServerMutator(container.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer()));
    }

    public List<Session> flatMapSessions() {
        return data.values().stream()
                .filter(container -> container.supports(PerServerKeys.SESSIONS))
                .map(container -> container.getUnsafe(PerServerKeys.SESSIONS))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public WorldTimes flatMapWorldTimes() {
        WorldTimes total = new WorldTimes(new HashMap<>());

        for (DataContainer container : data.values()) {
            if (container.supports(PerServerKeys.WORLD_TIMES)) {
                WorldTimes worldTimes = container.getUnsafe(PerServerKeys.WORLD_TIMES);
                total.add(worldTimes);
            }
        }

        return total;
    }

    public Map<UUID, WorldTimes> worldTimesPerServer() {
        Map<UUID, WorldTimes> timesMap = new HashMap<>();
        for (Map.Entry<UUID, DataContainer> entry : data.entrySet()) {
            DataContainer container = entry.getValue();
            timesMap.put(entry.getKey(), container.getValue(PerServerKeys.WORLD_TIMES).orElse(new WorldTimes(new HashMap<>())));
        }
        return timesMap;
    }

    public UUID favoriteServer() {
        long max = 0;
        UUID maxServer = null;

        for (Map.Entry<UUID, DataContainer> entry : data.entrySet()) {
            long total = SessionsMutator.forContainer(entry.getValue()).toPlaytime();
            if (total > max) {
                max = total;
                maxServer = entry.getKey();
            }
        }

        return maxServer;
    }

    public Map<UUID, List<Session>> sessionsPerServer() {
        Map<UUID, List<Session>> sessionMap = new HashMap<>();
        for (Map.Entry<UUID, DataContainer> entry : data.entrySet()) {
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
}