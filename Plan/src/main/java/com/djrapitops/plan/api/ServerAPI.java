/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;

import javax.inject.Inject;
import java.util.UUID;

/**
 * PlanAPI extension for Bukkit
 *
 * @author Rsl1122
 */
public class ServerAPI extends CommonAPI {

    private final HookHandler hookHandler;
    private final Database activeDatabase;
    private final DataCache dataCache;

    @Inject
    public ServerAPI(HookHandler hookHandler, Database activeDatabase, DataCache dataCache) {
        this.hookHandler = hookHandler;
        this.activeDatabase = activeDatabase;
        this.dataCache = dataCache;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        hookHandler.addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return dataCache.getName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return activeDatabase.fetch();
    }
}
