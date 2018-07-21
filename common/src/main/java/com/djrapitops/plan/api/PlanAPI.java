/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api;


import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import java.util.Map;
import java.util.UUID;

/**
 * Interface for PlanAPI methods.
 *
 * @author Rsl1122
 */
public interface PlanAPI {

    static PlanAPI getInstance() {
        return PlanSystem.getInstance().getPlanAPI();
    }

    void addPluginDataSource(PluginData pluginData);

    String getPlayerInspectPageLink(UUID uuid);

    String getPlayerInspectPageLink(String playerName);

    String getPlayerName(UUID uuid);

    UUID playerNameToUUID(String playerName);

    Map<UUID, String> getKnownPlayerNames();

    FetchOperations fetchFromPlanDB();
}
