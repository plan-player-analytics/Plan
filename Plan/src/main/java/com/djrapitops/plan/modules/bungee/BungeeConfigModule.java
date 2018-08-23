package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.data.plugin.PluginsConfigSection;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee Configuration.
 *
 * @author Rsl1122
 */
@Module
public class BungeeConfigModule {

    @Provides
    @Singleton
    ConfigSystem provideConfigSystem(BungeeConfigSystem bungeeConfigSystem) {
        return bungeeConfigSystem;
    }

    @Provides
    @Singleton
    PlanConfig provideConfig(ConfigSystem configSystem) {
        return configSystem.getConfig();
    }

    @Provides
    @Singleton
    PluginsConfigSection providePluginsConfigSection(PlanConfig config) {
        return config.getPluginsConfigSection();
    }

    @Provides
    @Singleton
    Theme provideTheme(ConfigSystem configSystem) {
        return configSystem.getTheme();
    }

}