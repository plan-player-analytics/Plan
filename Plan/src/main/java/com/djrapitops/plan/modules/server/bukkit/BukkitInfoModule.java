package com.djrapitops.plan.modules.server.bukkit;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.info.server.BukkitServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.properties.BukkitServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bukkit ServerInfo.
 *
 * @author Rsl1122
 */
@Module
public class BukkitInfoModule {

    @Provides
    @Singleton
    ServerInfo provideBukkitServerInfo(BukkitServerInfo bukkitServerInfo) {
        return bukkitServerInfo;
    }

    @Provides
    @Singleton
    ServerProperties provideServerProperties(Plan plugin) {
        return new BukkitServerProperties(plugin.getServer());
    }
}