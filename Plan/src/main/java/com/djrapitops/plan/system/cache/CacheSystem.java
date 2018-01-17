/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.utilities.NullCheck;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class CacheSystem implements SubSystem {

    private final DataCache dataCache;
    private final GeolocationCache geolocationCache;

    public CacheSystem(PlanSystem system) {
        dataCache = new DataCache(system);
        geolocationCache = new GeolocationCache();
    }

    public static CacheSystem getInstance() {
        CacheSystem cacheSystem = PlanSystem.getInstance().getCacheSystem();
        NullCheck.check(cacheSystem, new IllegalStateException("Cache System was not initialized."));
        return cacheSystem;
    }

    @Override
    public void enable() throws EnableException {
        try {
            GeolocationCache.checkDB();
        } catch (UnknownHostException e) {
            Log.error("Plan Requires internet access on first run to download GeoLite2 Geolocation database.");
        } catch (IOException e) {
            throw new EnableException("Something went wrong saving the downloaded GeoLite2 Geolocation database", e);
        }
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