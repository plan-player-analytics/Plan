package com.djrapitops.plan.modules.server.bukkit;

import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.ServerConfigSystem;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plugin.config.Config;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bukkit Configuration.
 *
 * @author Rsl1122
 */
@Module
public class BukkitConfigModule {

    @Provides
    ConfigSystem provideConfigSystem() {
        return new ServerConfigSystem();
    }

    @Provides
    Config provideConfig(ConfigSystem configSystem) {
        return configSystem.getConfig();
    }

    @Provides
    Theme provideTheme(ConfigSystem configSystem) {
        return configSystem.getTheme();
    }

}