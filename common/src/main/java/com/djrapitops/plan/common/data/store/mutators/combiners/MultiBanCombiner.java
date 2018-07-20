package com.djrapitops.plan.common.data.store.mutators.combiners;

import com.djrapitops.plan.common.data.store.containers.DataContainer;
import com.djrapitops.plan.common.data.store.containers.PerServerContainer;
import com.djrapitops.plan.common.data.store.containers.PlayerContainer;
import com.djrapitops.plan.common.data.store.keys.PerServerKeys;
import com.djrapitops.plan.common.data.store.keys.PlayerKeys;
import com.djrapitops.plan.common.data.store.keys.ServerKeys;
import com.djrapitops.plan.common.system.info.server.ServerInfo;

import java.util.*;

public class MultiBanCombiner {

    private final DataContainer container;

    /**
     * Constructor.
     *
     * @param container DataContainer that supports {@link com.djrapitops.plan.common.data.store.keys.ServerKeys}.PLAYERS
     */
    public MultiBanCombiner(DataContainer container) {
        this.container = container;
    }

    public void combine(Set<UUID> banned) {
        combine(Collections.singletonMap(ServerInfo.getServerUUID(), banned));
    }

    public void combine(Map<UUID, Set<UUID>> perServerBanned) {
        List<PlayerContainer> playerContainers = container.getValue(ServerKeys.PLAYERS).orElse(new ArrayList<>());

        for (Map.Entry<UUID, Set<UUID>> entry : perServerBanned.entrySet()) {
            UUID serverUUID = entry.getKey();
            Set<UUID> banned = entry.getValue();
            for (PlayerContainer player : playerContainers) {
                if (player.getValue(PlayerKeys.UUID).map(banned::contains).orElse(false)) {
                    PerServerContainer perServer = player.getValue(PlayerKeys.PER_SERVER)
                            .orElse(new PerServerContainer());
                    DataContainer perServerContainer = perServer.getOrDefault(serverUUID, new DataContainer());

                    perServerContainer.putRawData(PerServerKeys.BANNED, true);

                    perServer.put(serverUUID, perServerContainer);
                }
            }
        }
    }
}
