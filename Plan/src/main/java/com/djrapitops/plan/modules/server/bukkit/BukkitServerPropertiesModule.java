package com.djrapitops.plan.modules.server.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.info.server.properties.BukkitServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bukkit ServerProperties.
 *
 * @author Rsl1122
 */
@Module
public class BukkitServerPropertiesModule {

    @Provides
    @Singleton
    ServerProperties provideServerProperties(Plan plugin) {
        return new BukkitServerProperties(plugin.getServer());
    }
}