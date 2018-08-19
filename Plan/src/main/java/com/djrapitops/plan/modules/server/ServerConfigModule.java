package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.ServerConfigSystem;
import com.djrapitops.plan.system.settings.theme.Theme;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bukkit Configuration.
 *
 * @author Rsl1122
 */
@Module
public class ServerConfigModule {

    @Provides
    ConfigSystem provideConfigSystem() {
        return new ServerConfigSystem();
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