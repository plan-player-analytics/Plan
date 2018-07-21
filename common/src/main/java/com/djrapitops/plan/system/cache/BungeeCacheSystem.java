package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.PlanSystem;

/**
 * CacheSystem for Bungee.
 * <p>
 * Used for overriding {@link DataCache} with {@link BungeeDataCache}
 *
 * @author Rsl1122
 */
public class BungeeCacheSystem extends CacheSystem {

    public BungeeCacheSystem(PlanSystem system) {
        super(new BungeeDataCache(system));
    }

}
