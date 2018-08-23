package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.SpongeConfigSystem;
import com.djrapitops.plan.system.settings.theme.Theme;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Sponge Configuration.
 *
 * @author Rsl1122
 */
@Module
public class SpongeConfigModule {

    @Provides
    @Singleton
    ConfigSystem provideConfigSystem(SpongeConfigSystem spongeConfigSystem) {
        return spongeConfigSystem;
    }

    @Provides
    @Singleton
    PlanConfig provideConfig(ConfigSystem configSystem) {
        return configSystem.getConfig();
    }

    @Provides
    @Singleton
    Theme provideTheme(ConfigSystem configSystem) {
        return configSystem.getTheme();
    }

}