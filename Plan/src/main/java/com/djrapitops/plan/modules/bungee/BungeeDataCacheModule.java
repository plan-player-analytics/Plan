package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.system.cache.BungeeDataCache;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.system.cache.SessionCache;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee CacheSystem.
 *
 * @author Rsl1122
 */
@Module
public class BungeeDataCacheModule {

    @Provides
    @Singleton
    DataCache provideDataCache(BungeeDataCache bungeeDataCache) {
        return bungeeDataCache;
    }

    @Provides
    @Singleton
    SessionCache provideSessionCache(DataCache cache) {
        return cache;
    }

}