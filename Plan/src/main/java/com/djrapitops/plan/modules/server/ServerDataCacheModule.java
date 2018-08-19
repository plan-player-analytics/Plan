package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Server CacheSystem.
 *
 * @author Rsl1122
 */
@Module
public class ServerDataCacheModule {

    @Provides
    SessionCache provideSessionCache(DataCache cache) {
        return cache;
    }

}