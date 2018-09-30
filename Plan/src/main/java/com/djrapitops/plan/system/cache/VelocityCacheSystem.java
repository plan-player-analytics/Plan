package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.system.PlanSystem;

/**
 * CacheSystem for Velocity.
 *
 * Based on BungeeCacheSystem
 *
 * <p>
 * Used for overriding {@link DataCache} with {@link VelocityDataCache}
 *
 * @author MicleBrick
 */
public class VelocityCacheSystem extends CacheSystem {

    public VelocityCacheSystem(PlanSystem system) {
        super(new VelocityDataCache(system), system);
    }

}
