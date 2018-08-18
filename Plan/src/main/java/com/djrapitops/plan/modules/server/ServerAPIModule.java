package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.ServerAPI;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.database.databases.Database;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Server PlanAPI.
 *
 * @author Rsl1122
 */
@Module
public class ServerAPIModule {

    @Provides
    PlanAPI providePlanAPI(HookHandler hookHandler, DataCache dataCache, Database database) {
        return new ServerAPI(hookHandler, database, dataCache);
    }

}