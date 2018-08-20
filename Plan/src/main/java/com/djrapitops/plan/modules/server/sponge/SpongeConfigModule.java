package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.SpongeConfigSystem;
import com.djrapitops.plan.system.settings.theme.Theme;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bukkit Configuration.
 *
 * @author Rsl1122
 */
@Module
public class SpongeConfigModule {

    @Provides
    ConfigSystem provideConfigSystem(SpongeConfigSystem spongeConfigSystem) {
        return spongeConfigSystem;
    }

    @Provides
    PlanConfig provideConfig(ConfigSystem configSystem) {
        return configSystem.getConfig();
    }

    @Provides
    Theme provideTheme(ConfigSystem configSystem) {
        return configSystem.getTheme();
    }

}