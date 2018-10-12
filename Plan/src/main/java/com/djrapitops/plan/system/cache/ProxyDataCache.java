package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.PlanSystem;

import java.util.UUID;

/**
 * Proxy server specific DataCache.
 * <p>
 * Used for overriding {@link SessionCache#endSession(UUID, long)}.
 *
 * @author Rsl1122
 */
public class ProxyDataCache extends DataCache {

    public ProxyDataCache(PlanSystem system) {
        super(system);
    }

    @Override
    public void endSession(UUID uuid, long time) {
        removeSessionFromCache(uuid);
        /* Proxy should not save sessions so session is not removed.. */
    }
}
