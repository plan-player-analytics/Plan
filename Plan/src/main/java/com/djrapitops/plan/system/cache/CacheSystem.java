/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System that holds data caches of the plugin.
 *
 * @author Rsl1122
 */
@Singleton
public class CacheSystem implements SubSystem {

    private final DataCache dataCache;
    private final GeolocationCache geolocationCache;

    @Inject
    public CacheSystem(DataCache dataCache, GeolocationCache geolocationCache) {
        this.dataCache = dataCache;
        this.geolocationCache = geolocationCache;
    }

    @Override
    public void enable() throws EnableException {
        dataCache.enable();
        geolocationCache.enable();
    }

    @Override
    public void disable() {
        geolocationCache.clearCache();
    }

    public DataCache getDataCache() {
        return dataCache;
    }

    public GeolocationCache getGeolocationCache() {
        return geolocationCache;
    }

}
