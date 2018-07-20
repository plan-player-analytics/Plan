/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bukkit.api;

import com.djrapitops.plan.common.api.CommonAPI;
import com.djrapitops.plan.common.data.plugin.PluginData;
import com.djrapitops.plan.common.system.ServerSystem;
import com.djrapitops.plan.common.system.database.databases.operation.FetchOperations;

import java.util.UUID;

/**
 * PlanAPI extension for Bukkit
 *
 * @author Rsl1122
 */
public class PlanBukkitAPI extends CommonAPI {

    private final ServerSystem serverSystem;

    public PlanBukkitAPI(ServerSystem serverSystem) {
        this.serverSystem = serverSystem;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        serverSystem.getHookHandler().addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return serverSystem.getCacheSystem().getDataCache().getName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return serverSystem.getDatabaseSystem().getActiveDatabase().fetch();
    }
}
