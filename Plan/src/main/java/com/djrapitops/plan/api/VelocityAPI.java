/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.VelocitySystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import java.util.UUID;

/**
 * PlanAPI extension for Velocity.
 *
 * Based on BungeeAPI
 *
 * @author MicleBrick
 */
public class VelocityAPI extends CommonAPI {

    private final VelocitySystem velocitySystem;

    public VelocityAPI(VelocitySystem velocitySystem) {
        this.velocitySystem = velocitySystem;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        velocitySystem.getHookHandler().addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return velocitySystem.getCacheSystem().getDataCache().getName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return velocitySystem.getDatabaseSystem().getActiveDatabase().fetch();
    }
}
