package com.djrapitops.plan.system.cache;

import javax.inject.Inject;

/**
 * CacheSystem for Bungee.
 * <p>
 * Used for overriding {@link DataCache} with {@link BungeeDataCache}
 *
 * @author Rsl1122
 */
public class BungeeCacheSystem extends CacheSystem {

    @Inject
    public BungeeCacheSystem(BungeeDataCache dataCache, GeolocationCache geolocationCache) {
        super(dataCache, geolocationCache);
    }

}
