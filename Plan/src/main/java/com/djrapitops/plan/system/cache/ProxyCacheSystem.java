package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.PlanSystem;

/**
 * CacheSystem for proxy servers.
 * <p>
 * Used for overriding {@link DataCache} with {@link ProxyDataCache}
 *
 * @author Rsl1122
 */
public class ProxyCacheSystem extends CacheSystem {

    public ProxyCacheSystem(PlanSystem system) {
        super(new ProxyDataCache(system), system);
    }

}
