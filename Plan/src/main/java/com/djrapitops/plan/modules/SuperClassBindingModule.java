package com.djrapitops.plan.modules;

import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding instances of implementations to super classes.
 *
 * @author Rsl1122
 */
@Module
public class SuperClassBindingModule {

    @Provides
    @Singleton
    SessionCache provideSessionCache(DataCache cache) {
        return cache;
    }

}