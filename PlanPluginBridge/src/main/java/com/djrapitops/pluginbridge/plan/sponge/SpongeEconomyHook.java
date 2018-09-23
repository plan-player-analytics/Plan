package com.djrapitops.pluginbridge.plan.sponge;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;
import java.util.Optional;

/**
 * A Class responsible for hooking to Sponge and registering 1 data sources
 *
 * @author BrainStone
 * @since 4.4.6
 */
public class SpongeEconomyHook extends Hook {
    public SpongeEconomyHook(HookHandler hookHandler) {
        super("org.spongepowered.api.Sponge", hookHandler);
        
        try {
            Optional<EconomyService> serviceOpt = Sponge.getServiceManager().provide(EconomyService.class);
            enabled = serviceOpt.isPresent();
        } catch(NoClassDefFoundError e) {
            enabled = false;
        }
    }

    @Override
    public void hook() {
        if (enabled) {
            addPluginDataSource(new SpongeEconomyData(Sponge.getServiceManager().provide(EconomyService.class).get()));
        }
    }
}
