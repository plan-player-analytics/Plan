package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.pluginbridge.plan.advancedban.AdvancedBanHook;
import com.djrapitops.pluginbridge.plan.buycraft.BuyCraftHook;
import com.djrapitops.pluginbridge.plan.litebans.LiteBansBungeeHook;
import com.djrapitops.pluginbridge.plan.luckperms.LuckPermsHook;
import com.djrapitops.pluginbridge.plan.viaversion.ViaVersionBungeeHook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Plugin bridge for BungeeCord plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class BungeeBridge extends Bridge {

    private final AdvancedBanHook advancedBanHook;
    private final BuyCraftHook buyCraftHook;
    private final LiteBansBungeeHook liteBansHook;
    private final LuckPermsHook luckPermsHook;
    private final ViaVersionBungeeHook viaVersionHook;

    @Inject
    public BungeeBridge(
            PlanConfig config,
            ErrorHandler errorHandler,

            AdvancedBanHook advancedBanHook,
            BuyCraftHook buyCraftHook,
            LiteBansBungeeHook liteBansHook,
            LuckPermsHook luckPermsHook,
            ViaVersionBungeeHook viaVersionHook
    ) {
        super(config, errorHandler);
        this.advancedBanHook = advancedBanHook;
        this.buyCraftHook = buyCraftHook;
        this.liteBansHook = liteBansHook;
        this.luckPermsHook = luckPermsHook;
        this.viaVersionHook = viaVersionHook;
    }

    @Override
    Hook[] getHooks() {
        return new Hook[]{
                advancedBanHook,
                buyCraftHook,
                liteBansHook,
                luckPermsHook,
                viaVersionHook
        };
    }
}