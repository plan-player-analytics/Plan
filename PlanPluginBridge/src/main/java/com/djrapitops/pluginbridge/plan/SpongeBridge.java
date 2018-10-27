package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;
import com.djrapitops.pluginbridge.plan.luckperms.LuckPermsHook;
import com.djrapitops.pluginbridge.plan.nucleus.NucleusHook;
import com.djrapitops.pluginbridge.plan.sponge.SpongeEconomyHook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Plugin bridge for Sponge plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class SpongeBridge extends Bridge {

    private final BuyCraftHook buyCraftHook;
    private final LuckPermsHook luckPermsHook;
    private final NucleusHook nucleusHook;
    private final SpongeEconomyHook spongeEconomyHook;

    @Inject
    public SpongeBridge(
            PlanConfig config,
            ErrorHandler errorHandler,

            BuyCraftHook buyCraftHook,
            LuckPermsHook luckPermsHook,
            NucleusHook nucleusHook,
            SpongeEconomyHook spongeEconomyHook
    ) {
        super(config, errorHandler);
        this.buyCraftHook = buyCraftHook;
        this.luckPermsHook = luckPermsHook;
        this.nucleusHook = nucleusHook;
        this.spongeEconomyHook = spongeEconomyHook;
    }

    @Override
    Hook[] getHooks() {
        return new Hook[]{
                buyCraftHook,
                luckPermsHook,
                nucleusHook,
                spongeEconomyHook
        };
    }
}