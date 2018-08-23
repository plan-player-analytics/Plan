package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.properties.BungeeServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for Bukkit ServerInfo.
 *
 * @author Rsl1122
 */
@Module
public class BungeeInfoModule {

    @Provides
    ServerInfo provideBungeeServerInfo(BungeeServerInfo bungeeServerInfo) {
        return bungeeServerInfo;
    }

    @Provides
    ServerProperties provideServerProperties(PlanBungee plugin) {
        return new BungeeServerProperties(plugin.getProxy());
    }
}