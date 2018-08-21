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
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.UUID;

/**
 * PlanAPI extension for Bungee.
 *
 * @author Rsl1122
 */
public class BungeeAPI extends CommonAPI {

    private final HookHandler hookHandler;
    private final Database database;
    private final DataCache dataCache;

    @Inject
    public BungeeAPI(
            UUIDUtility uuidUtility,
            Database database,
            DataCache dataCache,
            HookHandler hookHandler,
            ErrorHandler errorHandler
    ) {
        super(uuidUtility, errorHandler);

        this.database = database;
        this.dataCache = dataCache;
        this.hookHandler = hookHandler;
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
        return database.fetch();
    }
}
