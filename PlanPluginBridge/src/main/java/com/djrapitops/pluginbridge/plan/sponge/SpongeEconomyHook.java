package com.djrapitops.pluginbridge.plan.sponge;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * A Class responsible for hooking to Sponge and registering 1 data sources
 *
 * @author BrainStone
 * @since 4.4.6
 */
@Singleton
public class SpongeEconomyHook extends Hook {

    @Inject
    public SpongeEconomyHook() {
        super("org.spongepowered.api.Sponge");
        
        try {
            Optional<EconomyService> serviceOpt = Sponge.getServiceManager().provide(EconomyService.class);
            enabled = serviceOpt.isPresent();
        } catch(NoClassDefFoundError e) {
            enabled = false;
        }
    }

    @Override
    public void hook(HookHandler handler) {
        if (enabled) {
            handler.addPluginDataSource(new SpongeEconomyData(Sponge.getServiceManager().provide(EconomyService.class).get()));
        }
    }
}
