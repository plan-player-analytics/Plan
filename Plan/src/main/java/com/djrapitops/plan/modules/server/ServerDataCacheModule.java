package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Server CacheSystem.
 *
 * @author Rsl1122
 */
@Module
public class ServerDataCacheModule {

    @Provides
    @Singleton
    SessionCache provideSessionCache(DataCache cache) {
        return cache;
    }

}