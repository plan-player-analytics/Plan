package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.properties.BungeeServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee ServerInfo.
 *
 * @author Rsl1122
 */
@Module
public class BungeeInfoModule {

    @Provides
    @Singleton
    ServerInfo provideBungeeServerInfo(BungeeServerInfo bungeeServerInfo) {
        return bungeeServerInfo;
    }

    @Provides
    @Singleton
    ServerProperties provideServerProperties(PlanBungee plugin, PlanConfig config) {
        return new BungeeServerProperties(plugin.getProxy(), config.getString(Settings.BUNGEE_IP));
    }
}