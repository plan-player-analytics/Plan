package com.djrapitops.plan.modules.proxy.bungee;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.system.info.server.properties.BungeeServerProperties;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee ServerProperties.
 *
 * @author Rsl1122
 */
@Module
public class BungeeServerPropertiesModule {

    @Provides
    @Singleton
    ServerProperties provideServerProperties(PlanBungee plugin, PlanConfig config) {
        return new BungeeServerProperties(plugin.getProxy(), config.getString(Settings.BUNGEE_IP));
    }
}