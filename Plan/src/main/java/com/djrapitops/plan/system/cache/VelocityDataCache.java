package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.PlanSystem;

import java.util.UUID;

/**
 * Velocity specific DataCache.
 *
 * Based on BungeeDataCache
 *
 * <p>
 * Used for overriding {@link SessionCache#endSession(UUID, long)}.
 *
 * @author MicleBrick
 */
public class VelocityDataCache extends DataCache {

    public VelocityDataCache(PlanSystem system) {
        super(system);
    }

    @Override
    public void endSession(UUID uuid, long time) {
        removeSessionFromCache(uuid);
        /* Velocity should not save sessions so session is not removed.. */
    }
}
