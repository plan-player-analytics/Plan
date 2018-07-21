/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bungee.api;


import com.djrapitops.plan.api.CommonAPI;
import com.djrapitops.plan.bungee.BungeeSystem;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import java.util.UUID;

/**
 * PlanAPI extension for Bungee.
 *
 * @author Rsl1122
 */
public class PlanBungeeAPI extends CommonAPI {

    private final BungeeSystem bungeeSystem;

    public PlanBungeeAPI(BungeeSystem bungeeSystem) {
        this.bungeeSystem = bungeeSystem;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        bungeeSystem.getHookHandler().addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return bungeeSystem.getCacheSystem().getDataCache().getName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return bungeeSystem.getDatabaseSystem().getActiveDatabase().fetch();
    }
}
