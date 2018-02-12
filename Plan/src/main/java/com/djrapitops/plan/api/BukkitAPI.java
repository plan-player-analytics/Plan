/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.BukkitSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import java.util.UUID;

/**
 * PlanAPI extension for Bukkit
 *
 * @author Rsl1122
 */
public class BukkitAPI extends CommonAPI {

    private final BukkitSystem bukkitSystem;

    public BukkitAPI(BukkitSystem bukkitSystem) {
        this.bukkitSystem = bukkitSystem;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        bukkitSystem.getHookHandler().addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return bukkitSystem.getCacheSystem().getDataCache().getName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return bukkitSystem.getDatabaseSystem().getActiveDatabase().fetch();
    }
}