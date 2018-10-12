package com.djrapitops.plan.modules.proxy.velocity;

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.system.info.server.properties.ServerProperties;
import com.djrapitops.plan.system.info.server.properties.VelocityServerProperties;
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
public class VelocityServerPropertiesModule {

    @Provides
    @Singleton
    ServerProperties provideServerProperties(PlanVelocity plugin, PlanConfig config) {
        return new VelocityServerProperties(plugin.getProxy(), config.getString(Settings.BUNGEE_IP));
    }

}