/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * PlanAPI extension for proxy servers.
 *
 * @author Rsl1122
 */
@Singleton
public class ProxyAPI extends CommonAPI {

    private final HookHandler hookHandler;
    private final DBSystem dbSystem;

    @Inject
    public ProxyAPI(
            UUIDUtility uuidUtility,
            DBSystem dbSystem,
            HookHandler hookHandler,
            ErrorHandler errorHandler
    ) {
        super(uuidUtility, errorHandler);

        this.dbSystem = dbSystem;
        this.hookHandler = hookHandler;
    }

    @Override
    public void addPluginDataSource(PluginData pluginData) {
        hookHandler.addPluginDataSource(pluginData);
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return dbSystem.getDatabase().fetch().getPlayerName(uuid);
    }

    @Override
    public FetchOperations fetchFromPlanDB() {
        return dbSystem.getDatabase().fetch();
    }
}
