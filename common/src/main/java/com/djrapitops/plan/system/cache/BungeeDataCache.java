package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.PlanSystem;

import java.util.UUID;

/**
 * Bungee specific DataCache.
 * <p>
 * Used for overriding {@link SessionCache#endSession(UUID, long)}.
 *
 * @author Rsl1122
 */
public class BungeeDataCache extends DataCache {

    public BungeeDataCache(PlanSystem system) {
        super(system);
    }

    @Override
    public void endSession(UUID uuid, long time) {
        removeSessionFromCache(uuid);
        /* Bungee should not save sessions so session is not removed.. */
    }
}
