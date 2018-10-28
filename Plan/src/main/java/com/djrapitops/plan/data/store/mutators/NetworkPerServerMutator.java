/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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
