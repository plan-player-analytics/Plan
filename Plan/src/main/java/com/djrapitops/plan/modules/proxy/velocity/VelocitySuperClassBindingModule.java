package com.djrapitops.plan.modules.proxy.velocity;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.VelocityServerInfo;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.listeners.VelocityListenerSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.ProxyConfigSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.system.tasks.VelocityTaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Velocity specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class VelocitySuperClassBindingModule {

    @Provides
    @Singleton
    ServerInfo provideVelocityServerInfo(VelocityServerInfo velocityServerInfo) {
        return velocityServerInfo;
    }

    @Provides
    @Singleton
    ConfigSystem provideVelocityConfigSystem(ProxyConfigSystem proxyConfigSystem) {
        return proxyConfigSystem;
    }

    @Provides
    @Singleton
    TaskSystem provideVelocityTaskSystem(VelocityTaskSystem velocityTaskSystem) {
        return velocityTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideVelocityListenerSystem(VelocityListenerSystem velocityListenerSystem) {
        return velocityListenerSystem;
    }
}