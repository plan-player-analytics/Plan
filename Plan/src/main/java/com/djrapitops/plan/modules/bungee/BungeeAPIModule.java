package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.api.BungeeAPI;
import com.djrapitops.plan.api.PlanAPI;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bungee PlanAPI.
 *
 * @author Rsl1122
 */
@Module
public class BungeeAPIModule {

    @Provides
    PlanAPI providePlanAPI(BungeeAPI bungeeAPI) {
        return bungeeAPI;
    }

}