package com.djrapitops.plan.common.system.cache;

import com.djrapitops.plan.common.system.PlanSystem;

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
