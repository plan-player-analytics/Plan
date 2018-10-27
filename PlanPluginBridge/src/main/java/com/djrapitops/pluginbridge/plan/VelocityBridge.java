package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Plugin bridge for Velocity plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class VelocityBridge extends Bridge {

    private final BuyCraftHook buyCraftHook;

    @Inject
    public VelocityBridge(
            PlanConfig config,
            ErrorHandler errorHandler,

            BuyCraftHook buyCraftHook
    ) {
        super(config, errorHandler);
        this.buyCraftHook = buyCraftHook;
    }

    @Override
    Hook[] getHooks() {
        return new Hook[]{
                buyCraftHook
        };
    }
}