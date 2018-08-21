package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bukkit Configuration.
 *
 * @author Rsl1122
 */
@Module
public class BungeeConfigModule {

    @Provides
    ConfigSystem provideConfigSystem(BungeeConfigSystem bungeeConfigSystem) {
        return bungeeConfigSystem;
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