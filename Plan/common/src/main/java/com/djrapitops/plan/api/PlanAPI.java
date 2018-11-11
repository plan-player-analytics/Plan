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

import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

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
    }

    void addPluginDataSource(PluginData pluginData);

    String getPlayerInspectPageLink(UUID uuid);

    String getPlayerInspectPageLink(String playerName);

    String getPlayerName(UUID uuid);

    UUID playerNameToUUID(String playerName);

    Map<UUID, String> getKnownPlayerNames();

    FetchOperations fetchFromPlanDB();
}
