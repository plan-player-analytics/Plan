package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.ServerAPI;
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
    PlanAPI providePlanAPI(ServerAPI serverAPI) {
        return serverAPI;
    }

}