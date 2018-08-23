package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.system.cache.BungeeDataCache;
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
public class BungeeDataCacheModule {

    @Provides
    DataCache provideDataCache(BungeeDataCache bungeeDataCache) {
        return bungeeDataCache;
    }

    @Provides
    SessionCache provideSessionCache(DataCache cache) {
        return cache;
    }

}