/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plugin.utilities.Verify;

/**
 * System that holds data caches of the plugin.
 *
 * @author Rsl1122
 */
public class CacheSystem implements SubSystem {

    private final DataCache dataCache;
    private final GeolocationCache geolocationCache;

    public CacheSystem(PlanSystem system) {
        this(new DataCache(system));
    }

    protected CacheSystem(DataCache dataCache) {
        this.dataCache = dataCache;
        geolocationCache = new GeolocationCache();
    }

    public static CacheSystem getInstance() {
        CacheSystem cacheSystem = PlanSystem.getInstance().getCacheSystem();
        Verify.nullCheck(cacheSystem, () -> new IllegalStateException("Cache System was not initialized."));
        return cacheSystem;
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
