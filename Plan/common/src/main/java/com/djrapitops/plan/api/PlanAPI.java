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
package com.djrapitops.plan.api;

import com.djrapitops.plan.api.data.PlayerContainer;
import com.djrapitops.plan.api.data.ServerContainer;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface for PlanAPI methods.
 *
 * @author Rsl1122
 */
public interface PlanAPI {

    static PlanAPI getInstance() {
        return Optional.ofNullable(PlanAPIHolder.API)
                .orElseThrow(() -> new IllegalStateException("PlanAPI has not been initialised yet."));
    }

    class PlanAPIHolder {
        static PlanAPI API;

        static void set(PlanAPI api) {
            PlanAPIHolder.API = api;
        }

        private PlanAPIHolder() {
            /* Static variable holder */
        }
    }

    void addPluginDataSource(PluginData pluginData);

    String getPlayerInspectPageLink(UUID uuid);

    String getPlayerInspectPageLink(String playerName);

    String getPlayerName(UUID uuid);

    UUID playerNameToUUID(String playerName);

    Map<UUID, String> getKnownPlayerNames();

    /**
     * Fetch things from the database.
     *
     * @return FetchOperations object.
     * @deprecated FetchOperations interface is going to removed since it is too rigid.
     */
    @Deprecated
    FetchOperations fetchFromPlanDB();

    /**
     * Fetch PlayerContainer from the database.
     * <p>
     * Blocking operation.
     *
     * @param uuid UUID of the player.
     * @return a {@link PlayerContainer}.
     */
    default PlayerContainer fetchPlayerContainer(UUID uuid) {
        return new PlayerContainer(fetchFromPlanDB().getPlayerContainer(uuid));
    }

    /**
     * Fetch a ServerContainer from the database.
     * <p>
     * Blocking operation.
     *
     * @param serverUUID UUID of the server.
     * @return a {@link ServerContainer}.
     */
    default ServerContainer fetchServerContainer(UUID serverUUID) {
        return new ServerContainer(fetchFromPlanDB().getServerContainer(serverUUID));
    }

    /**
     * Fetch server UUIDs.
     *
     * @return All Plan server UUIDs.
     */
    default Collection<UUID> fetchServerUUIDs() {
        return fetchFromPlanDB().getServerUUIDs();
    }
}
