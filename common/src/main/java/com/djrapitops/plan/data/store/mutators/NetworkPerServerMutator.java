package com.djrapitops.plan.data.store.mutators;


import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.containers.PerServerContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.CommonKeys;
import com.djrapitops.plan.data.store.keys.PlayerKeys;

import java.util.*;

public class NetworkPerServerMutator {

    private final Map<UUID, List<DataContainer>> perServerContainers;

    public NetworkPerServerMutator(PlayersMutator playersMutator) {
        this.perServerContainers = perServerContainers(playersMutator);
    }

    public static NetworkPerServerMutator forContainer(DataContainer container) {
        return new NetworkPerServerMutator(
                container.getValue(CommonKeys.PLAYERS_MUTATOR)
                        .orElse(PlayersMutator.forContainer(container))
        );
    }

    public Map<UUID, List<DataContainer>> getPerServerContainers() {
        return perServerContainers;
    }

    private Map<UUID, List<DataContainer>> perServerContainers(PlayersMutator playersMutator) {
        Map<UUID, List<DataContainer>> dataContainerMap = new HashMap<>();

        for (PlayerContainer playerContainer : playersMutator.all()) {
            UUID uuid = playerContainer.getUnsafe(PlayerKeys.UUID);
            PerServerContainer perServerContainer = playerContainer.getValue(PlayerKeys.PER_SERVER).orElse(new PerServerContainer());
            for (Map.Entry<UUID, DataContainer> entry : perServerContainer.entrySet()) {
                UUID serverUUID = entry.getKey();
                DataContainer container = entry.getValue();
                container.putRawData(PlayerKeys.UUID, uuid);
                List<DataContainer> dataContainers = dataContainerMap.getOrDefault(serverUUID, new ArrayList<>());
                dataContainers.add(container);
                dataContainerMap.put(serverUUID, dataContainers);
            }
        }

        return dataContainerMap;
    }
}
