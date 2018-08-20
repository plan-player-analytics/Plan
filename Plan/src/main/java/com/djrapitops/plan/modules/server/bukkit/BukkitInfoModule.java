package com.djrapitops.plan.modules.server.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.info.server.properties.BukkitServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bukkit ServerInfo.
 *
 * @author Rsl1122
 */
@Module
public class BukkitInfoModule {

    @Provides
    ServerProperties provideServerProperties(Plan plugin) {
        return new BukkitServerProperties(plugin.getServer());
    }
}