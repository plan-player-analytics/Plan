package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.api.BungeeAPI;
import com.djrapitops.plan.api.PlanAPI;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee PlanAPI.
 *
 * @author Rsl1122
 */
@Module
public class BungeeAPIModule {

    @Provides
    @Singleton
    PlanAPI providePlanAPI(BungeeAPI bungeeAPI) {
        return bungeeAPI;
    }

}