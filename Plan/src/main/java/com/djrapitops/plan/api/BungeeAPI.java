/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.BungeeSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import java.util.UUID;

/**
 * PlanAPI extension for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeAPI extends CommonAPI {

    private final BungeeSystem bungeeSystem;

    public BungeeAPI(BungeeSystem bungeeSystem) {
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